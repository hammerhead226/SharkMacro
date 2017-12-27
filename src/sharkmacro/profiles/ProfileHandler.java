package sharkmacro.profiles;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Notifier;
import sharkmacro.Constants;

public class ProfileHandler {

	private final double[][] profile;
	private int profileSize;
	private int gainsProfile;
	private CANTalon talon;
	private ExecutionState executionState;
	private TalonState currentMode;
	private boolean finished = false;
	private boolean started = false;

	private CANTalon.MotionProfileStatus status = new CANTalon.MotionProfileStatus();

	public ProfileHandler(final double[][] profile, CANTalon talon, int gainsProfile) {
		this.profile = profile;
		this.profileSize = profile.length;
		this.talon = talon;
		this.gainsProfile = gainsProfile;
		this.executionState = ExecutionState.WAITING;

		bufferThread = new Notifier(new PeriodicBufferProcessor());
		bufferThread.startPeriodic(Constants.DT_SECONDS / 2.0);
		this.talon.changeMotionControlFramePeriod((int) Math.round((Constants.DT_MS / 2.0)));

		executorThread = new Notifier(new PeriodicExecutor());
	}

	/**
	 * Call this to start the execution of the motion profile.
	 */
	public void execute() {
		executorThread.startPeriodic(0.025);
		started = true;
	}

	public void onInterrupt() {
		bufferThread.stop();
		executorThread.stop();
		setMode(TalonState.NEUTRAL);
	}

	/**
	 * Called periodically while the motion profile is being executed
	 */
	public void manage() {
		talon.getMotionProfileStatus(status);

		switch (executionState) {
		case WAITING:
			if (started) {
				started = false;
				setMode(TalonState.NEUTRAL);
				talon.changeControlMode(TalonControlMode.MotionProfile);
				fillTalonWithMotionProfile(gainsProfile);
				executionState = ExecutionState.STARTED;
			}
			break;
		case STARTED:
			if (status.btmBufferCnt > Constants.MINIMUM_POINTS_IN_TALON) {
				setMode(TalonState.EXECUTE);
				executionState = ExecutionState.EXECUTING;
			}
			break;
		case EXECUTING:
			if (status.activePointValid && status.activePoint.isLastPoint) {
				setMode(TalonState.NEUTRAL);
				bufferThread.stop();
				executorThread.stop();
				finished = true;
			}
			break;
		}
	}

	private void setMode(TalonState t) {
		this.currentMode = t;
		talon.set(t.getValue());
	}

	/**
	 * Fill the Talon's top-level buffer with a given motion profile
	 * 
	 * @param gainsProfile
	 */
	private void fillTalonWithMotionProfile(int gainsProfile) {

		CANTalon.TrajectoryPoint point = new CANTalon.TrajectoryPoint();

		talon.clearMotionProfileTrajectories();

		for (int i = 0; i < profileSize; ++i) {
			point.position = profile[i][0];
			point.velocity = profile[i][1];
			point.timeDurMs = (int) profile[i][2];
			point.profileSlotSelect = gainsProfile;
			point.velocityOnly = false;
			point.zeroPos = false;
			if (i == 0) {
				point.zeroPos = true;
			}

			point.isLastPoint = false;
			if ((i + 1) == profileSize) {
				point.isLastPoint = true;
			}

			talon.pushMotionProfileTrajectory(point);
		}
	}

	/**
	 * @return the status object of the Talon being controlled
	 */
	public CANTalon.MotionProfileStatus getStatus() {
		return status;
	}

	public TalonState getMode() {
		return this.currentMode;
	}

	/**
	 * @return {@code true} when the motion profile is finished executing
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Class to periodically call processMotionProfileBuffer for the Talon
	 */
	class PeriodicBufferProcessor implements java.lang.Runnable {
		public void run() {
			talon.processMotionProfileBuffer();
		}
	}

	Notifier bufferThread;

	class PeriodicExecutor implements java.lang.Runnable {
		public void run() {
			manage();
		}
	}

	Notifier executorThread;
}

enum TalonState {

	NEUTRAL(0), EXECUTE(1), HOLD(2);

	private int value;

	private TalonState(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}

enum ExecutionState {
	WAITING, STARTED, EXECUTING;
}
