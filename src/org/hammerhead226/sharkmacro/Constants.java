package org.hammerhead226.sharkmacro;

public final class Constants {
	
	public static final char SEPARATOR = ',';
	public static final char QUOTECHAR = '"';
	public static final char ESCAPECHAR = '\\';
	public static final String NEWLINE = "\n";
	
	// Motion profiling

	public static final double DT_MS = 10.0;
	public static final double DT_SECONDS = DT_MS / 1000.0;
	public static final int PROFILERECORDER_LIST_DEFAULT_LENGTH = 2500;

	public static final String PROFILE_DEFAULT_PREFIX = "profile";
	public static final String PROFILE_STORAGE_DIRECTORY = "/home/lvuser/profiles";
	public static final String SMARTDASHBOARD_PROFILE_NAME_KEY = "profilename";

	public static final int MINIMUM_POINTS_IN_TALON = 5;

	// Actions
	
	public static final int ACTIONRECORDER_LIST_DEFAULT_LENGTH = 20;
	
	public static final String ACTIONLIST_DEFAULT_PREFIX = "actionlist";
	public static final String ACTIONLIST_STORAGE_DIRECTORY = "/home/lvuser/actionlists";
	
}