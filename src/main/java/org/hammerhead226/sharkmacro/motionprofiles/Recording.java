package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

/**
 * Intermediary class that represents a raw recording of a motion profile.
 * 
 * @author Alec Minchington
 *
 */
public class Recording {

	/**
	 * A list containing the lists of recorded positions and velocities.
	 */
	private ArrayList<ArrayList<Double>> recordings;

	/**
	 * Talons to pass to the {@link Profile} generated in {@link #toProfile()}.
	 */
	private MotionProfileTalonSRX[] talons;

	/**
	 * Constructs a new {@link Recording} object.
	 * 
	 * @param recordings
	 *            a list containing the lists of recorded positions and velocities
	 * @param talons
	 *            Talons used to record position and velocity, passed from
	 *            {@link ProfileRecorder#stop()}
	 */
	public Recording(ArrayList<ArrayList<Double>> recordings, MotionProfileTalonSRX[] talons) {
		this.recordings = recordings;
		this.talons = talons;
	}

	/**
	 * Transforms the raw recorded positions and velocities into a Talon-formatted
	 * motion profile. Each point in the motion profile is formatted as follows:
	 * <p>
	 * <center>
	 * {@code [ <position in raw units>, <velocity in raw units per 100ms>, <time for the Talon to hold this point> ]}
	 * </center>
	 * </p>
	 * 
	 * @return a new {@link Profile} containing the new motion profiles
	 */
	public Profile toProfile() {

		// Remove differential in list size
		int minSize = Integer.MAX_VALUE;
		for (int i = 0; i < recordings.size(); i++) {
			if (recordings.get(i).size() < minSize) {
				minSize = recordings.get(i).size();
			}
		}
		for (int i = 0; i < recordings.size(); i++) {
			recordings.get(i).subList(minSize, recordings.get(i).size()).clear();
		}

		// Remove leading zero rows
		for (int i = 0; i < minSize; i++) {
			if (!areEqual(i, 0)) {
				for (int j = 0; j < recordings.size(); j++) {
					recordings.get(j).subList(0, i).clear();
				}
				minSize -= i;
				break;
			}
		}

		double[][][] profiles = new double[talons.length][][];

		for (int i = 0; i < talons.length; i = i + 2) {
			ArrayList<Double> position = recordings.get(i);
			ArrayList<Double> feedForwardValues = recordings.get(i + 1);
			MotionProfileTalonSRX talon = talons[i];
			for (int j = 0; j < minSize; j++) {
				talon.getProfile_Double()[j][0] = position.get(j);
				talon.getProfile_Double()[j][1] = feedForwardValues.get(j);
				talon.getProfile_Double()[j][2] = Constants.DT_MS;
			}
			profiles[i] = talon.getProfile_Double();
		}
		return new Profile(profiles);
	}

	/**
	 * Converts an {@code ArrayList} of type Integer to an {@code ArrayList} of type
	 * Double.
	 * 
	 * @param list
	 *            list to convert
	 * @return converted list
	 */
	private ArrayList<Double> toDoubleList(ArrayList<Integer> list) {
		ArrayList<Double> d = new ArrayList<Double>(list.size());
		for (Integer i : list) {
			d.add((double) i);
		}
		return d;
	}

	/**
	 * Compares the positions and velocities at a given instant in time.
	 * 
	 * @param idx
	 *            list index to get comparable values from
	 * @param comparator
	 *            value to compare the positions and velocities to
	 * @return {@code true} if all four positions and velocities are equal to the
	 *         comparator, {@code false} otherwise
	 */
	private boolean areEqual(int idx, double comparator) {
		return (recordings.get(0).get(idx) == comparator && recordings.get(1).get(idx) == comparator
				&& recordings.get(2).get(idx) == comparator && recordings.get(3).get(idx) == comparator);
	}

	/**
	 * Converts raw units into wheel rotations.
	 * 
	 * @param rawUnits
	 *            raw sensor units
	 * @return raw units as wheel rotations
	 */
	private double toRotations(double rawUnits) {
		return rawUnits * (1.0 / Constants.ENCODER_COUNTS_PER_REV);
	}

	/**
	 * Converts raw units per 100ms into RPM.
	 * 
	 * @param rawUnitsPer100ms
	 *            raw sensor units
	 * @return raw units per 100ms as RPM
	 */
	private double toRPM(double rawUnitsPer100ms) {
		return rawUnitsPer100ms * (600.0 / Constants.ENCODER_COUNTS_PER_REV);
	}
}
