package sharkmacro.motionprofiles;

import java.util.ArrayList;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Notifier;
import sharkmacro.Constants;
import sharkmacro.SharkMacro;
import sharkmacro.SharkMacroNotInitialized;

public class ProfileRecorder {

	private final double dt_s = Constants.DT_SECONDS;

	private final CANTalon[] talons;

	private ArrayList<Double> leftPosition = new ArrayList<Double>(Constants.ARRAYLIST_DEFAULT_LENGTH);
	private ArrayList<Double> leftVelocity = new ArrayList<Double>(Constants.ARRAYLIST_DEFAULT_LENGTH);
	private ArrayList<Double> rightPosition = new ArrayList<Double>(Constants.ARRAYLIST_DEFAULT_LENGTH);
	private ArrayList<Double> rightVelocity = new ArrayList<Double>(Constants.ARRAYLIST_DEFAULT_LENGTH);

	private ArrayList<ArrayList<Double>> lists;

	/**
	 * Construct a new ProfileRecorder.
	 */
	public ProfileRecorder(CANTalon left, CANTalon right) {
		talons = new CANTalon[] { left, right };
		thread = new Notifier(new PeriodicRunnable());
	}

	public void start() {
		// Create new thread running at dt to record encoder readings
		clear();
		thread.startPeriodic(dt_s);
	}

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

		return new Recording(lists);
	}

	private void clear() {
		if (lists != null) {
			for (int i = 0; i < lists.size(); i++) {
				lists.get(i).clear();
			}
		}
	}

	class PeriodicRunnable implements java.lang.Runnable {
		public void run() {
			leftPosition.add(talons[0].getPosition());
			leftVelocity.add(talons[0].getSpeed());

			rightPosition.add(talons[1].getPosition());
			rightVelocity.add(talons[1].getSpeed());
		}
	}

	Notifier thread;

}