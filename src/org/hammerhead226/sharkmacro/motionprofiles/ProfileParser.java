package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;
import java.util.List;

import org.hammerhead226.sharkmacro.Constants;
import org.hammerhead226.sharkmacro.Parser;
import org.hammerhead226.sharkmacro.actions.ActionList;
import org.hammerhead226.sharkmacro.actions.ActionListParser;

import com.ctre.CANTalon;

public final class ProfileParser extends Parser {

	/**
	 * Constructs a new {@link ProfileParser} object.
	 * {@link org.hammerhead226.sharkmacro.Parser#filename Parser.filename} will be
	 * set to the value of
	 * {@link org.hammerhead226.sharkmacro.Parser#getNewFilename()
	 * Parser.getNewFilename()}.
	 * 
	 */
	public ProfileParser() {
		super(Constants.PROFILE_STORAGE_DIRECTORY, Constants.PROFILE_DEFAULT_PREFIX);
	}

	/**
	 * Constructs a new {@link ProfileParser} object.
	 * 
	 * @param filename
	 *            name of the file to read or write a new file with
	 */
	public ProfileParser(String filename) {
		super(Constants.PROFILE_STORAGE_DIRECTORY, Constants.PROFILE_DEFAULT_PREFIX, filename);
	}

	/**
	 * This method writes an {@link Profile} to a file. The given {@code Profile} is
	 * transformed into a writable list and then passed to
	 * {@link org.hammerhead226.sharkmacro.Parser Parser} to be written.
	 * 
	 * @param al
	 *            the {@code Profile} instance to write to file
	 * @return {@code true} if the file was written successfully, {@code false} if
	 *         not
	 */
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

	/**
	 * This method gets the raw action list from
	 * {@link org.hammerhead226.sharkmacro.Parser Parser} and transforms it into an
	 * {@link Profile} instance.
	 * <p>
	 * {@link org.hammerhead226.sharkmacro.Parser#cache Parser.cache} is checked
	 * first to see if the file has already been parsed, and returns a clone of the
	 * {@code Profile} from the cache if the file exists in the cache. If the file
	 * does not exist in the cache, then the file is parsed and added to the cache.
	 * 
	 * @return a new {@code Profile} instance
	 */
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
