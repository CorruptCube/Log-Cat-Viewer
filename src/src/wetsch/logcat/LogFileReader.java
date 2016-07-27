package wetsch.logcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
/*
 * This class reads back in the data stored in the text file.
 * This class takes in a context, text-view and a string.
 * The context tells the progress dialog whre to display it's window.
 * The text view is for loading in the data from the text files.
 * The string holds the file name to be opened and loaded back in to the application.
 */
public class LogFileReader extends AsyncTask<String, Integer, String> {
	private File logFile;//File to read from.
	private byte[] data;//Holds the byte data from the file.
	private TextView text;//Linked to the text-view in the main activity.
	private ProgressDialog progressDialog;//Show the task.
	private Context context;//Tell the progress dialog which activity is is to display on.
	private String tag = this.getClass().getName();
	public LogFileReader(Context context, TextView text){
		this.context = context;
		this.text = text;
	}
	@Override
	protected String doInBackground(String... params) {
		logFile = new File(params[0]);
		String collected = null;
		publishProgress(25);
		try {
			FileInputStream fis = new FileInputStream(logFile.getPath()
					.toString());
			data = new byte[fis.available()];
			while (fis.read(data) != -1) {
				collected = new String(data);
			}
			fis.close();
			publishProgress(99);
			return collected;
		} catch (FileNotFoundException e) {
			Log.i(tag , "Log file not found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(tag , "There was an input output error wile accessing log file.");

		} catch (Exception e) {
			Log.i(tag , "Unknown error.");
			e.printStackTrace();
		}
		return params[0];
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context, R.style.StyledProgressDialog);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle("Loading Log");
		progressDialog.setMax(100);
		progressDialog.show();
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.incrementProgressBy(values[0]);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(String result) {
		text.setText(result.toString());
		progressDialog.dismiss();
		super.onPostExecute(result);
	}
}