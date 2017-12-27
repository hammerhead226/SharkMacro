package sharkmacro;

import com.ctre.CANTalon;

public class SharkMacro {

	public static CANTalon leftTalon;
	public static CANTalon rightTalon;

	public static int leftGainsProfile;
	public static int rightGainsProfile;

	private static boolean initialized = false;

	public static void initialize(CANTalon left, int leftGainsProfile, CANTalon right, int rightGainsProfile) {
		setTalons(left, right);
		setGainsProfiles(leftGainsProfile, rightGainsProfile);
		initialized = true;
	}

	private static void setTalons(CANTalon left, CANTalon right) {
		SharkMacro.leftTalon = left;
		SharkMacro.rightTalon = right;
	}

	private static void setGainsProfiles(int leftGainsProfile, int rightGainsProfile) {
		SharkMacro.leftGainsProfile = leftGainsProfile;
		SharkMacro.rightGainsProfile = rightGainsProfile;
	}

	public static boolean isInitialized() {
		return SharkMacro.initialized;
	}
}
