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
public class ProfileHandler {

	/**
	 * List of the motion profiles to be executed.
	 */
	private double[][][] profiles;

	/**
	 * Represents the current point being streamed from the left profile to the left
	 * talon.
	 */
	private int profileIndex = 0;

	/**
	 * List of PID gains slots to use on respective talons for motion profile
	 * execution.
	 */
	private int[] pidSlotIdxs;

	/**
	 * Talons to be used for motion profile execution.
	 */
	private TalonSRX[] talons;

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
	 * The current state of the talons.
	 * 
	 * @see SetValueMotionProfile
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
	 * The status of {@link #leftTalon}.
	 */
	private MotionProfileStatus leftStatus = new MotionProfileStatus();

	/**
	 * The status of {@link #rightTalon}.
	 */
	private MotionProfileStatus rightStatus = new MotionProfileStatus();

	/**
	 * Constructs a new {@link MotionProfileHandler} object that will handle the
	 * execution of the given motion profiles on their respective talons.
	 * 
	 * @param profiles
	 *            the motion profiles to be executed on their respective talon
	 * @param talons
	 *            the talons to execute the motion profiles on
	 * @param pidSlotIdxs
	 *            the pid profile slots to execute the motion profiles with
	 */
	public ProfileHandler(final double[][][] profiles, TalonSRX[] talons, int[] pidSlotIdxs) {
		this.profiles = profiles;
		this.talons = talons;
		this.pidSlotIdxs = pidSlotIdxs;
		this.executionState = ExecutionState.WAITING;

		bufferThread = new Notifier(new PeriodicBufferProcessor());
		bufferThread.startPeriodic(Constants.DT_SECONDS / 2.0);
		
		this.talons[0].changeMotionControlFramePeriod(Constants.MOTIONCONTROL_FRAME_PERIOD);
		this.talons[1].changeMotionControlFramePeriod(Constants.MOTIONCONTROL_FRAME_PERIOD);

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
		talons[0].clearMotionProfileTrajectories();
		talons[1].clearMotionProfileTrajectories();
	}

	/**
	 * Called periodically while the motion profile is being executed. Manages the
	 * state of the Talons executing the motion profiles.
	 */
	public void manage() {
		fillTalonsWithMotionProfile();
		talons[0].getMotionProfileStatus(leftStatus);
		talons[1].getMotionProfileStatus(rightStatus);

		switch (executionState) {
		case WAITING:
			if (started) {
				started = false;
				setMode(SetValueMotionProfile.Disable);
				executionState = ExecutionState.STARTED;
			}
			break;
		case STARTED:
			if (leftStatus.btmBufferCnt > Constants.MINIMUM_POINTS_IN_TALON
					&& rightStatus.btmBufferCnt > Constants.MINIMUM_POINTS_IN_TALON) {
				setMode(SetValueMotionProfile.Enable);
				executionState = ExecutionState.EXECUTING;
			}
			break;
		case EXECUTING:
			if ((leftStatus.activePointValid && leftStatus.isLast)
					&& (rightStatus.activePointValid && rightStatus.isLast)) {
				onFinish();
			}
			break;
		}
	}

	/**
	 * Sets the state of the talons.
	 * 
	 * @param t
	 *            the motion profile mode to set the Talon to
	 */
	private void setMode(SetValueMotionProfile mode) {
		this.currentMode = mode;
		talons[0].set(ControlMode.MotionProfile, mode.value);
		talons[1].set(ControlMode.MotionProfile, mode.value);
	}

	/**
	 * Fill the Talons' top-level buffer with a given motion profile.
	 * 
	 */
	private void fillTalonsWithMotionProfile() {

		// maybe need two point objects?
		TrajectoryPoint leftPoint = new TrajectoryPoint();
		TrajectoryPoint rightPoint = new TrajectoryPoint();

		if (profileIndex == 0) {
			talons[0].clearMotionProfileTrajectories();
			talons[0].configMotionProfileTrajectoryPeriod(TrajectoryDuration.Trajectory_Duration_0ms.value, 0);
			talons[0].clearMotionProfileHasUnderrun(0);
			talons[1].clearMotionProfileTrajectories();
			talons[1].configMotionProfileTrajectoryPeriod(TrajectoryDuration.Trajectory_Duration_0ms.value, 0);
			talons[1].clearMotionProfileHasUnderrun(0);
		}

		while ((leftStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT && profileIndex < profiles[0].length)
				|| (rightStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT
						&& profileIndex < profiles[1].length)) {

			leftPoint.position = profiles[0][profileIndex][0];
			rightPoint.position = profiles[1][profileIndex][0];

			leftPoint.velocity = profiles[0][profileIndex][1];
			rightPoint.velocity = profiles[1][profileIndex][1];

			leftPoint.headingDeg = 0;
			rightPoint.headingDeg = 0;

			leftPoint.timeDur = toTrajectoryDuration((int) profiles[0][profileIndex][2]);
			rightPoint.timeDur = toTrajectoryDuration((int) profiles[1][profileIndex][2]);

			leftPoint.profileSlotSelect0 = pidSlotIdxs[0];
			rightPoint.profileSlotSelect0 = pidSlotIdxs[1];

			leftPoint.profileSlotSelect1 = 0;
			rightPoint.profileSlotSelect1 = 0;

			leftPoint.zeroPos = false;
			rightPoint.zeroPos = false;
			if (profileIndex == 0) {
				leftPoint.zeroPos = true;
				rightPoint.zeroPos = true;
			}

			leftPoint.isLastPoint = false;
			rightPoint.isLastPoint = false;
			if ((profileIndex + 1) == profiles[0].length) {
				leftPoint.isLastPoint = true;
			}
			if ((profileIndex + 1) == profiles[1].length) {
				rightPoint.isLastPoint = true;
			}

			talons[0].pushMotionProfileTrajectory(leftPoint);
			talons[1].pushMotionProfileTrajectory(rightPoint);

			talons[0].getMotionProfileStatus(leftStatus);
			talons[1].getMotionProfileStatus(rightStatus);

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
	 *         CANTalon.MotionProfileStatus} objects of each of the talons.
	 */
	public MotionProfileStatus[] getStatus() {
		return new MotionProfileStatus[] { leftStatus, rightStatus };
	}

	/**
	 * @return the {@link TalonState} representing the talons' current state
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
	 * processMotionProfileBufffer()} for {@link ProfileHandler#leftTalon} and
	 * {@link ProfileHandler#rightTalon}.
	 */
	class PeriodicBufferProcessor implements java.lang.Runnable {
		public void run() {
			talons[0].processMotionProfileBuffer();
			talons[1].processMotionProfileBuffer();
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
