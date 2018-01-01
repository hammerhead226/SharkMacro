package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;
import java.util.Arrays;

public final class Action {

	private final String commandName;
	private final double startTime;
	private final double endTime;

	public Action(String commandName, double startTime, double endTime) {
		this.commandName = commandName;
		this.startTime = Math.round(startTime * 1000.0) / 1000.0;
		this.endTime = Math.round(endTime * 1000.0) / 1000.0;
	}

	public void start() {
		try {
			RecordableCommand rc = (RecordableCommand) Class.forName(commandName).getConstructor().newInstance();
			rc.setTimeoutSeconds(endTime - startTime);
			rc.isPlayback = true;
			rc.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] toStringArray() {
		@SuppressWarnings("serial")
		ArrayList<String> list = new ArrayList<String>() {
			{
				add(commandName);
				add(Double.toString(startTime));
				add(Double.toString(endTime));
			}
		};
		return Arrays.toString(list.toArray()).replace("[", "").replace("]", "").replace(" ", "").split(",");
	}

	public String getCommandName() {
		return this.commandName;
	}

	public double getStartTime() {
		return this.startTime;
	}

	public double getEndTime() {
		return this.endTime;
	}
}
