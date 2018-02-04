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
	 * The motion profile to execute on {@link #leftTalon}.
	 */
	private double[][] leftProfile;

	/**
	 * The motion profile to execute on {@link #rightTalon}.
	 */
	private double[][] rightProfile;

	/**
	 * Represents the current point being streamed from the left profile to the left talon.
	 */
	int leftProfileIndex = 0;

	/**
	 * Represents the current point being streamed from the right profile to the right talon.
	 */
	int rightProfileIndex = 0;

	/**
	 * The PID gains profile {@link #leftTalon} will use to execute the motion
	 * profile
	 */
	private int leftGainsProfile;

	/**
	 * The PID gains profile {@link #rightTalon} will use to execute the motion
	 * profile
	 */
	private int rightGainsProfile;

	/**
	 * The talon used to execute the left motion profile.
	 */
	private TalonSRX leftTalon;

	/**
	 * The talon used to execute the right motion profile.
	 */
	private TalonSRX rightTalon;

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
	 * @param leftProfile
	 *            the motion profile to be executed on the left talon
	 * @param rightProfile
	 *            the motion profile to be executed on the right talon
	 * @param leftTalon
	 *            the talon to execute the left profile on
	 * @param rightTalon
	 *            the talon to execute the right profile on
	 * @param leftGainsProfile
	 *            the PID gains profile to use on the left talon
	 * @param rightGainsProfile
	 *            the PID gains profile to use on the right talon
	 */
	public ProfileHandler(final double[][] leftProfile, final double[][] rightProfile, TalonSRX leftTalon,
			TalonSRX rightTalon, int leftGainsProfile, int rightGainsProfile) {
		this.leftProfile = leftProfile;
		this.rightProfile = rightProfile;
		this.leftTalon = leftTalon;
		this.rightTalon = rightTalon;
		this.leftGainsProfile = leftGainsProfile;
		this.rightGainsProfile = rightGainsProfile;
		this.executionState = ExecutionState.WAITING;

		bufferThread = new Notifier(new PeriodicBufferProcessor());
		bufferThread.startPeriodic(Constants.DT_SECONDS / 2.0);
		this.leftTalon.changeMotionControlFramePeriod((int) Math.round((Constants.DT_MS / 2.0)));
		this.rightTalon.changeMotionControlFramePeriod((int) Math.round((Constants.DT_MS / 2.0)));

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
		leftTalon.clearMotionProfileTrajectories();
		rightTalon.clearMotionProfileTrajectories();
	}

	/**
	 * Called periodically while the motion profile is being executed. Manages the
	 * state of the Talon executing the motion profile.
	 */
	public void manage() {
		fillTalonsWithMotionProfile();
		leftTalon.getMotionProfileStatus(leftStatus);
		rightTalon.getMotionProfileStatus(rightStatus);

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
		leftTalon.set(ControlMode.MotionProfile, mode.value);
		rightTalon.set(ControlMode.MotionProfile, mode.value);
	}

	/**
	 * Fill the Talon's top-level buffer with a given motion profile.
	 * 
	 * @param gainsProfile
	 *            the PID gains profile to use to execute the motion profile
	 */
	private void fillTalonsWithMotionProfile() {

		// maybe need two point objects?
		TrajectoryPoint point = new TrajectoryPoint();

		if (leftProfileIndex == 0) {
			leftTalon.clearMotionProfileTrajectories();
			leftTalon.configMotionProfileTrajectoryPeriod(TrajectoryDuration.Trajectory_Duration_0ms.value, 0);
			leftTalon.clearMotionProfileHasUnderrun(0);
		}

		if (rightProfileIndex == 0) {
			rightTalon.clearMotionProfileTrajectories();
			rightTalon.configMotionProfileTrajectoryPeriod(TrajectoryDuration.Trajectory_Duration_0ms.value, 0);
			rightTalon.clearMotionProfileHasUnderrun(0);
		}

		while (leftStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT
				&& leftProfileIndex < leftProfile.length) {

			point.position = leftProfile[leftProfileIndex][0];
			point.velocity = leftProfile[leftProfileIndex][1];
			point.headingDeg = 0;
			point.timeDur = toTrajectoryDuration((int) leftProfile[leftProfileIndex][2]);
			point.profileSlotSelect0 = leftGainsProfile;
			point.profileSlotSelect1 = 0;
			point.zeroPos = false;
			if (leftProfileIndex == 0) {
				point.zeroPos = true;
			}

			point.isLastPoint = false;
			if ((leftProfileIndex + 1) == leftProfile.length) {
				point.isLastPoint = true;
			}

			leftTalon.pushMotionProfileTrajectory(point);

			leftTalon.getMotionProfileStatus(leftStatus);

			leftProfileIndex++;
		}

		while (rightStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT
				&& rightProfileIndex < rightProfile.length) {

			point.position = rightProfile[rightProfileIndex][0];
			point.velocity = rightProfile[rightProfileIndex][1];
			point.headingDeg = 0;
			point.timeDur = toTrajectoryDuration((int) rightProfile[rightProfileIndex][2]);
			point.profileSlotSelect0 = rightGainsProfile;
			point.profileSlotSelect1 = 0;
			point.zeroPos = false;
			if (rightProfileIndex == 0) {
				point.zeroPos = true;
			}

			point.isLastPoint = false;
			if ((rightProfileIndex + 1) == rightProfile.length) {
				point.isLastPoint = true;
			}

			rightTalon.pushMotionProfileTrajectory(point);

			rightTalon.getMotionProfileStatus(rightStatus);

			rightProfileIndex++;
		}
		
//		while ((leftStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT && leftProfileIndex < leftProfile.length)
//				|| (rightStatus.topBufferCnt < Constants.TALON_TOP_BUFFER_MAX_COUNT
//						&& rightProfileIndex < rightProfile.length)) {
//
//			leftPoint.position = leftProfile[leftProfileIndex][0];
//			rightPoint.position = rightProfile[rightProfileIndex][0];
//
//			leftPoint.velocity = leftProfile[leftProfileIndex][1];
//			rightPoint.velocity = rightProfile[rightProfileIndex][1];
//
//			leftPoint.headingDeg = 0;
//			rightPoint.headingDeg = 0;
//
//			leftPoint.timeDur = toTrajectoryDuration((int) leftProfile[leftProfileIndex][2]);
//			rightPoint.timeDur = toTrajectoryDuration((int) rightProfile[rightProfileIndex][2]);
//
//			leftPoint.profileSlotSelect0 = leftGainsProfile;
//			rightPoint.profileSlotSelect0 = rightGainsProfile;
//
//			leftPoint.profileSlotSelect1 = 0;
//			rightPoint.profileSlotSelect1 = 0;
//
//			leftPoint.zeroPos = false;
//			if (leftProfileIndex == 0) {
//				leftPoint.zeroPos = true;
//			}
//			rightPoint.zeroPos = false;
//			if (rightProfileIndex == 0) {
//				rightPoint.zeroPos = true;
//			}
//
//			leftPoint.isLastPoint = false;
//			if ((leftProfileIndex + 1) == leftProfile.length) {
//				leftPoint.isLastPoint = true;
//			}
//			rightPoint.isLastPoint = false;
//			if ((rightProfileIndex + 1) == rightProfile.length) {
//				rightPoint.isLastPoint = true;
//			}
//
//			leftTalon.pushMotionProfileTrajectory(leftPoint);
//			rightTalon.pushMotionProfileTrajectory(rightPoint);
//
//			leftTalon.getMotionProfileStatus(leftStatus);
//			rightTalon.getMotionProfileStatus(rightStatus);
//
//			leftProfileIndex++;
//			rightProfileIndex++;
//		}
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
			leftTalon.processMotionProfileBuffer();
			rightTalon.processMotionProfileBuffer();
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
