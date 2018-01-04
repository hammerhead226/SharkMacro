package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Notifier;

public class ProfileRecorder {

	/**
	 * Whether the {@link ProfileRecorder} is recording or not.
	 */
	private boolean isRecording = false;

	/**
	 * An array of the Talons being recorded.
	 */
	private final CANTalon[] talons;

	/**
	 * Holds the recorded positions of the left Talon.
	 */
	private ArrayList<Double> leftPosition = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded velocities of the left Talon.
	 */
	private ArrayList<Double> leftVelocity = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded positions of the right Talon.
	 */
	private ArrayList<Double> rightPosition = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded velocities of the right Talon.
	 */
	private ArrayList<Double> rightVelocity = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * A list of the lists holding the Talons' positions and velocities.
	 */
	private ArrayList<ArrayList<Double>> lists;

	/**
	 * Construct a new {@link ProfileRecorder} object.
	 * 
	 * @param left
	 *            the left Talon
	 * @param right
	 *            the right Talon
	 */
	public ProfileRecorder(CANTalon left, CANTalon right) {
		talons = new CANTalon[] { left, right };
		thread = new Notifier(new PeriodicRunnable());
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

		lists = new ArrayList<ArrayList<Double>>() {
			{
				add(leftPosition);
				add(leftVelocity);
				add(rightPosition);
				add(rightVelocity);
			}
		};
		isRecording = false;
		return new Recording(lists, talons[0], talons[1]);
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
			leftPosition.add(talons[0].getPosition());
			leftVelocity.add(talons[0].getSpeed());

			rightPosition.add(talons[1].getPosition());
			rightVelocity.add(talons[1].getSpeed());
		}
	}

	/**
	 * Object that takes a runnable class and starts a new thread to call its
	 * {@link java.lang.Runnable#run() run()} method periodically.
	 */
	Notifier thread;

}