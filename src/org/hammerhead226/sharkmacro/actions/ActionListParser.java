package org.hammerhead226.sharkmacro.actions;

import java.util.ArrayList;
import java.util.List;

import org.hammerhead226.sharkmacro.Constants;
import org.hammerhead226.sharkmacro.Parser;

public class ActionListParser extends Parser {

	public ActionListParser() {
		super(Constants.ACTIONLIST_STORAGE_DIRECTORY, Constants.ACTIONLIST_DEFAULT_PREFIX);
	}

	public ActionListParser(String filename) {
		super(Constants.ACTIONLIST_STORAGE_DIRECTORY, Constants.ACTIONLIST_DEFAULT_PREFIX, filename);
	}

	public boolean writeToFile(ActionList al) {
		if (al.getSize() == 0) {
			System.out.println("Tried to write empty ActionList");
			return false;
		}

		ArrayList<String[]> profileToWrite = new ArrayList<String[]>(al.getSize());
		for (Action a : al) {
			profileToWrite.add(a.toStringArray());
		}

		return super.writeToFile(profileToWrite);
	}

	public ActionList toObject() {
		if (inCache()) {
			return (ActionList) ((ActionList) getFromCache()).clone();
		}

		List<String[]> actionListRaw = readFromFile();

		ArrayList<Action> list = new ArrayList<Action>(Constants.ACTIONRECORDER_LIST_DEFAULT_LENGTH);
		for (String[] s : actionListRaw) {
			list.add(new Action(s[0], Double.parseDouble(s[1]), Double.parseDouble(s[2])));
		}

		ActionList al = new ActionList(list);
		putInCache(al);

		return al;
	}

}
