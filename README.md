# SharkMacro

## Motion profiles

### Before you start

* In order to play back recorded motion profiles, the Talons' PID must be tuned for your drivetrain.

* Configure left and right talon settings
    * SharkMacro uses 10ms trajectory points by default so no extra trajectory time must be added
    ```java
    talon.configMotionProfileTrajectoryPeriod(0, 0);
    ```
    * Correct feedback sensor must be set
    ```java
    talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
    ```
    * Sensor values will not be updated fast enough for the recorder unless the feedback status frame is set manually
    ```java
    talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 5, 0);
    ```
---

* Record a profile
    ```java
    ProfileRecorder recorder = new ProfileReorder(leftTalon, rightTalon);
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
    Profile p = parser.toObject(leftTalon, rightTalon);
    p.execute(leftPIDSlot, rightPIDSlot);
    ```

## Action lists

SharkMacro's action recording framework piggybacks off of the WPILib Command system, meaning teams won't have to learn a new format for robot commands.

All robot commands that are going to be recorded should extend `SharkMacro.RecordableCommand` instead of `Command` and should call `super.initialize()` in `initialize()`, `super.end()` in `end()`, and `super.interrupted()` in `interrupted()`.

Example command class:

```java
public class ExampleCommand extends RecordableCommand {

	public Drive() {
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
    ActionList al = parser.toObject(leftTalon, rightTalon);
    al.execute();
    ```

# Other

## Save directories
SharkMacro saves profiles directly to the roboRIO internal storage
* Motion profiles: `\home\lvuser\profiles`
* Action lists: `\home\lvuser\actionlists`

You can access the saved files via the roboRIO web dashboard's file system browser, but it is recommended that you use an external FTP client such as [WinSCP](https://winscp.net/eng/download.php).

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
```
profile0001.csv
profile0002.csv
profile0003.csv
```

### Using the SharkMacro naming convention

There are a couple different methods that can be used to access/generate SharkMacro-named files.

* `ProfileParser.getNewFilename()` & `ActionListParser.getNewFilename()` - Generates a new filename in the SharkMacro convention by looking through the directory for already-existing files and returning a filename with a number equal to the current greatest number plus one. For example, calling `ProfileParser.getNewFilename()` with the motion profile save directory containing
    ```
    profile0001.csv
    profile0002.csv
    profile0003.csv
    ```
    would return `profile0004`.
---
* `ProfileParser.getNewestFilename()` & `ActionListParser.getNewestFilename()` - Get the filename of the current newest (highest numbered) file in the save directory. For example, calling `ProfileParser.getNewestFilename()` with the motion profile save directory containing
```
    profile0001.csv
    profile0002.csv
    profile0003.csv
```
would return `profile0003`.
