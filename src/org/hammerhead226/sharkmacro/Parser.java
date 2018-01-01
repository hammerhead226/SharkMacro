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

public abstract class Parser {

	String directory;
	String prefix;
	final String filename;

	private static HashMap<String, Object> cache = new HashMap<String, Object>();

	public Parser(String directory, String prefix, String filename) {
		this.directory = directory;
		this.prefix = prefix;

		if (!filename.endsWith(".csv")) {
			filename += ".csv";
		}
		this.filename = directory + "/" + filename;
	}

	public Parser(String directory, String prefix) {
		this.directory = directory;
		this.prefix = prefix;
		this.filename = getNewFilename();
	}

	protected Object getFromCache() {
		return cache.get(filename);
	}

	protected void putInCache(Object obj) {
		cache.put(filename, obj);
	}

	protected boolean inCache() {
		return cache.containsKey(filename);
	}

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

	public String getNewFilename() {
		return directory + "/" + filename + String.format("%04d", findLatestNumberedFile(prefix) + 1) + ".csv";
	}

	public String getNewestFilename() {
		return prefix + String.format("%04d", findLatestNumberedFile(prefix));
	}

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
	 * @return an ArrayList of type String of the files starting with the given
	 *         prefix
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
	 * @param filenames
	 *            - list of numbered files
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
