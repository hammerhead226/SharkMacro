package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import edu.wpi.first.wpilibj.Notifier;

/**
 * Class for recording motion profiles in real time.
 * 
 * @author Alec Minchington, Nidhi Jaison
 *
 */
public class ProfileRecorder {

	/**
	 * Whether the {@link ProfileRecorder} is recording or not.
	 */
	private boolean isRecording = false;

	/**
	 * Whether the {@link ProfileRecorder} will record voltage or speed.
	 */
	private RecordingType recordingType;

	/**
	 * An array of the Talons being recorded.
	 */
	private final MotionProfileTalonSRX[] talons;

	/**
	 * A list of the lists holding the Talons' positions and velocities.
	 */
	private ArrayList<ArrayList<Double>> lists;

	/**
	 * Object that takes a runnable class and starts a new thread to call its
	 * {@link java.lang.Runnable#run() run()} method periodically.
	 */
	Notifier thread;

	Object listLock = new Object();

	/**
	 * Construct a new {@link ProfileRecorder} object.
	 * 
	 * @param left
	 *            the left Talon
	 * @param right
	 *            the right Talon
	 * @param recordingType
	 *            the type of data that will be recorded, either voltage or velocity
	 */
	public ProfileRecorder(RecordingType recordingType, MotionProfileTalonSRX...talons) {
		this.talons = talons;
		thread = new Notifier(new PeriodicRunnable());
		this.recordingType = recordingType;
	}

	/**
	 * Clear previously recorded data and start recording new motion profiles.
	 */
	public void start() {
		clear();
		thread.startPeriodic(Constants.DT_SECONDS);
		isRecording = true;
	}

	/**
	 * Stops recording and exports the recorded positions and velocities to a new
	 * {@link Recording}.
	 * 
	 * @return a new {@code Recording} of the recorded data
	 */
	public Recording stop() {
		// Stop recording encoder readings
		thread.stop();

		synchronized (listLock) {
			lists = new ArrayList<ArrayList<Double>>() {
				{
					for(MotionProfileTalonSRX talon : talons) {
						add(talon.position);
						add(talon.feedForwardValues);
					}
				}
			};
		}
		isRecording = false;
		return new Recording(lists, talons);
	}

	/**
	 * Clear all recorded data.
	 */
	private void clear() {
		if (lists != null) {
			for (int i = 0; i < lists.size(); i++) {
				lists.get(i).clear();
			}
		}
	}

	/**
	 * @return {@code true} if data is currently being recorded, {@code false}
	 *         otherwise
	 */
	public boolean isRecording() {
		return isRecording;
	}

	/**
	 * Simple class to run code periodically. Passed to a
	 * {@link edu.wpi.first.wpilibj.Notifier Notifier} instance, which calls
	 * {@link PeriodicRunnable#run() run()} periodically.
	 */
	class PeriodicRunnable implements java.lang.Runnable {

		/**
		 * Add position and velocity readings from the Talons to their respective list.
		 */
		public void run() {
			synchronized (listLock) {
				if (recordingType == RecordingType.VOLTAGE) {
					for(MotionProfileTalonSRX talon : talons) {
						talon.position.add((double) talon.getSelectedSensorPosition(0));
						talon.feedForwardValues.add(talon.getMotorOutputVoltage());
					}
				} else {
					for(MotionProfileTalonSRX talon : talons) {
						talon.position.add((double) talon.getSelectedSensorPosition(0));
						talon.feedForwardValues.add((double) talon.getSelectedSensorVelocity());
					}
				}
			}
		}
	}

	public enum RecordingType {
		VELOCITY, VOLTAGE;
	}
}