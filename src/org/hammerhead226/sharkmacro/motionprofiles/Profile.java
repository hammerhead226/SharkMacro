package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.Arrays;

import com.ctre.CANTalon;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Class representation of a motion profile. Formatted to work with Talon SRX
 * motion profiling mode.
 * 
 * @author Alec Minchington
 *
 */
public class Profile {

	/**
	 * The number of points in both {@link #leftProfile} and {@link #rightProfile}.
	 */
	public final int length;

	/**
	 * The amount of time, in milliseconds, that each motion profile point will be
	 * held in the Talon.
	 */
	public final int dt;

	/**
	 * The {@link ProfileHandler} that will handle {@link #leftProfile}.
	 */
	private ProfileHandler left;

	/**
	 * The {@link ProfileHandler} that will handle {@link #rightProfile}.
	 */
	private ProfileHandler right;

	/**
	 * the Talon to execute the left profile with
	 */
	private TalonSRX leftTalon;

	/**
	 * the Talon to execute the right profile with
	 */
	private TalonSRX rightTalon;

	/**
	 * The left motion profile.
	 */
	private double[][] leftProfile;

	/**
	 * The right motion profile.
	 */
	private double[][] rightProfile;

	/**
	 * Constructs a new {@link Profile} object.
	 * 
	 * @param leftProfile
	 *            the left motion profile
	 * @param rightProfile
	 *            the right motion profile
	 * @param leftTalon
	 *            the Talon to execute the left profile with
	 * @param rightTalon
	 *            the Talon to execute the right profile with
	 */
	public Profile(double[][] leftProfile, double[][] rightProfile, TalonSRX leftTalon, TalonSRX rightTalon) {
		this.leftProfile = leftProfile;
		this.rightProfile = rightProfile;
		this.leftTalon = leftTalon;
		this.rightTalon = rightTalon;
		this.length = this.leftProfile.length;
		this.dt = (int) this.leftProfile[0][2];
	}

	/**
	 * Constructs a new {@link Profile} object. Passed {@link java.lang.String
	 * String} arrays are automatically converted to and stored as
	 * {@link java.lang.Double Double} arrays.
	 * 
	 * @param leftProfile
	 *            the left motion profile
	 * @param rightProfile
	 *            the right motion profile
	 * @param leftTalon
	 *            the Talon to execute the left profile with
	 * @param rightTalon
	 *            the Talon to execute the right profile with
	 */
	public Profile(String[][] leftProfile, String[][] rightProfile, TalonSRX leftTalon, TalonSRX rightTalon) {
		this.leftProfile = toDoubleArray(leftProfile);
		this.rightProfile = toDoubleArray(rightProfile);
		this.leftTalon = leftTalon;
		this.rightTalon = rightTalon;
		this.length = this.leftProfile.length;
		this.dt = (int) this.leftProfile[0][2];
	}

	/**
	 * Execute a motion profile. This is done by passing {@link #leftProfile} and
	 * {@link #rightProfile} to new {@link ProfileHandler}s and calling their
	 * {@link ProfileHandler#execute() execute()} method.
	 * 
	 * @param leftGainsProfile
	 *            the PID gains profile to use to execute the left motion profile
	 * @param rightGainsProfile
	 *            the PID gains profile to use to execute the right motion profile
	 */
	public void execute(int leftGainsProfile, int rightGainsProfile) {
		left = new ProfileHandler(leftProfile, leftTalon, leftGainsProfile);
		right = new ProfileHandler(rightProfile, rightTalon, rightGainsProfile);
		left.execute();
		right.execute();
	}

	/**
	 * Safely stops motion profile execution. This method should be called if the
	 * {@link edu.wpi.first.wpilibj.commands.Command Command} controlling the motion
	 * profile's execution is interrupted.
	 */
	public void onInterrupt() {
		left.onInterrupt();
		right.onInterrupt();
	}

	/**
	 * Returns whether this {@link Profile} is finished executing.
	 * 
	 * @return {@code true} if both {@link MotionProfileHandler}s are finished
	 *         executing their respective profiles, {@code false} otherwise
	 */
	public boolean isFinished() {
		return left.isFinished() && right.isFinished();
	}

	/**
	 * Returns this {@link Profile}'s {@link #leftProfile} property.
	 * 
	 * @return the left side motion profile
	 */
	public double[][] getLeftProfile_Double() {
		return this.leftProfile;
	}

	/**
	 * Returns this {@link Profile}'s {@link #rightProfile} property.
	 * 
	 * @return the right side motion profile
	 */
	public double[][] getRightProfile_Double() {
		return this.rightProfile;
	}

	/**
	 * Returns the result of {@link #toStringArray(double[][])} on
	 * {@link #leftProfile}.
	 * 
	 * @return the {@link java.lang.String String} array representation of
	 *         {@link #leftProfile}
	 */
	public String[][] getLeftProfile_String() {
		return toStringArray(this.leftProfile);
	}

	/**
	 * Returns the result of {@link #toStringArray(double[][])} on
	 * {@link #rightProfile}.
	 * 
	 * @return the {@link java.lang.String String} array representation of
	 *         {@link #rightProfile}
	 */
	public String[][] getRightProfile_String() {
		return toStringArray(this.rightProfile);
	}

	/**
	 * Converts a 2D {@link java.lang.Double Double} array to a 2D array of type
	 * {@link java.lang.String String}.
	 * 
	 * @param arr
	 *            the array to convert
	 * @return the converted array
	 */
	private static String[][] toStringArray(double[][] arr) {
		String[][] str = new String[arr.length][arr[0].length];
		int i = 0;
		for (double[] d : arr) {
			str[i++] = Arrays.toString(d).replace("[", "").replace("]", "").replace(" ", "").split(",");
		}
		return str;
	}

	/**
	 * Converts a 2D {@link java.lang.String String} array of numbers to a 2D array
	 * of type {@link java.lang.Double Double}.
	 * 
	 * @param arr
	 *            the array to convert
	 * @return the converted array
	 */
	private static double[][] toDoubleArray(String[][] arr) {
		double[][] dbl = new double[arr.length][arr[0].length];
		int i = 0;
		for (String[] s : arr) {
			dbl[i++] = Arrays.stream(s).mapToDouble(Double::parseDouble).toArray();
		}
		return dbl;
	}

}
