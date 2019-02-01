package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.Arrays;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Class representation of a motion profile. Formatted to work with Talon SRX
 * motion profiling mode.
 * 
 * @author Alec Minchington, Nidhi Jaison
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
	 * The {@link ProfileHandler} that will handle the motion profiles.
	 */
	private ProfileHandler handler;

	/**
	 * The PID Slots used on each {@link TalonSRX}
	 */
	private int[] pidSlotIdxs;

	/**
	 * An array of {@link MotionProfileTalonSRX} to be recorded}
	 */
	private MotionProfileTalonSRX[] talons;

	/**
	 * An array of Profiles for each {@link TalonSRX}
	 */
	private double[][][] profiles;

	/**
	 * Constructs a new {@link Profile} object.
	 * 
	 * @param talons
	 *            An array of MotionProfileTalons to execute profiles with. 
	 */
	public Profile(MotionProfileTalonSRX... talons) {
		for (int i = 0; i < talons.length; i++) {
			profiles[i] = talons[i].getProfile_Double();
			this.talons[i] = talons[i];
			pidSlotIdxs[i] = talons[i].getPidSlot();
		}
		length = talons[0].getProfile_Double().length;
		dt = (int) talons[0].getProfile_Double()[0][2];

		handler = new ProfileHandler(profiles, this.talons, pidSlotIdxs);
	}

	/**
	 * Constructs a new {@link Profile} object without execution Talons or PID slot
	 * indexes. Only to be used in {@link Recording}.
	 * 
	 * @param profiles
	 *            An array of motions profiles of all the Talons
	 * 
	 */
	public Profile(double[][]...profiles) {
		this.profiles = profiles;
		this.length = this.profiles[0].length;
		this.dt = (int) this.profiles[0][0][2];
	}

	/**
	 * Execute a motion profile. This is done by passing each {@link #profiles}
	 * to new {@link ProfileHandler}s and calling their
	 * {@link ProfileHandler#execute() execute()} method.
	 * 
	 */
	public void execute() {
		boolean runProfile = true;
		for(double[][] profile : profiles) {
			if(profile.length == 0) {
				runProfile = false;
			}
		}
		if (runProfile) {
			handler.execute();
		} else {
			DriverStation.getInstance();
			DriverStation.reportError("Tried to run empty profile!", false);
		}
	}

	/**
	 * Safely stops motion profile execution. This method should be called if the
	 * {@link edu.wpi.first.wpilibj.commands.Command Command} controlling the motion
	 * profile's execution is interrupted.
	 */
	public void onInterrupt() {
		if (handler != null) {
			handler.onInterrupt();
		} else {
			DriverStation.getInstance();
			DriverStation.reportWarning("No instance of ProfileHandler to interrupt!", false);
		}
	}

	/**
	 * Returns whether this {@link Profile} is finished executing.
	 * 
	 * @return {@code true} if both {@link MotionProfileHandler}s are finished
	 *         executing their respective profiles, {@code false} otherwise
	 */
	public boolean isFinished() {
		return handler.isFinished();
	}

	/**
	 * Returns this {@link Profile}'s {@link #leftProfile} property.
	 * 
	 * @return the left side motion profile
	 */
	public double[][] getProfile_Double(int port){
		double[][] profile = null;
		for(MotionProfileTalonSRX talon : talons) {
			if(talon.getDeviceID() == port) {
				profile = talon.getProfile_Double();
			}
		}
		DriverStation.reportWarning("Cannot get empty profile", false);
		return profile;
	}

	/**
	 * Returns the result of {@link #toStringArray(double[][])} on
	 * {@link #rightProfile}.
	 * 
	 * @return the {@link java.lang.String String} array representation of
	 *         {@link #rightProfile}
	 */
	public String[][] getProfile_String(int port){
		return toStringArray(getProfile_Double(port));
	}
	
	public TalonSRX[] getTalons() {
		return talons;
	}
	
	public String[][][] getProfiles_String(){
		String[][][] strProfiles = new String[profiles.length][][];
		int i = 0;
		for (double[][] profile : profiles) {
			strProfiles[i++] = toStringArray(profile);
		}
		return strProfiles;
	}

	/**
	 * Converts a 2D {@link java.lang.Double Double} array to a 2D array of type
	 * {@link java.lang.String String}.
	 * 
	 * @param arr
	 *            the array to convert
	 * @return the converted array
	 */
	protected static String[][] toStringArray(double[][] arr) {
		if (arr.length == 0) {
			return new String[0][0];
		}
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
	protected static double[][] toDoubleArray(String[][] arr) {
		if (arr.length == 0) {
			return new double[0][0];
		}
		double[][] dbl = new double[arr.length][arr[0].length];
		int i = 0;
		for (String[] s : arr) {
			dbl[i++] = Arrays.stream(s).mapToDouble(Double::parseDouble).toArray();
		}
		return dbl;
	}

}
