# SharkMacro

## Dependencies

1. OpenCSV (tested on 4.1)
2. Apache Commons Lang (tested on 3.7)
3. WPILibJ
4. Cross The Road Electronics Phoenix Lib(5.8.1)

## Installation (for FRC Eclipse)

1. Download the latest release of SharkMacro from [here](https://github.com/hammerhead226/SharkMacro/releases).
2. Download Apache Commons Lang from [here](http://apache.spinellicreations.com//commons/lang/binaries/commons-lang3-3.7-bin.zip).
3. Download OpenCSV from [here](https://sourceforge.net/projects/opencsv/files/latest/download).
4. Repeat the process below for each downloaded file:
    1. Copy the downloaded file to `C:\Users\<YourUser>\wpilib\user\java\lib`.
    2. Right click on your robot code project in Eclipse > Build Path > Add External Archives...
    3. Select the file just copied to `lib`.


## Motion profiles

### Before you start

* In order to play back recorded motion profiles, the Talons' PID must be tuned for your drivetrain.

* Configure left and right talon settings

  * SharkMacro uses 10ms trajectory points by default so no extra trajectory time must be added:

    `talon.configMotionProfileTrajectoryPeriod(0, 0);`
        
  * Correct feedback sensor must be set:

    `talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);`

  * Sensor values will not be updated fast enough for the recorder unless the feedback status frame is set manually:
        
    `talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, 0);`
---

* Record a profile
    ```java
    ProfileRecorder recorder = new ProfileRecorder(leftTalon, rightTalon, RecordingType.VOLTAGE/VELOCITY);
    recorder.start();
    ```
* Save a profile to file
    ```java
    Profile p = recorder.stop().toProfile();
    ProfileParser parser = new ProfileParser(filename);
    parser.writeToFile(p);
    ```
* Load and play a profile from file
    ```java
    ProfileParser parser = new ProfileParser(filenameToLoad);
    Profile p = parser.toObject(leftTalon, rightTalon, leftPIDSlotIdx, rightPIDSlotIdx);
    p.execute();
    ```
Teams can use a Command to toggle recording:

```java
public class ToggleProfileRecording extends InstantCommand {

    public ToggleProfileRecording() {
        super();
        requires(Robot.driveTrain);
    }

    protected void initialize() {
    	Robot.driveTrain.toggleRecording();
    }

}
```

In this example the logic for toggling the motion profile recording is contained in the DriveTrain subsystem.

```java
public class DriveTrain extends Subsystem {

    private TalonSRX leftTalon = new TalonSRX(RobotMap.DT_FL_MOTOR);
    private TalonSRX rightTalon = new TalonSRX(RobotMap.DT_FR_MOTOR);

    private ProfileRecorder r = new ProfileRecorder(leftTalon, rightTalon, RecordingType.VOLTAGE);

    public void toggleRecording() {
        if (r.isRecording()) {
            ProfileParser p = new ProfileParser(ProfileParser.getNewFilename());
            p.writeToFile(r.stop().toProfile(leftPIDSlotIdx, rightPIDSlotIdx));
        } else {
            // Zero both encoders before recording
            leftTalon.setSelectedSensorPosition(0, 0, 0);
            rightTalon.setSelectedSensorPosition(0, 0, 0);
            r.start();
        }
    }
}
```

## Action lists

SharkMacro's action recording framework piggybacks off of the WPILib Command system, meaning teams won't have to learn a new format for robot commands.

All robot commands that are going to be recorded should extend `SharkMacro.RecordableCommand` instead of `Command` and should call `super.initialize()` in `initialize()`, `super.end()` in `end()`, and `super.interrupted()` in `interrupted()`.

Example command class:

```java
public class ExampleCommand extends RecordableCommand {

	public ExampleCommand() {
	}

	protected void initialize() {
	    super.initialize();
            // Your code here
	}

	protected void execute() {
	}

	protected boolean isFinished() {
	    return false;
	}

	protected void end() {
	    super.end();
            // Your code here
	}

	protected void interrupted() {
        super.interrupted();
            // Your code here
	}
}
```

* Record a list of actions
    ```java
    ActionRecorder.start();
    // Run your commands as usual...
    ```
* Save a list of actions to file
    ```java
    ActionListParser parser = new ActionListParser(filename);
    parser.writeToFile(ActionRecorder.stop());
    ```
* Load and play a list of actions from file
    ```java
    ActionListParser parser = new ActionListParser(filenameToLoad);
    ActionList al = parser.toObject();
    al.execute();
    ```
Teams can use a Command to toggle recording:

```java
public class ToggleActionRecording extends InstantCommand {

    public ToggleActionRecording() {
        super();
    }

    protected void initialize() {
        Robot.driveTrain.toggleActionRecording();
    }

}
```

In this example the logic for toggling the action recording is contained in the DriveTrain subsystem.

```java
public class DriveTrain extends Subsystem {

    public void toggleActionRecording() {
        if (ActionRecorder.isRecording()) {
            ActionListParser al = new ActionListParser(ActionListParser.getNewFilename());
            al.writeToFile(ActionRecorder.stop());
        } else {
            ActionRecorder.start();
        }
    }
}
```

## Other

## Save directories

SharkMacro saves profiles directly to the roboRIO internal storage

* Motion profiles: `/home/lvuser/profiles`

* Action lists: `/home/lvuser/actionlists`

You can access the saved files via the roboRIO web dashboard's file system browser, but it is recommended that you use an external SFTP client such as [WinSCP](https://winscp.net/eng/download.php).

## File naming convention
The default naming convention for files saved by SharkMacro is:
```
prefix####.csv
```
Where prefix is the specific keyword for each type of recorded file.

* Motion profiles: `profile`

* Action lists: `actionlist`

And `####` is the number of the saved file, starting with `0001`.

So, three motion profiles saved with the SharkMacro naming convention would look like:

* `profile0001.csv`
* `profile0002.csv`
* `profile0003.csv`

### Using the SharkMacro naming convention

There are a couple different methods that can be used to access/generate SharkMacro-named files.

* `ProfileParser.getNewFilename()` & `ActionListParser.getNewFilename()` - Generates a new filename in the SharkMacro convention by looking through the directory for already-existing files and returning a filename with a number equal to the current greatest number plus one. For example, calling `ProfileParser.getNewFilename()` with the motion profile save directory containing

  * `profile0001.csv`
  * `profile0002.csv`
  * `profile0003.csv`

    would return `profile0004`.
---

* `ProfileParser.getNewestFilename()` & `ActionListParser.getNewestFilename()` - Get the filename of the current newest (highest numbered) file in the save directory. For example, calling `ProfileParser.getNewestFilename()` with the motion profile save directory containing

  * `profile0001.csv`
  * `profile0002.csv`
  * `profile0003.csv`

would return `profile0003`.

## Things to look out for

* Be sure to have your code call `actionList.execute()` and `profile.execute()` at the same time. If not called close to simultaneously, the robot's movement and its actions will be out of sync.

* Be sure to set PID gains in the talons that will be playing back your recorded profile. Not much, if any, PID tuning is required with motion profiling. I found that a P gain of between 0.5 and 1.5 on both talons and I, D and F gains of 0 worked well. (Tested on 4-CIM West Coast drivetrain with a total robot weight of ~100 lbs.)