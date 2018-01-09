package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

/**
 * This class's function is to record a robot's actions and export them into an
 * {@link ActionList}.
 * 
 * @author Alec Minchington
 *
 */
public class ActionRecorder {

	/**
	 * Represents whether this {@link ActionRecorder} has started recording
	 * {@link Actions}.
	 */
	private static boolean started = false;

	/**
	 * {@link edu.wpi.first.wpilibj.Timer Timer} used to record the start and end
	 * time of {@link Action}s being recorded.
	 */
	private static Timer timer = new Timer();

	/**
	 * This list serves as a buffer for recorded {@link Action}s to be stored in
	 * before they're exported to an {@link ActionList}.
	 */
	private static ArrayList<Action> buffer = new ArrayList<Action>(Constants.ACTIONRECORDER_LIST_DEFAULT_LENGTH);

	/**
	 * This method starts listening for calls to {@link #addAction(Action)} and
	 * starts the {@link #timer}.
	 */
	public static void start() {
		if (!started) {
			buffer.clear();
			timer.start();
			started = true;
		}
	}

	/**
	 * This method stops listening for calls to {@link #addAction(Action)} and
	 * returns an {@link ActionList} containing the {@link Action}s in
	 * {@link #buffer}.
	 * 
	 * @return a new {@link ActionList} of the recorded {@link Action}s
	 */
	public static ActionList stop() {
		if (started) {
			timer.stop();
			timer.reset();
			started = false;
			return new ActionList(buffer);
		} else {
			System.out.println("Tried to stop recording but not started!");
			return new ActionList(new ArrayList<Action>());
		}
	}

	/**
	 * Adds an {@link Action} to the {@link #buffer}, to be made into an
	 * {@link ActionList} after recording.
	 * 
	 * @param a
	 *            the {@code Action} to be added
	 */
	public static void addAction(Action a) {
		if (started) {
			buffer.add(a);
		} else {
			System.out.println("Tried to add action while not recording! Call start() first.");
		}
	}

	/**
	 * This method gets the time since listening for calls to
	 * {@link #addAction(Action)} started.
	 * 
	 * @return the time elapsed since recording was started
	 */
	public static double getTime() {
		return timer.get();
	}

}
