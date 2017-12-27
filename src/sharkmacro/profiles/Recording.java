package sharkmacro.profiles;

import java.util.ArrayList;

import sharkmacro.Constants;

public class Recording {

	private ArrayList<ArrayList<Double>> recordings;

	public Recording(ArrayList<ArrayList<Double>> recordings) {
		this.recordings = recordings;
	}

	public Profile toProfile() {

		// get rid of any differential in list size
		int minSize = Integer.MAX_VALUE;
		for (int i = 0; i < recordings.size(); i++) {
			if (recordings.get(i).size() < minSize) {
				minSize = recordings.get(i).size();
			}
		}
		for (int i = 0; i < recordings.size(); i++) {
			recordings.get(i).subList(minSize, recordings.get(i).size()).clear();
		}

		ArrayList<Double> leftPosition = recordings.get(0);
		ArrayList<Double> leftVelocity = recordings.get(1);
		ArrayList<Double> rightPosition = recordings.get(2);
		ArrayList<Double> rightVelocity = recordings.get(3);

		double[][] leftProfile = new double[minSize][3];
		double[][] rightProfile = new double[minSize][3];

		for (int i = 0; i < minSize; i++) {
			leftProfile[i][0] = leftPosition.get(i);
			leftProfile[i][1] = leftVelocity.get(i);
			leftProfile[i][2] = Constants.DT_MS;

			rightProfile[i][0] = rightPosition.get(i);
			rightProfile[i][1] = rightVelocity.get(i);
			rightProfile[i][2] = Constants.DT_MS;
		}
		return new Profile(leftProfile, rightProfile);
	}
}
