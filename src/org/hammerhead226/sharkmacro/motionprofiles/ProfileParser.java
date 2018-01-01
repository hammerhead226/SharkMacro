package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;
import java.util.List;

import org.hammerhead226.sharkmacro.Constants;
import org.hammerhead226.sharkmacro.Parser;

import com.ctre.CANTalon;

public final class ProfileParser extends Parser {
	
	public ProfileParser() {
		super(Constants.PROFILE_STORAGE_DIRECTORY, Constants.PROFILE_DEFAULT_PREFIX);
	}
	
	public ProfileParser(String filename) {
		super(Constants.PROFILE_STORAGE_DIRECTORY, Constants.PROFILE_DEFAULT_PREFIX, filename);
	}

	public boolean writeToFile(Profile profile) {
		ArrayList<String[]> profileToWrite = new ArrayList<String[]>(profile.length);
		String[][] left = profile.getLeftProfile_String();
		String[][] right = profile.getRightProfile_String();

		for (int i = 0; i < profile.length; i++) {
			String[] line = new String[] { left[i][0], left[i][1], right[i][0], right[i][1],
					Integer.toString(profile.dt, 10) };
			profileToWrite.add(line);
		}

		return super.writeToFile(profileToWrite);
	}

	public Profile toObject(CANTalon leftTalon, CANTalon rightTalon) {
		if (inCache()) {
			return (Profile) ((Profile) getFromCache()).clone();
		}
		
		List<String[]> profileRaw = readFromFile();
		
		// Process read values into Profile
		String[][] left = new String[profileRaw.size()][3];
		String[][] right = new String[profileRaw.size()][3];

		for (int i = 0; i < profileRaw.size(); i++) {
			left[i][0] = profileRaw.get(i)[0];
			left[i][1] = profileRaw.get(i)[1];
			left[i][2] = profileRaw.get(i)[4];

			right[i][0] = profileRaw.get(i)[2];
			right[i][1] = profileRaw.get(i)[3];
			right[i][2] = profileRaw.get(i)[4];
		}

		Profile p = new Profile(left, right, leftTalon, rightTalon);
		putInCache(p);

		return p;
	}

}
