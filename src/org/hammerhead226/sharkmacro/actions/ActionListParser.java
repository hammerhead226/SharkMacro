package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;
import java.util.List;

import org.hammerhead226.sharkmacro.Constants;
import org.hammerhead226.sharkmacro.Parser;
import org.hammerhead226.sharkmacro.motionprofiles.Profile;

/**
 * Handles the reading and writing of {@link ActionList}s.
 * 
 * @author Alec Minchington
 *
 */
public class ActionListParser extends Parser {

	/**
	 * Constructs a new {@link ActionListParser} object.
	 * 
	 * @param filename
	 *            name of the file to read or write a new file with
	 */
	public ActionListParser(String filename) {
		super(Constants.ACTIONLIST_STORAGE_DIRECTORY, Constants.ACTIONLIST_DEFAULT_PREFIX, filename);
	}

	/**
	 * This method writes an {@link ActionList} to a file. The given
	 * {@code ActionList} is transformed into a writable list and then passed to
	 * {@link org.hammerhead226.sharkmacro.Parser Parser} to be written.
	 * 
	 * @param al
	 *            the {@code ActionList} instance to write to file
	 * @return {@code true} if the file was written successfully, {@code false} if
	 *         not
	 */
	public boolean writeToFile(ActionList al) {
		if (al.getSize() == 0) {
			System.out.println("Tried to write empty ActionList");
			return false;
		}

		ArrayList<String[]> actionListToWrite = new ArrayList<String[]>(al.getSize());
		for (Action a : al) {
			actionListToWrite.add(a.toStringArray());
		}

		return super.writeToFile(actionListToWrite);
	}

	/**
	 * This method gets the raw action list from
	 * {@link org.hammerhead226.sharkmacro.Parser Parser} and transforms it into an
	 * {@link ActionList} instance.
	 * <p>
	 * {@link org.hammerhead226.sharkmacro.Parser#cache Parser.cache} is checked
	 * first to see if the file has already been parsed, and returns a clone of the
	 * {@code ActionList} from the cache if the file exists in the cache. If the
	 * file does not exist in the cache, then the file is parsed and added to the
	 * cache.
	 * 
	 * @return a new {@code ActionList} instance
	 */
	public ActionList toObject() {
		List<String[]> actionListRaw = readFromFile();

		ArrayList<Action> list = new ArrayList<Action>(Constants.ACTIONRECORDER_LIST_DEFAULT_LENGTH);
		for (String[] s : actionListRaw) {
			list.add(new Action(s[0], Double.parseDouble(s[1]), Double.parseDouble(s[2])));
		}

		ActionList al = new ActionList(list);
		putInCache(al);

		return al;
	}

	/**
	 * This method generates a new filename to be used for saving a new file. For
	 * example, if the newest file in the storage directory is
	 * {@code prefix0003.csv}, the method will return {@code prefix0004.csv}.
	 * 
	 * @return a new complete filename in the prefix + number naming convention
	 */
	protected static String getNewFilename() {
		return Constants.ACTIONLIST_STORAGE_DIRECTORY + "/" + Constants.ACTIONLIST_DEFAULT_PREFIX
				+ String.format("%04d",
						findLatestNumberedFile(Constants.ACTIONLIST_STORAGE_DIRECTORY, Constants.ACTIONLIST_DEFAULT_PREFIX) + 1)
				+ ".csv";
	}

	/**
	 * This method finds the newest file named with prefix + number naming
	 * convention in the storage directory.
	 * 
	 * @return the complete filename of the latest (highest numbered) file in the
	 *         storage directory
	 */
	public static String getNewestFilename() {
		return Constants.ACTIONLIST_DEFAULT_PREFIX + String.format("%04d",
				findLatestNumberedFile(Constants.ACTIONLIST_STORAGE_DIRECTORY, Constants.ACTIONLIST_DEFAULT_PREFIX));
	}
}
