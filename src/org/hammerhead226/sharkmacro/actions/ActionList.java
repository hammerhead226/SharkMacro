package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.hammerhead226.sharkmacro.Constants;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;

/**
 * A list of {@link Action}s. This is executed concurrently with a
 * {@link org.hammerhead226.sharkmacro.motionprofiles.Profile Profile} to form a
 * complete autonomous routine.
 * 
 * @author Alec Minchington
 *
 */
public class ActionList implements Iterable<Action>, Cloneable {

	/**
	 * List that holds the {@link Action}s that make up this {@link ActionList}.
	 */
	private ArrayList<Action> actionList;

	/**
	 * {@link edu.wpi.first.wpilibj.Timer Timer} used to keep track of which
	 * {@link Action}s should be executed when.
	 */
	private Timer timer = new Timer();

	/**
	 * Represents whether this {@link ActionList} has finished executing.
	 */
	private boolean isFinished = false;

	/**
	 * Constructs a new {@link ActionList} object.
	 * 
	 * @param list
	 *            the list of {@link Action}s this {@code ActionList} represents
	 */
	public ActionList(ArrayList<Action> list) {
		this.actionList = list;
		thread = new Notifier(new PeriodicRunnable());
	}

	/**
	 * This method returns the {@link java.util.ArrayList#size() size()} property of
	 * this class's {@link #actionList} member.
	 * 
	 * @return the number of {@link Action}s this {@code ActionList} contains
	 */
	public int getSize() {
		return actionList.size();
	}

	/**
	 * This method allows {@link ActionList} to be treated as an iterable. This
	 * allows it to be used in a foreach loop, etc.
	 */
	@Override
	public Iterator<Action> iterator() {
		return actionList.iterator();
	}

	/**
	 * This method starts the execution process of this {@link ActionList}.
	 */
	public void execute() {
		isFinished = false;
		timer.start();
		thread.startPeriodic(Constants.DT_SECONDS);
	}

	/**
	 * This method returns this class's {@link #finished} property.
	 * 
	 * @return {@code true} if the {@code ActionList} is finished executing,
	 *         {@code false} otherwise
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Simple class to run code periodically. Passed to a
	 * {@link edu.wpi.first.wpilibj.Notifier Notifier} instance, which calls
	 * {@link PeriodicRunnable#run() run()} periodically.
	 */
	class PeriodicRunnable implements java.lang.Runnable {

		/**
		 * Poll the next {@link Action} in the list to see if it is time to start it. If
		 * it is time, start the {@code Action} and remove it from the list. Then begin
		 * polling the next {@code Action}.
		 */
		public void run() {
			int i = 0;
			while (i < actionList.size()) {
				if (actionList.get(i).getStartTime() <= timer.get()) {
					actionList.get(i).start();
					actionList.remove(i);
				} else {
					i++;
				}
			}
			if (actionList.size() == 0) {
				isFinished = true;
				thread.stop();
				return;
			}
		}
	}

	/**
	 * Object that takes a runnable class and starts a new thread to call its
	 * {@link java.lang.Runnable#run() run()} method periodically.
	 */
	Notifier thread;

	/**
	 * This method clones this class and all of its members.
	 * 
	 * @return a new copy of this {@link ActionList}
	 */
	protected Object clone() {
		try {
			ActionList al = (ActionList) super.clone();
			al.actionList = (ArrayList<Action>) this.actionList.clone();
			al.timer = new Timer();
			return al;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Object();
	}
}
