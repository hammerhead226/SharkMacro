package org.hammerhead226.sharkmacro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Class to read and write csv files. This class is extended by two classes,
 * {@link org.hammerhead226.sharkmacro.actions.ActionListParser
 * ActionListParser} and
 * {@link org.hammerhead226.sharkmacro.motionprofiles.ProfileParser
 * ProfileParser}, which call this class's read and write methods and add to
 * {@link #cache}.
 * 
 * @author Alec Minchington
 * @see org.hammerhead226.sharkmacro.actions.ActionListParser ActionListParser
 * @see org.hammerhead226.sharkmacro.motionprofiles.ProfileParser ProfileParser
 */
public abstract class Parser {

	/**
	 * Directory to read or write files to.
	 */
	private final String directory;

	/**
	 * Prefix to name or search for files with.
	 */
	private final String prefix;

	/**
	 * Filename to read from or write a new file with.
	 */
	private final String filename;

	/**
	 * HashMap object representing a cache of all previously loaded
	 * {@link org.hammerhead226.sharkmacro.motionprofiles.Profile Profiles} and
	 * {@link org.hammerhead226.sharkmacro.actions.ActionList ActionLists}.
	 */
	private static HashMap<String, Object> cache = new HashMap<String, Object>();

	/**
	 * Constructs a new {@link Parser} object. {@link #filename} will be set to the
	 * value of {@link #getNewFilename()}.
	 * 
	 * @param directory
	 *            the directory to read or write files to
	 * @param prefix
	 *            the prefix to name new files or read existing files with
	 */
	public Parser(String directory, String prefix) {
		this.directory = directory;
		this.prefix = prefix;
		this.filename = getNewFilename();
	}

	/**
	 * Constructs a new {@link Parser} object.
	 * 
	 * @param directory
	 *            the directory to read or write files to
	 * @param prefix
	 *            the prefix to name new files or read existing files with
	 * @param filename
	 *            name of the file to read or write a new file with
	 */
	public Parser(String directory, String prefix, String filename) {
		this.directory = directory;
		this.prefix = prefix;

		if (!filename.endsWith(".csv")) {
			filename += ".csv";
		}
		this.filename = directory + "/" + filename;
	}

	/**
	 * Gets the {@link java.lang.Object Object} corresponding to the key equal to
	 * the filename.
	 * 
	 * @return an {@code Object} from the cache
	 */
	protected Object getFromCache() {
		return cache.get(filename);
	}

	/**
	 * Adds a key/value pair to the cache with the filename as the key and
	 * {@code obj} as the value.
	 * 
	 * @param obj
	 *            object to put in the cache
	 */
	protected void putInCache(Object obj) {
		cache.put(filename, obj);
	}

	/**
	 * Checks the cache for a a key equal to {@link #filename}.
	 * 
	 * @return {@code true} if the cache contains the filename, {@code false} if not
	 */
	protected boolean inCache() {
		return cache.containsKey(filename);
	}

	/**
	 * Given values to write, this method writes those values to a csv file.
	 * 
	 * @param data
	 *            the lines of the file to be written
	 * @return {@code true} if the file was written successfully, {@code false} if
	 *         the storage directory wasn't able to be created or the file wasn't
	 *         able to be written.
	 */
	protected boolean writeToFile(ArrayList<String[]> data) {
		Path p = Paths.get(directory);
		if (Files.notExists(p)) {
			try {
				Files.createDirectories(p);
				System.out.println("Created Directory: " + directory);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		CSVWriter writer;

		try {
			writer = new CSVWriter(new FileWriter(filename), Constants.SEPARATOR, Constants.QUOTECHAR,
					Constants.ESCAPECHAR, Constants.NEWLINE);
			writer.writeAll(data);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Reads the contents of a csv file and returns the result as a
	 * {@link java.util.List List} with each String array containing the values in
	 * each row of the file.
	 * 
	 * @return a list representing the contents of the read file
	 */
	protected List<String[]> readFromFile() {
		CSVReader reader;
		List<String[]> rawFile = new ArrayList<String[]>(0);
		try {
			reader = new CSVReader(new FileReader(filename));
			rawFile = reader.readAll();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return rawFile;
	}

	/**
	 * This method generates a new filename to be used for saving a new file. For
	 * example, if the newest file in the storage directory is
	 * {@code prefix0003.csv}, the method will return {@code prefix0004.csv}.
	 * 
	 * @return a new complete filename in the prefix + number naming convention
	 */
	public String getNewFilename() {
		return directory + "/" + filename + String.format("%04d", findLatestNumberedFile(prefix) + 1) + ".csv";
	}

	/**
	 * This method finds the newest file named with prefix + number naming
	 * convention in the storage directory.
	 * 
	 * @return the complete filename of the latest (highest numbered) file in the
	 *         storage directory
	 */
	public String getNewestFilename() {
		return prefix + String.format("%04d", findLatestNumberedFile(prefix));
	}

	/**
	 * Given a prefix, this method returns the number of the highest numbered file
	 * in the directory. If there are no files named with the prefix in the
	 * directory, the method returns {@code 0}.
	 * 
	 * @param prefix
	 *            the string that the file must start with
	 * @return the number of the newest (highest numbered) file
	 */
	private int findLatestNumberedFile(String prefix) {
		ArrayList<String> str = getFilesWithPrefix();
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
	 * This method returns a list of the files in the storage directory that start
	 * with {@link #prefix}.
	 * 
	 * @return an {@link java.util.ArrayList ArrayList} of type String of the files
	 *         starting with the given prefix
	 */
	private ArrayList<String> getFilesWithPrefix() {
		final File folder = new File(directory);
		ArrayList<String> str = new ArrayList<String>(folder.listFiles().length);
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().startsWith(prefix)) {
				str.add(fileEntry.getName());
			}
		}
		return str;
	}

	/**
	 * This method takes a list of {@link Parser}-generated filenames and enumerates
	 * them, returning an integer array of the files' numbers. For example, if the
	 * directory contains {@code prefix0025.csv}, {@code prefix0026.csv}, and
	 * {@code prefix0027.csv}, this method would return {@code [ 25, 26, 27 ]}.
	 * 
	 * @param filenames
	 *            list of numbered files
	 * @return an integer array of the file numbers
	 */
	private int[] toNumbers(ArrayList<String> filenames) {
		if (filenames.isEmpty()) {
			return new int[] { 0 };
		}
		// Isolate number part of filename
		for (int i = 0; i < filenames.size(); i++) {
			filenames.set(i, filenames.get(i).substring(prefix.length(), filenames.get(i).length() - 4));
		}

		int[] nums = new int[filenames.size()];
		for (int i = 0; i < nums.length; i++) {
			nums[i] = Integer.parseInt(filenames.get(i), 10);
		}
		return nums;
	}

}
