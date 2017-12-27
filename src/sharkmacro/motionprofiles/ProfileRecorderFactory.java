package sharkmacro.motionprofiles;

import sharkmacro.SharkMacroNotInitialized;

public class ProfileRecorderFactory {

	public static ProfileRecorder createProfileRecorder() {
		ProfileRecorder r;
		try {
			r = new ProfileRecorder();
		} catch (SharkMacroNotInitialized e) {
			r = null;
			e.printStackTrace();
		}
		return r;
	}
}
