package sharkmacro.motionprofiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import sharkmacro.Constants;

public final class ProfileParser {

	public static void writeToFile(Profile profile) throws IOException {
		// Check if profile save directory exists, create it if not
		Path p = Paths.get(Constants.PROFILE_STORAGE_DIRECTORY);
		if (Files.notExists(p)) {
			try {
				Files.createDirectories(p);
				System.out.println("Created Directory: " + Constants.PROFILE_STORAGE_DIRECTORY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Write a Profile to a file
		CSVWriter writer;
		writer = new CSVWriter(new FileWriter(getFilename()), Constants.SEPARATOR, Constants.QUOTECHAR,
				Constants.ESCAPECHAR, Constants.NEWLINE);

		// Write the left and right profiles to an ArrayList
		ArrayList<String[]> profileToWrite = new ArrayList<String[]>(profile.length);
		String[][] left = profile.getLeftProfile_String();
		String[][] right = profile.getRightProfile_String();

		for (int i = 0; i < profile.length; i++) {
			String[] line = new String[] { left[i][0], left[i][1], right[i][0], right[i][1],
					Integer.toString(profile.dt, 10) };
			profileToWrite.add(line);
		}

		// Write the ArrayList to file
		writer.writeAll(profileToWrite);
		writer.close();
	}

	public static Profile readFromFile(String fileName) throws FileNotFoundException, IOException {
		CSVReader reader;
		List<String[]> profileRaw = new ArrayList<String[]>(0);
		try {
			reader = new CSVReader(
					new FileReader(formatFilename(fileName)));
			profileRaw = reader.readAll();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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

		return new Profile(left, right);
	}

	public static String getFilename() {
		String filename = Constants.PROFILE_DEFAULT_PREFIX;
		return Constants.PROFILE_STORAGE_DIRECTORY + "/" + filename
				+ String.format("%04d", findLatestNumberedFile(filename) + 1) + Constants.PROFILE_FILE_TYPE;
	}

	public static String getNewestFileAbsolutePath() {
		return Constants.PROFILE_STORAGE_DIRECTORY + "/" + Constants.PROFILE_DEFAULT_PREFIX
				+ String.format("%04d", findLatestNumberedFile(Constants.PROFILE_DEFAULT_PREFIX))
				+ Constants.PROFILE_FILE_TYPE;
	}

	public static String getNewestFilename() {
		return Constants.PROFILE_DEFAULT_PREFIX
				+ String.format("%04d", findLatestNumberedFile(Constants.PROFILE_DEFAULT_PREFIX));
	}

	public static int findLatestNumberedFile(String prefix) {
		ArrayList<String> str = getFilesWithPrefix(new File(Constants.PROFILE_STORAGE_DIRECTORY), prefix);
		int[] fileNumbers = toNumbers(str);
		// Find greatest number
		int greatest = Integer.MIN_VALUE;
		for (int i : fileNumbers) {
			if (i > greatest) {
				greatest = i;
			}
		}
		return greatest;
	}

	/**
	 * Return an ArrayList of the files starting with the given prefix
	 * 
	 * @param folder
	 * @param prefix
	 * @return
	 */
	public static ArrayList<String> getFilesWithPrefix(final File folder, String prefix) {
		ArrayList<String> str = new ArrayList<String>(folder.listFiles().length);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				getFilesWithPrefix(fileEntry, prefix);
			} else {
				if (fileEntry.getName().startsWith(prefix)) {
					str.add(fileEntry.getName());
				}
			}
		}
		return str;
	}

	/**
	 * Returns an array of integers representing the numbering of the profiles in
	 * given list.
	 * 
	 * @param fileNames
	 * @return an array of the profile numbers
	 */
	public static int[] toNumbers(ArrayList<String> fileNames) {
		if (fileNames.isEmpty()) {
			return new int[] { 0 };
		}
		// Isolate number part of filename
		for (int i = 0; i < fileNames.size(); i++) {
			fileNames.set(i, fileNames.get(i).substring(Constants.PROFILE_DEFAULT_PREFIX.length(),
					fileNames.get(i).length() - Constants.PROFILE_FILE_TYPE.length()));
		}

		int[] nums = new int[fileNames.size()];
		for (int i = 0; i < nums.length; i++) {
			nums[i] = Integer.parseInt(fileNames.get(i), 10);
		}
		return nums;
	}

	public static void deleteAllProfiles() throws FileNotFoundException {
		try {
			delete(new File(Constants.PROFILE_STORAGE_DIRECTORY));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void delete(File f) throws FileNotFoundException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				delete(c);
			}
		}
		if (!f.delete())
			try {
				throw new FileNotFoundException("Failed to delete file: " + f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	}
	
	private static String formatFilename(String name) {
		if (!name.endsWith(Constants.PROFILE_FILE_TYPE)) {
			name += Constants.PROFILE_FILE_TYPE;
		}
		return Constants.PROFILE_STORAGE_DIRECTORY + "/" + name;
	}

}
