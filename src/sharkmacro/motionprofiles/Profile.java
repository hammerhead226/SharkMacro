package sharkmacro.motionprofiles;

import java.util.Arrays;

import com.ctre.CANTalon;

/**
 * Class representation of a motion profile. Formatted to work with Talon SRX
 * motion profiling mode.
 * 
 * @author Alec Minchington
 *
 */
public class Profile {

	public final int length;
	public final int dt;

	private ProfileHandler left;
	private ProfileHandler right;
	
	private CANTalon leftTalon;
	private CANTalon rightTalon;

	private final double[][] leftProfile;
	private final double[][] rightProfile;

	public Profile(double[][] leftProfile, double[][] rightProfile, CANTalon leftTalon, CANTalon rightTalon) {
		this.leftProfile = leftProfile;
		this.rightProfile = rightProfile;
		this.leftTalon = leftTalon;
		this.rightTalon = rightTalon;
		this.length = this.leftProfile.length;
		this.dt = (int) this.leftProfile[0][2];
	}

	public Profile(String[][] leftProfile, String[][] rightProfile, CANTalon leftTalon, CANTalon rightTalon) {
		this.leftProfile = toDoubleArray(leftProfile);
		this.rightProfile = toDoubleArray(rightProfile);
		this.leftTalon = leftTalon;
		this.rightTalon = rightTalon;
		this.length = this.leftProfile.length;
		this.dt = (int) this.leftProfile[0][2];
	}

	public void execute(int leftGainsProfile, int rightGainsProfile) {
		left = new ProfileHandler(leftProfile, leftTalon, leftGainsProfile);
		right = new ProfileHandler(rightProfile, rightTalon, rightGainsProfile);
		left.execute();
		right.execute();
	}

	public void onInterrupt() {
		left.onInterrupt();
		right.onInterrupt();
	}
	
	public boolean isFinished() {
		return left.isFinished() && right.isFinished();
	}

	public double[][] getLeftProfile_Double() {
		return this.leftProfile;
	}

	public double[][] getRightProfile_Double() {
		return this.rightProfile;
	}

	public String[][] getLeftProfile_String() {
		return toStringArray(this.leftProfile);
	}

	public String[][] getRightProfile_String() {
		return toStringArray(this.rightProfile);
	}

	private static String[][] toStringArray(double[][] arr) {
		String[][] str = new String[arr.length][arr[0].length];
		int i = 0;
		for (double[] d : arr) {
			str[i++] = Arrays.toString(d).replace("[", "").replace("]", "").replace(" ", "").split(",");
		}
		return str;
	}

	private static double[][] toDoubleArray(String[][] arr) {
		double[][] dbl = new double[arr.length][arr[0].length];
		int i = 0;
		for (String[] s : arr) {
			dbl[i++] = Arrays.stream(s).mapToDouble(Double::parseDouble).toArray();
		}
		return dbl;
	}

}
