package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import com.ctre.CANTalon;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Notifier;

/**
 * Class for recording motion profiles in real time.
 * 
 * @author Alec Minchington
 *
 */
public class ProfileRecorder {

	/**
	 * Whether the {@link ProfileRecorder} is recording or not.
	 */
	private boolean isRecording = false;

	/**
	 * An array of the Talons being recorded.
	 */
	private final TalonSRX[] talons;

	/**
	 * Holds the recorded positions of the left Talon.
	 */
	private ArrayList<Integer> leftPosition = new ArrayList<Integer>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded velocities of the left Talon.
	 */
	private ArrayList<Integer> leftVelocity = new ArrayList<Integer>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded positions of the right Talon.
	 */
	private ArrayList<Integer> rightPosition = new ArrayList<Integer>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * Holds the recorded velocities of the right Talon.
	 */
	private ArrayList<Integer> rightVelocity = new ArrayList<Integer>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * A list of the lists holding the Talons' positions and velocities.
	 */
	private ArrayList<ArrayList<Integer>> lists;
	
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
	 */
	public ProfileRecorder(TalonSRX left, TalonSRX right) {
		talons = new TalonSRX[] { left, right };
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

		synchronized (listLock) {
			lists = new ArrayList<ArrayList<Integer>>() {
				{
					add(leftPosition);
					add(leftVelocity);
					add(rightPosition);
					add(rightVelocity);
				}
			};
		}
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
			synchronized (listLock) {
				leftPosition.add(talons[0].getSelectedSensorPosition(0));
				leftVelocity.add(talons[0].getSelectedSensorVelocity(0));

				rightPosition.add(talons[1].getSelectedSensorPosition(0));
				rightVelocity.add(talons[1].getSelectedSensorVelocity(0));
			}
		}
	}

}