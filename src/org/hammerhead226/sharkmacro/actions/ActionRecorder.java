package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

public class ActionRecorder {

	private static boolean started = false;

	private static Timer timer = new Timer();

	private static ArrayList<Action> buffer = new ArrayList<Action>(Constants.ACTIONRECORDER_LIST_DEFAULT_LENGTH);

	public static void start() {
		if (!started) {
			buffer.clear();
			timer.start();
			started = true;
		}
	}

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

	public static double getTime() {
		return timer.get();
	}

	public static void addAction(Action a) {
		if (started) {
			buffer.add(a);
		} else {
			System.out.println("Tried to add action while not recording! Call start() first.");
		}
	}

}
