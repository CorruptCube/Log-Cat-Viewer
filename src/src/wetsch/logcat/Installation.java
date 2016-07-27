package wetsch.logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class Installation {
	private static   File didUserAgree;//Holds the value used to check if user agreed to user agreement.
	private static SharedPreferences sharedPref = null;//The shared preferences object.
	private static String tag = null;//Used to set class name for inserting into log-cat.

    /**
	 * This method checks to see if the app version matches the current app version .
	 * The about app dialog will be displayed if the version does not match.
	 * @param context The activity from which this method is called.
	 */
    public static void checkVersion(Context context){
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		//Checking if the key that holds the app version exists in shared preferences.
		if(!sharedPref.contains("version_name")){
			Editor e = sharedPref.edit();
			e.putString("version_name", context.getString(R.string.version_name));
			e.commit();
			context.startActivity(new Intent(context, AboutAppDialog.class));
			
		/*If the key that holds the app version exists, checking if the app version stored in the key 
		* matches.
		*/
		}else if(!sharedPref.getString("version_name", "").equals(context.getString(R.string.version_name))){
			Editor e = sharedPref.edit();
			e.putString("version_name", context.getString(R.string.version_name));
			e.commit();
			context.startActivity(new Intent(context, AboutAppDialog.class));
		}
	}
    
	//Check if the user has agreed to the applications conditions.
    public static void userAgreementCheck(Context context){
		didUserAgree = new File(context.getFilesDir(),"useragreed.txt");
		boolean value= false;
		if(didUserAgree.exists()){
		try {
			FileInputStream fis = new FileInputStream(didUserAgree);
			byte[] data = new byte[fis.available()];
			while(fis.read(data)!= -1)
				value = Boolean.parseBoolean(new String(data));
			fis.close();
			if(value){
				return;
			}
		} catch (FileNotFoundException e) {
			tag = context.getClass().getName();
			e.printStackTrace();
			Log.e(tag , "File not found exseption thrown in user agreement check.");
			System.exit(0);
		} catch (IOException e) {
			Log.e(tag , "An input output exseption was thrown while checking user agreement.");
			e.printStackTrace();
			System.exit(0);
		}
		}else{
			showUserAgreementPopupDialog(context);
		}
	}
	
    /*
     * Shows the agreement dialog.
     * If the user clicks cancel, the app will be closed.
     */
	private static void showUserAgreementPopupDialog(Context context){
		AssetManager assetsManager = context.getAssets();
		final UserAgreementDialog userAgreement = new UserAgreementDialog(context,R.style.dialogstyle,assetsManager);
		userAgreement.setOKButtonListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					if(!didUserAgree.exists())
						didUserAgree.createNewFile();
					BufferedWriter output = new BufferedWriter(new FileWriter(didUserAgree));
					output.write(Boolean.toString(true));
					output.flush();
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(tag , "Input output exseption thrown while writing value for user agreement.");
					System.exit(0);
				}
				userAgreement.dismiss();
			}
		});
		//If user clicks cancel, the application will close.
		userAgreement.setCalcelButtonListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				userAgreement.dismiss();
				System.exit(0);
			}
		});
		userAgreement.show();
		}


}