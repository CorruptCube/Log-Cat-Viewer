package wetsch.logcat;
/*
 * This class holds the dialog box that asks to user to confirm their understanding about this application.
 * The onClickListenr implemented is used to change the status of the ok button when the agree check box is 
 * checked.  Once the user confirms that they understand, The onClicklistener for the OK button will save a
 * File stored in the applications internal storage folder with the value true.
 * This value tells the application to no longer display this dialog box at run time.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wetsch.logcat.R.id;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class UserAgreementDialog extends Dialog implements android.view.View.OnClickListener{

	private TextView messageText;//Holds message for dialog box.
	private CheckBox agreeCheckBox;//Allow user to agree to message before using the application.
	private Button okButton;//enabled and closes dialog if user checks agree check box.
	private Button calcelButton;
	private StringBuilder messageString;//Holds string for user agreement.
	private AssetManager assetManager;
	
	
	public UserAgreementDialog(Context context, int theme, AssetManager assets) {
		super(context, theme);
		this.assetManager = assets;
		setContentView(R.layout.user_agreement_dialog);
		SetupDialog();
		buildMessage();
	}

	private void SetupDialog(){
		messageString = new StringBuilder();
		setCancelable(false);
		setTitle("Log Cat");
		messageText = (TextView) findViewById(id.version_check_dialog_message_text);
		agreeCheckBox = (CheckBox) findViewById(id.version_check_dialog_agreecheckbox);
		okButton = (Button) findViewById(id.version_check_ok_button);
		okButton.setEnabled(false);
		calcelButton = (Button) findViewById(id.version_check_cancel_button);
		
		agreeCheckBox.setOnClickListener(this);
	
	}

	public void setOKButtonListener(View.OnClickListener listener){
		okButton.setOnClickListener(listener);
	}
	
	public void setCalcelButtonListener(View.OnClickListener listener){
		calcelButton.setOnClickListener(listener);
	}
	
	public void buildMessage(){
		InputStream inps;
		try {

			String line = null;
			inps = assetManager.open("useragreement-message.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(inps));
			while((line = in.readLine()) != null)
				messageString.append(line);
			messageText.setText(messageString.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.version_check_dialog_agreecheckbox:
				if(agreeCheckBox.isChecked())
					okButton.setEnabled(true);
				else
					okButton.setEnabled(false);
				}
	}
}
