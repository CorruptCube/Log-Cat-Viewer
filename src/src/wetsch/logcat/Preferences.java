package wetsch.logcat;
/*
 * This class is for the app Preferences.
 * When this activity is started, it
 * will check to see if the devices is rooted.
 * The isDeviceRooted returns true if the
 * /system/xbin/su and /system/app/Superuser.apk files  
 *are found.
 *If the isDeviceRooted method returns false, then the
 *check box to run as root is disabled so as to keep
 *the app from trying to send the commands as superuser.   
 */
import java.io.File;

import wetsch.logcat.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity implements OnPreferenceChangeListener{
	CheckBoxPreference isRootEnabled;//objectt to the is_root preference.
	String tag = this.getClass().getName();//holds the activities full path.class name.
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_pref);
			isRootEnabled = (CheckBoxPreference) getPreferenceManager().findPreference("is_root");//linking to Preferences.
		
		//checking if device is rooted.
		if(isDeviceRooted()){
			isRootEnabled.setEnabled(true);
		}else{
			isRootEnabled.setEnabled(false);
			isRootEnabled.setSummaryOff("Your device does not appear to be rooted");
			Log.w(tag, "su and Superuser were not found");
		}
		isRootEnabled.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		switch(preference.getKey()){
		case "is_root":
			if(!isRootEnabled.isChecked())
			isRootEnabled.setSummary("Root enabled");
			break;
		};
		return true;
	}
	//Checking to see if Superuser.apk and su are on the device.
	private boolean isDeviceRooted(){
		Log.i(tag , "Checking device for root");
		//File apkFile = new File("/system/app/Superuser.apk");
		File binaryFile = new File("/system/xbin/su");

		if(binaryFile.exists())
			return true;
		else
			return false;
	
	}
}