package org.hammerhead226.sharkmacro.motionprofiles;

import org.hammerhead226.sharkmacro.Constants;

import com.ctre.phoenix.motion.MotionProfileStatus;
import com.ctre.phoenix.motion.SetValueMotionProfile;
import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motion.TrajectoryPoint.TrajectoryDuration;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;

/**
 * Class to easily manage motion profile execution on a Talon SRX. Some logic
 * taken from <a href=
 * "https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java/MotionProfile/src/org/usfirst/frc/team217/robot/MotionProfileExample.java">here</a>
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
	 * The PID gains profile {@link #talon} will use to execute the motion profile.s
	 */
	private int gainsProfile;

	/**
	 * The Talon used to execute the motion profile.
	 */
	private TalonSRX talon;

	/**
	 * Object that takes a runnable class and starts a new thread to call its
	 * {@link java.lang.Runnable#run() run()} method periodically. This instance
	 * will handle {@link PeriodicExecutor}.
	 */
	private Notifier executorThread;

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
	private SetValueMotionProfile currentMode;

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
	private MotionProfileStatus status = new MotionProfileStatus();

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
	public ProfileHandler(final double[][] profile, TalonSRX talon, int gainsProfile) {
		this.profile = profile;
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
		setMode(SetValueMotionProfile.Disable);
	}

	/**
	 * Called after motion profile execution has finished.
	 */
	private void onFinish() {
		finished = true;
		bufferThread.stop();
		executorThread.stop();
		setMode(SetValueMotionProfile.Disable);
		talon.clearMotionProfileTrajectories();
	}

	/**
	 * Called periodically while the motion profile is being executed. Manages the
	 * state of the Talon executing the motion profile.
	 */
	public void manage() {
		fillTalonWithMotionProfile(gainsProfile);
		talon.getMotionProfileStatus(status);

		switch (executionState) {
		case WAITING:
			if (started) {
				started = false;
				setMode(SetValueMotionProfile.Disable);
				executionState = ExecutionState.STARTED;
			}
			break;
		case STARTED:
			if (status.btmBufferCnt > Constants.MINIMUM_POINTS_IN_TALON) {
				setMode(SetValueMotionProfile.Enable);
				executionState = ExecutionState.EXECUTING;
			}
			break;
		case EXECUTING:
			if (status.activePointValid && status.isLast) {
				onFinish();
			}
			break;
		}
	}

	/**
	 * Sets the state of {@link #talon}.
	 * 
	 * @param t
	 *            the motion profile mode to set the Talon to
	 */
	private void setMode(SetValueMotionProfile mode) {
		this.currentMode = mode;
		talon.set(ControlMode.MotionProfile, mode.value);
	}

	/**
	 * Fill the Talon's top-level buffer with a given motion profile.
	 * 
	 * @param gainsProfile
	 *            the PID gains profile to use to execute the motion profile
	 */
	int profileIndex = 0;

	private void fillTalonWithMotionProfile(int gainsProfile) {

		TrajectoryPoint point = new TrajectoryPoint();

		if (profileIndex == 0) {
			talon.clearMotionProfileTrajectories();
			talon.configMotionProfileTrajectoryPeriod(TrajectoryDuration.Trajectory_Duration_0ms.value, 0);
			talon.clearMotionProfileHasUnderrun(0);
		}

		while (status.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT && profileIndex < profile.length) {

			point.position = profile[profileIndex][0];
			point.velocity = profile[profileIndex][1];
			point.headingDeg = 0;
			point.timeDur = toTrajectoryDuration((int) profile[profileIndex][2]);
			point.profileSlotSelect0 = gainsProfile;
			point.profileSlotSelect1 = 0;
			point.zeroPos = false;
			if (profileIndex == 0) {
				point.zeroPos = true;
			}

			point.isLastPoint = false;
			if ((profileIndex + 1) == profile.length) {
				point.isLastPoint = true;
			}

			talon.pushMotionProfileTrajectory(point);

			talon.getMotionProfileStatus(status);

			profileIndex++;
		}
	}

	/**
	 * Converts an integer time value into a
	 * {@link com.ctre.phoenix.motion.TrajectoryPoint.TrajectoryDuration
	 * TrajectoryDuration}.
	 * 
	 * @param durationMs
	 *            time duration of the trajectory
	 * @return {@code TrajectoryDuration} with the value of the passed duration
	 */
	private TrajectoryDuration toTrajectoryDuration(int durationMs) {
		TrajectoryDuration dur = TrajectoryDuration.Trajectory_Duration_0ms;
		dur = dur.valueOf(durationMs);
		if (dur.value != durationMs) {
			DriverStation.getInstance();
			DriverStation.reportError(
					"Trajectory Duration not supported - use configMotionProfileTrajectoryPeriod instead", false);
		}
		return dur;
	}

	/**
	 * @return the {@link com.ctre.CANTalon.MotionProfileStatus
	 *         CANTalon.MotionProfileStatus} object of {@link #talon}
	 */
	public MotionProfileStatus getStatus() {
		return status;
	}

	/**
	 * @return the {@link TalonState} representing {@link #talon}'s current state
	 */
	public SetValueMotionProfile getMode() {
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
