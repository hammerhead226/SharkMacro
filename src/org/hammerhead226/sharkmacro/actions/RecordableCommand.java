package org.hammerhead226.sharkmacro.actions;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

public class RecordableCommand extends Command {

	private double startTime;

	protected boolean isPlayback = false;

	public RecordableCommand() {
	}

	public void setTimeoutSeconds(double sec) {
		setTimeout(sec);
	}

	@Override
	protected void initialize() {
		if (!isPlayback) {
			startTime = ActionRecorder.getTime();
		}
		System.out.println(this.getClass().getName() + " started at " + Timer.getFPGATimestamp());
	}

	@Override
	protected boolean isFinished() {
		return isTimedOut();
	}

	@Override
	protected void end() {
		System.out.println(this.getClass().getName() + " ended at " + Timer.getFPGATimestamp());
		if (!isPlayback) {
			ActionRecorder.addAction(new Action(this.getClass().getName(), startTime, ActionRecorder.getTime()));
		}
	}

	@Override
	protected void interrupted() {
		end();
	}
}
