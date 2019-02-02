package org.hammerhead226.sharkmacro.motionprofiles;

import java.util.ArrayList;

import org.hammerhead226.sharkmacro.Constants;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

/**
 * Wrapper class for all {@link #TalonSRX} that are to be used in motion
 * profiling mode.
 * 
 * @author Nidhi Jaison
 * 
 */
public class MotionProfileTalonSRX extends TalonSRX {
    /**
     * Profile to be used with this {@link #MotionProfileTalonSRX}
     */
    private double[][] profile;

    /**
     * PID slot used on this {@link #MotionProfileTalonSRX}
     */
    private int pidSlot;

    /**
     * List of recorded encoder positions
     */
    public ArrayList<Double> position = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

    /**
     * List of recorded Voltage or Velocity values
     */
    public ArrayList<Double> feedForwardValues = new ArrayList<Double>(Constants.PROFILERECORDER_LIST_DEFAULT_LENGTH);

    /**
     * Constructor for all {@link #TalonSRX} to be used with motion profiling
     * 
     * @param port    Device ID
     * @param pidSlot PID Slot to be used with this Talon
     */
    public MotionProfileTalonSRX(int port, int pidSlot) {
        super(port);
        this.pidSlot = pidSlot;
    }

    /**
     * Method to set the positions and feed forward values together
     * 
     * @param profile 2D array containing talon encoder and feed forward values
     */
    public void setProfile_Double(double[][] profile) {
        this.profile = profile;
    }

    /**
     * @return Most recently recorded profile
     */
    public double[][] getProfile_Double() {
        return profile;
    }

    /**
     * Sets the profile from a 2D String Array to a Double
     * 
     * @param profile
     */
    public void setProfile_String(String[][] profile) {
        this.profile = Profile.toDoubleArray(profile);
    }

    /**
     * @return Motion profile in a 2D array of type String
     */
    public String[][] getProfile_String() {
        return Profile.toStringArray(profile);
    }

    /**
     * @return Selected PID slot for this talon
     */
    public int getPidSlot() {
        return pidSlot;
    }

}