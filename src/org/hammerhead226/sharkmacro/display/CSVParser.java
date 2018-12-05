package org.hammerhead226.sharkmacro.display;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class CSVParser {

	private static ArrayList<Double> loadProfile(String fileLocation) {

		ArrayList<Double> temp = new ArrayList<Double>(45000);
		String InputLine;
		Scanner scanner = null;
		String tempString;

		try {
			scanner = new Scanner(new BufferedReader(new FileReader(fileLocation)));

			while (scanner.hasNextLine()) {
				InputLine = scanner.nextLine();
				String[] InArray = InputLine.split(",");
				for (int i = 0; i < InArray.length; i++) {
					tempString = InArray[i].replaceAll("\"", "");
					temp.add(Double.parseDouble(tempString));
				}

			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return temp;
	}

	public static ArrayList<Double> readLeftPosition(String fileLocation) {
		ArrayList<Double> profile = loadProfile(fileLocation);
		ArrayList<Double> position = new ArrayList<Double>();

		for (int i = 0; i < (profile.size() / 5); i++) {
			position.add( profile.get(5 * i));
		}
		
		return position;
	}

	public static ArrayList<Double> readRightPosition(String fileLocation) {
		ArrayList<Double> profile = loadProfile(fileLocation);
		ArrayList<Double> position = new ArrayList<Double>();

		for (int i = 0; i < (profile.size() / 5); i++) {
			position.add(profile.get(5 * i + 2));
		}
		
		return position;
	}
}
