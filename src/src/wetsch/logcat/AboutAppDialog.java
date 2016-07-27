package wetsch.logcat;
/**
 * This activity displays the about application dialog to the user.
 * This dialog contains the application name, version name and short
 * description about the application.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wetsch.logcat.R.id;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutAppDialog extends Activity {
	private String tag = getClass().getName();
	private AssetManager assets;
	private TextView dialogText;
	private StringBuilder dialogMessageText;
	private InputStream messageData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_app_dialog_layout);
		activitySetup();
		populateDialog();
	}
	
	private void activitySetup(){
		assets = getAssets();
		dialogMessageText =new StringBuilder();
		dialogText = (TextView) findViewById(id.message_text);
	}
	
	private void populateDialog(){
		String line = null;
		dialogMessageText.append("Application Version:\t"+getAppVersion()+"\n\n");
		try {
			messageData = assets.open("about_app_dialog_message.txt");
			BufferedReader data = new BufferedReader(new InputStreamReader(messageData));
			while ((line = data.readLine()) != null){
				if(line.equals("\\n"))
					dialogMessageText.append("\n");
				else if(line.equals("\\n\\n"))
					dialogMessageText.append("\n\n");
				else if(line.equals("\\u2022"))
					dialogMessageText.append("\n\u2022 " + data.readLine());
				else
					dialogMessageText.append(line);
			}
			messageData.close();
			data.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		dialogText.setText(dialogMessageText.toString());
	}

	//Getting application version name.
	private String getAppVersion(){
		String versionName = null;
			try {
				versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				Log.e(tag, "There was a problem while fetching the package version name.");
			}
			return versionName;
	}
}