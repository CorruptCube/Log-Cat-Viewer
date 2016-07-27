package wetsch.logcat;
/*
 * This class holds the log manager activity.
 * This activity allows you to reload or delete the saved logs on the SD card.
 * The getFilesArray method that populates the file array list will check to
 * make sure the logcat directory is on the SD card.
 * In the event that this directory does not exists, the directory will be created.
 * If this check is not done, and the directory is not on the SD card, the
 * activity will crash.
 * The longClick listener allows the user to delete a log off the SD card, and
 * the normal itemClicked listener allows the user to reload a saved log.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import wetsch.logcat.R;
import wetsch.logcat.R.id;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LogManager extends Activity{
	private TextView tvPathONSDCard;//displays the path to user where logs are to be stored on device.
	private int arrayAdapterPosition;//keep track of the lists item position in the arrays.
	private File path = new File(Environment.getExternalStorageDirectory().toString()+"/LogCat");//path too logs saved on Sd card.
	private ArrayList<File> fileArray = new ArrayList<File>();//hold the files in the logcat folder on SD card.
	private ArrayAdapter<File> arrayAdapter;//Adapter used for list view.
	private AlertDialog.Builder alBuilder;//Confirm if user wants to execute a task.
	private ProgressDialog fileDeleteStatus;//Progress dialog to show delete status of all files.
	private MenuItem deleteAllLogs;//Access the delete all log files button on action bar.
	private ListView list;
	//private	 Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logmanager);
		populateFileArray();//Scanning for saved logs on SD card.
		 arrayAdapter = new CustomArrayAdapter();
		 list = (ListView) findViewById(id.log_files_listview);//object to access ListView.
		 list.setAdapter(arrayAdapter);//Setting list-adapter used to populate list view. 
		 tvPathONSDCard = (TextView) findViewById(id.path_on_sd_card);
		 tvPathONSDCard.setText(path.toString());
		listeners();//Setup listeners for activity.
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.logmanagermenu, menu);
		deleteAllLogs = menu.findItem(id.deleteallfiles);
		if(!fileArray.isEmpty())
			deleteAllLogs.setVisible(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.deleteallfiles:
			alBuilder = new AlertDialog.Builder(this,R.style.dialogstyle);
			alBuilder.setIcon(R.drawable.questionmark);
			alBuilder.setTitle(R.string.alert_dialog_title_Confirmation);
			alBuilder.setMessage(R.string.alert_dialog_delete_all_logs_message);
			alBuilder.setCancelable(false);
			alBuilder.setPositiveButton(R.string.alert_dialog_file_button_Confirm, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new DeleteLog(actionPreform.deleteAllLogs).execute(fileArray.size());
					
				}
			});
			alBuilder.setNegativeButton(R.string.alert_dialog_file_button_cancel, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(LogManager.this, "Operation canceld",Toast.LENGTH_LONG).show();
				}
			});
			alBuilder.show();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	//
	private void populateFileArray(){
		try{
			if(!path.exists())
				path.mkdirs();
			fileArray.addAll(Arrays.asList(path.listFiles()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void deleteSingleLog(){
		alBuilder = new AlertDialog.Builder(this,R.style.dialogstyle);
		alBuilder.setCancelable(false);
		alBuilder.setIcon(R.drawable.questionmark);
		alBuilder.setTitle(R.string.alert_dialog_title_Confirmation);
		alBuilder.setMessage(R.string.alert_dialog_delete_a_log_message);
		alBuilder.setPositiveButton(getResources().getString(R.string.alert_dialog_file_button_Confirm), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DeleteLog(actionPreform.deleteLog).execute(arrayAdapterPosition);
				}	
		});
		alBuilder.setNegativeButton(getResources().getString(R.string.alert_dialog_file_button_cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Operation canceld",Toast.LENGTH_LONG).show();;
			}
		});
	}
	private void listeners(){
		//Setting up the OnItemClickListener.
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent resultdata = new Intent();
				resultdata.putExtra("filename", fileArray.get(arrayAdapterPosition).getPath().toString());
				setResult(RESULT_OK, resultdata);
				finish();
			}
		});//End of setOnclickItemListener.
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				arrayAdapterPosition = position;
				deleteSingleLog();
				alBuilder.show();
				return true;
			}
		});
	}
/*
 * /The CustomArrayAdapter is used to build
 * the custom layout for the activity's log_files_listview.
 */
private class CustomArrayAdapter extends ArrayAdapter<File>{
	public CustomArrayAdapter(){
		super(LogManager.this,R.layout.filemanager_listviewlayout1 ,fileArray);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myView = convertView;
		if(myView == null)
			myView = getLayoutInflater().inflate(R.layout.filemanager_listviewlayout1,parent, false);
		File currentFile = fileArray.get(position);
		TextView filename = (TextView) myView.findViewById(R.id.file_name);
		TextView FileSize = (TextView) myView.findViewById(R.id.file_size);
		filename.setText(currentFile.getName().toString());
		FileSize.setText(Long.toString(currentFile.length()/1024)+"kB");
		return myView;
	}//End of getView method.
}//End of CustomArrayAdapter class.
//Delete all logs on SD-Card.
private class DeleteLog extends AsyncTask<Integer, Integer, Boolean>{
	private actionPreform action;
	private DeleteLog(actionPreform action){
		this.action = action;
	}
	@SuppressWarnings("incomplete-switch")
	@Override
	protected Boolean doInBackground(Integer... params) {
		try{
			switch(action){
			case deleteAllLogs:
			int progress = 0;
		while(!fileArray.isEmpty()){
			progress++;
			fileArray.get(0).delete();
			fileArray.remove(0);
			publishProgress(progress+1);
			Thread.sleep(88);
		}
			return true;
			case deleteLog:
				fileArray.get(arrayAdapterPosition).delete();
				fileArray.remove(arrayAdapterPosition);
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}
		return false;
	}
	@Override
	protected void onPreExecute() {
		if(action == actionPreform.deleteAllLogs){
			fileDeleteStatus= new ProgressDialog(LogManager.this, R.style.StyledProgressDialog);
			fileDeleteStatus.setTitle("DElete All");
			fileDeleteStatus.setMessage("Deleting all logs on SD-Card.");
			fileDeleteStatus.setMax(fileArray.size());
			fileDeleteStatus.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			fileDeleteStatus.show();
			}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(result){
			arrayAdapter.notifyDataSetChanged();
			if(action == actionPreform.deleteAllLogs){
				Toast.makeText(LogManager.this, "All Log files removed.", Toast.LENGTH_LONG).show();
				fileDeleteStatus.dismiss();
				deleteAllLogs.setVisible(false);
				fileDeleteStatus.dismiss();
			}else if(action == actionPreform.deleteLog){
				arrayAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),"Log deleated.", Toast.LENGTH_LONG).show();
				if(fileArray.isEmpty())
					deleteAllLogs.setVisible(false);
		}
		}else{
			Toast.makeText(LogManager.this, "There was an error while deleting files.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		fileDeleteStatus.setProgress(values[0]);
		super.onProgressUpdate(values);
	}
}
}