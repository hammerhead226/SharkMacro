package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.hammerhead226.sharkmacro.Constants;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;

public class ActionList implements Iterable<Action>, Cloneable {
	
	private ArrayList<Action> actionList;
	
	private Timer timer = new Timer();
	
	private boolean finished = false;

	public ActionList(ArrayList<Action> list) {
		this.actionList = list;
		thread = new Notifier(new PeriodicRunnable());
	}

	public int getSize() {
		return actionList.size();
	}

	@Override
	public Iterator<Action> iterator() {
		return actionList.iterator();
	}
	
	public void execute() {
		finished = false;
		timer.start();
		thread.startPeriodic(Constants.DT_SECONDS);
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	class PeriodicRunnable implements java.lang.Runnable {
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
				finished = true;
				thread.stop();
				return;
			}
		}
	}

	Notifier thread;
	
	
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
