package org.hammerhead226.sharkmacro.motionprofiles;

import org.hammerhead226.sharkmacro.Constants;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Notifier;

/**
 * Class to easily manage motion profile execution on a Talon SRX.
 * 
 * @author Alec Minchington
 *
 */
public class ProfileHandler implements Cloneable {

	/**
	 * The motion profile to execute.
	 */
	private double[][] profile;

	/**
	 * The length of {@link #profile}.
	 */
	private int profileSize;

	/**
	 * The PID gains profile {@link #talon} will use to execute the motion profile.s
	 */
	private int gainsProfile;

	/**
	 * The Talon used to execute the motion profile.
	 */
	private CANTalon talon;

	/**
	 * The current state of the motion profile execution manager.
	 * 
	 * @see ExecutionState
	 */
	private ExecutionState executionState;

	/**
	 * The current state of {@link #talon}.
	 * 
	 * @see TalonState
	 */
	private TalonState currentMode;

	/**
	 * Whether the motion profile execution has finished.
	 */
	private boolean finished = false;

	/**
	 * Whether the motion profile execution has started.
	 */
	private boolean started = false;

	/**
	 * The status of {@link #talon}.
	 */
	private CANTalon.MotionProfileStatus status = new CANTalon.MotionProfileStatus();

	/**
	 * Constructs a new {@link MotionProfileHandler} object that will handle the
	 * execution of {@link #profile} on {@link #talon}.
	 * 
	 * @param profile
	 *            the motion profile to be executed
	 * @param talon
	 *            the Talon to execute the profile on
	 * @param gainsProfile
	 *            the PID gains profile to use
	 */
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
	 * Called to start the execution of the motion profile.
	 */
	public void execute() {
		executorThread.startPeriodic(0.025);
		started = true;
	}

	/**
	 * Called if the motion profile execution needs to be prematurely stopped.
	 */
	public void onInterrupt() {
		bufferThread.stop();
		executorThread.stop();
		setMode(TalonState.NEUTRAL);
	}

	/**
	 * Called periodically while the motion profile is being executed. Manages the
	 * state of the Talon executing the motion profile.
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

	/**
	 * Sets the state of {@link #talon}.
	 * 
	 * @param t
	 *            the {@link TalonState} to set the Talon to
	 */
	private void setMode(TalonState t) {
		this.currentMode = t;
		talon.set(t.getValue());
	}

	/**
	 * Fill the Talon's top-level buffer with a given motion profile.
	 * 
	 * @param gainsProfile
	 *            the PID gains profile to use to execute the motion profile
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
	 * @return the {@link com.ctre.CANTalon.MotionProfileStatus
	 *         CANTalon.MotionProfileStatus} object of {@link #talon}
	 */
	public CANTalon.MotionProfileStatus getStatus() {
		return status;
	}

	/**
	 * @return the {@link TalonState} representing {@link #talon}'s current state
	 */
	public TalonState getMode() {
		return this.currentMode;
	}

	/**
	 * @return {@code true} when the motion profile is finished executing,
	 *         {@code false} otherwise
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Class to periodically call
	 * {@link com.ctre.CANTalon#processMotionProfileBuffer()
	 * processMotionProfileBufffer()} for {@link ProfileHandler#talon}.
	 */
	class PeriodicBufferProcessor implements java.lang.Runnable {
		public void run() {
			talon.processMotionProfileBuffer();
		}
	}

	/**
	 * Object that takes a runnable class and starts a new thread to call its
	 * {@link java.lang.Runnable#run() run()} method periodically. This instance
	 * will handle {@link PeriodicBufferProcessor}.
	 */
	Notifier bufferThread;

	/**
	 * Class to periodically call {@link ProfileHandler#manage()}.
	 */
	class PeriodicExecutor implements java.lang.Runnable {
		public void run() {
			manage();
		}
	}

}

/**
 * Represents Talon SRX motion profile execution modes.
 * 
 * @author Alec Minchington
 *
 */
enum TalonState {

	/**
	 * The possible states of the Talon while in motion profile execution mode.
	 * <p>
	 * {@link #NEUTRAL}: Talon is idle
	 * <p>
	 * {@link #EXECUTE}: Talon executes the motion profile contained in its buffer
	 * <p>
	 * {@link #HOLD}: Talon holds the current motion profile point
	 */
	NEUTRAL(0), EXECUTE(1), HOLD(2);

	/**
	 * The integer value of each state, passed to
	 * {@link com.ctre.CANTalon#set(double)} to change the Talon's mode.
	 */
	private int value;

	/**
	 * Construct a new {@link TalonState} with its given integer value.
	 * 
	 * @param value
	 *            the integer value of the {@link TalonState}
	 */
	private TalonState(int value) {
		this.value = value;
	}

	/**
	 * @return The integer value of this {@link TalonState}
	 */
	public int getValue() {
		return this.value;
	}
}

/**
 * Enum to help manage the current state of the motion profile execution.
 * 
 * @author Alec Minchington
 *
 */
enum ExecutionState {

	/**
	 * The possible states of motion profile execution.
	 * <p>
	 * {@link #WAITING}: The manager is waiting for motion profile execution to
	 * start
	 * <p>
	 * {@link #STARTED}: The manager starts the execution when the Talon's buffer is
	 * sufficiently filled
	 * <p>
	 * {@link #EXECUTING}: The manager waits for execution to finish and then safely
	 * exits the execution process
	 */
	WAITING, STARTED, EXECUTING;
}
