package wetsch.logcat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;
/*
 * This class handles the background shell commands to work with log-cat.
 * The class takes in a context, boolean, text view and integer.
 * The context is used to allow the posting of Toast messages to the Main activity.
 * The boolean is used to tell the background tread to run shell commands as root.
 * The text view is used to populate the output from the shell commands.
 * The integer is passed in statically to specify which switch case to execute.
 * The output is then displayed to the user.
 */
public class BackgroundShell extends AsyncTask<Integer, Integer, String> {
	//Static final vars
	private Process process;//Access shell to execute commands.
	private BufferedReader is;//Input stream.
	private DataOutputStream os;//output stream used for root user.
	private BufferedReader STDERR;//Input stream to read errors.
	private StringBuilder logString = new StringBuilder();//Holds output from shell command.
	private StringBuilder errorString = new StringBuilder();//Holds errors from shell commands.
	private Context context;//passed in by the calling activity. Used to 
	private boolean isRoot;//Tells the task to run commands as super user.
	private String line;//used with the input buffer reader.
	private TextView text;//The ext view to store the output from logcat command.
	private ProgressDialog progressDialog;//Display to the user to let them know task is in progress.
	private actionPreform action;
	public BackgroundShell(Context context, boolean isRoot, TextView text, actionPreform action){
		this.context = context;
		this.isRoot = isRoot;
		this.text = text;
		this.action = action;
	}
	
	@Override
	protected void onPreExecute() {
		logString = new StringBuilder();
		errorString = new StringBuilder();
		progressDialog = new ProgressDialog(context,R.style.StyledProgressDialog);
		progressDialog.setTitle(R.string.progress_dialog_title_loading);
		progressDialog.setCancelable(false);
		if(action == actionPreform.clearLogCat)
			progressDialog.setMessage(context.getString(R.string.progress_dialog_message_clear_device));
		else
			progressDialog.setMessage(context.getString(R.string.progress_dialog_message_logcat_load));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String result) {
		if(action == actionPreform.clearLogCat)
			Toast.makeText(context, R.string.ToastMessageLogCleared, Toast.LENGTH_LONG).show();
		else if(logString.toString().isEmpty())
			text.setText(errorString.toString()+"\n Try turning on \"run as root\"in app settings.");
		else
			text.setText(logString.toString());
		progressDialog.dismiss();
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setProgress(values[0]);
		super.onProgressUpdate(values);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	protected String doInBackground(Integer... params) {
		try {
			switch (action) {
			case getLogCat:
				if(isRoot){
					process = Runtime.getRuntime().exec("su");
					os = new DataOutputStream(process.getOutputStream());
					os.writeBytes("logcat -d\n");
					os.writeBytes("exit\n");
					os.flush();
					os.close();
					is = new BufferedReader(new InputStreamReader(process.getInputStream()));
					
					while ((line = is.readLine()) != null){
							logString.append("\n").append(line);
						}
					publishProgress(75);
						//logString.append("\n\n"+totalLength);
				}else{
					process = Runtime.getRuntime().exec("logcat -d");
					is = new BufferedReader(new InputStreamReader(process.getInputStream()));
		            STDERR = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		              	while ((line = is.readLine()) != null){
							logString.append("\n").append(line);
		              	}
						while((line = STDERR.readLine()) != null){
							errorString.append("\n").append(line);
						}	
				}
				break;
			case clearLogCat:
				if(isRoot){
					process = Runtime.getRuntime().exec("su");
					os = new DataOutputStream((process.getOutputStream()));
					os.writeBytes("logcat -c\n");
					os.writeBytes("exit\n");
					os.flush();
					os.close();
				}else{
					Runtime.getRuntime().exec("logcat -c");
				}
				break;
			};//end of switch
			is.close();//Closing stream.
	}catch(Exception e){
		e.printStackTrace();
	}	
		return null;
	}
}
