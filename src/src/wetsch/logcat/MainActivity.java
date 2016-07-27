package wetsch.logcat;
/*
 * This is the applications main activity class.
 * This class houses the main options menu and
 * text view for he logs.
 * The application is configured to store the
 * saved logs to a folder called log-cat on the root of the SD card.
 * The application uses a separate thread to query the
 * devise log-cat buffer. That way the main UI thread will
 * not be tied up with each query to dump or clear the logcat buffer. 
 */

import wetsch.logcat.R;
import wetsch.logcat.R.id;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
	private SharedPreferences getPref;//object used to get saved preferences
	private TextView text;//Hold log pulled in from logcat.
	private boolean isRootEnabled;//Used to run application as root.
	private AlertDialog.Builder albuilder;//Confirm if user wants to execute a task.
	private EditText filename;//Access edit text in dialog when user specifies file name.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActivitySetup();//Setting up activity
		getPreferences();//Getting saved preferences.
		Installation.userAgreementCheck(this);
		Installation.checkVersion(this);
	}

	@Override
	protected void onResume() {
		getPreferences();
		super.onResume();
	}
	

	//Inflating option menu.
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	//option menu item listener.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			//Clear log-cat.
		case R.id.menu_clear_log:
			albuilder = new AlertDialog.Builder(MainActivity.this,R.style.dialogstyle);
			albuilder.setIcon(R.drawable.questionmark);
			albuilder.setPositiveButton(R.string.alert_dialog_file_button_Confirm, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new BackgroundShell(MainActivity.this, isRootEnabled, text,actionPreform.clearLogCat).execute(0);
				}
			});
			albuilder.setNegativeButton(R.string.alert_dialog_file_button_cancel, new OnClickListener(){
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(MainActivity.this, R.string.ToastMesgOperationCanceled, Toast.LENGTH_LONG).show();
				}
			});
			albuilder.setTitle(R.string.alert_dialog_title_Confirmation);
			albuilder.setMessage(R.string.alert_dialog_clear_log_device_message);
			albuilder.show();
			break;
			//Clear screen
		case R.id.menu_clear_screen:
			text.setText("");
			break;
			//Refresh log-cat.
		case R.id.menu_refresh_log:
			new BackgroundShell(this, isRootEnabled, text,actionPreform.getLogCat).execute(0);
			break;
			//save log to SD card.
		case R.id.menu_save_to_sd:
			getSaveFileNameAs();
			
			break;
			//show application Preferences.
		case R.id.menu_settings:
			Intent appSettings = new Intent(MainActivity.this, Preferences.class);
			startActivity(appSettings);
			break;
			//Log-Manager.
		case R.id.menu_manage_logs:
			Intent logManager = new Intent(MainActivity.this,LogManager.class);
			startActivityForResult(logManager,0);
			break;
		case R.id.menu_about_app:
			Intent aboutApplication = new Intent(this, AboutAppDialog.class);
			startActivity(aboutApplication);
			break;
			//Exit the application.
		case R.id.menu_Exit:
			finish();
			break;
		};
		return super.onOptionsItemSelected(item);
	}
	
	//Get the file name from user when user wants to save log to SD-card.
	@SuppressLint("InflateParams")
	private void getSaveFileNameAs(){
		final InputMethodManager imm = (InputMethodManager) MainActivity.this
	            .getSystemService(Context.INPUT_METHOD_SERVICE);
		String dateTime;//This string is used for the name of the text file saved o the SD-card.
    	Time now = new Time();//get the time from the system.
		now.setToNow();
		dateTime = now.format2445();
		View dialogView = getLayoutInflater().inflate(R.layout.view_custom_edit_text, null);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.dialogstyle);
		dialog.setIcon(R.drawable.questionmark);
		dialog.setTitle(R.string.alert_dialogSaveFileTitle);
		dialog.setMessage(R.string.alert_dialogSaveFileMessage);
		dialog.setCancelable(false);
		dialog.setView(dialogView);
		
		filename = (EditText)dialogView.findViewById(id.dialog_Filename);
		filename.setText(dateTime);
		
		dialog.setPositiveButton(R.string.user_agreement_dialog_ok_button, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(filename.getText().toString().isEmpty())
					Toast.makeText(MainActivity.this, R.string.alert_dialogSaveFileEmptyTextBox, Toast.LENGTH_LONG).show();
				else
					new LogFileWriter(MainActivity.this, filename.getText().toString()).execute(text.getText().toString());
				imm.hideSoftInputFromWindow(filename.getWindowToken(), 0);

			}
		});
		dialog.setNegativeButton(R.string.user_agreement_dialog_calcel_button, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				imm.hideSoftInputFromWindow(filename.getWindowToken(), 0);
				dialog.dismiss();
				Toast.makeText(MainActivity.this, R.string.ToastMesgOperationCanceled, Toast.LENGTH_LONG).show();
			}
		});
	dialog.show();

    if (!imm.isAcceptingText()) 
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 
                InputMethodManager.HIDE_IMPLICIT_ONLY);  

	}
	
	//Getting saved preferences.
	private void getPreferences(){
		isRootEnabled = getPref.getBoolean("is_root", false);
	}
	
	
	//Setting up activity objects.
	private void ActivitySetup() {
		getPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		text = (TextView) findViewById((R.id.textview1));
		text.setText("Use the refresh log option  on the options bar to dump the current logcat data..");
	}
	
	
	//This method gets the result from the LogManager class.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK)
			new LogFileReader(this,text).execute(data.getStringExtra("filename").toString());
		super.onActivityResult(requestCode, resultCode, data);
		}//End  of onActivityResult
}