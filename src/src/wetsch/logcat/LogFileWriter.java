package wetsch.logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
/*
 * This class writes the log-cat output to a text file stored on the device's SD-Card.
 * The class takes in a context and a string.
 * The context allows posting of Toast messages to the main activity.
 * The string holds the log-cat output to be written to the text file.
 * The file names are set as the current time stamp.
 * 
 */
public class LogFileWriter extends AsyncTask<String, Integer, Boolean> {
	private File path;
	private File logFile;
	private String userFileName;
	private Context context;
	private boolean fileExists = false;
	public LogFileWriter(Context context, String fileName){
		this.context = context;
		userFileName = fileName;
		path = new File(Environment.getExternalStorageDirectory().toString()+"/LogCat");
		
	}
	
	@Override
	protected void onPreExecute() {
		
		logFile = new File(path,userFileName+".txt");


		super.onPreExecute();
	}



	@Override
	protected Boolean doInBackground(String... params) {
		String checkMediaStorage = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(checkMediaStorage)){
			try {
				path.mkdirs();//make directory on SD card for log files.
				//Checking if file created for the log exists
				if(!logFile.exists()){
					logFile.createNewFile();
					BufferedWriter output = new BufferedWriter(new FileWriter(logFile));
					output.write(params[0]);
					output.flush();
					output.close();
					return true;
				}else{
					fileExists = true;
					return true;
				}
				
					
			} catch (IOException e) {
				
				e.printStackTrace();	
			}//End of try catch.
		}//End of if statement for SD card check.
		return false;
	}
	
	
	@Override
	protected void onPostExecute(Boolean result) {
		if(fileExists){
			Toast.makeText(context, "File already exists.", Toast.LENGTH_LONG).show();;
			
		}else if(result){
			Toast.makeText(context, "File saved to" + logFile.getParent().toString()+"/"+logFile.getName().toString(), Toast.LENGTH_LONG).show();;
		}else
			Toast.makeText(context, "File cound not be created.", Toast.LENGTH_LONG).show();;
		super.onPostExecute(result);
	}
	
	

}
