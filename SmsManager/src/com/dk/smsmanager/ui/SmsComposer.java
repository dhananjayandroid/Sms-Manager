package com.dk.smsmanager.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dk.smsmanager.R;
import com.dk.smsmanager.utils.AppConstants;
import com.dk.smsmanager.utils.CommonUtils;

/**
 * Class for SMS Composer
 * @author DELL PC
 *
 */
public class SmsComposer implements OnClickListener {

	private Dialog composeDialog;
	private EditText edt_compose_number, edt_send_body;
	private Button btn_send;
	private ProgressDialog sendingSmsDialog;
	private Context mContext;
	private final String SMS_SENT = "SMS_SENT";
	private final String DELIVERED = "SMS_DELIVERED";
	private String address;

	public void showComposeMsgDialog(Context mContext, String number){
		this.address = number;
		this.mContext = mContext;
		composeDialog = new Dialog(mContext);
		composeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		composeDialog.setContentView(R.layout.dialog_compose_msg);

		edt_compose_number = (EditText) composeDialog.findViewById(R.id.edt_compose_number);
		edt_send_body = (EditText) composeDialog.findViewById(R.id.edt_send_body);
		btn_send = (Button) composeDialog.findViewById(R.id.btn_send);


		if(address!= null){
			edt_compose_number.setText(CommonUtils.getContactName(mContext, address));
			edt_compose_number.setEnabled(false); 
		}
		btn_send.setEnabled(false);
		btn_send.setOnClickListener(this);
		edt_send_body.addTextChangedListener(emptyTextWatcher);
		composeDialog.show();
	}

	// TextWatcher to disable send button if no text in body
	TextWatcher emptyTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {
			if (TextUtils.isEmpty(edt_send_body.getText().toString().trim()))
				btn_send.setEnabled(false);
			else
				btn_send.setEnabled(true);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			if(address == null)
				address = edt_compose_number.getText().toString();
			if(address != null && address.length() > 0 && edt_send_body.getText().toString().length() > 0){
				if(CommonUtils.checkAndAskForDefaultApp(mContext)){
					SendSMS();
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Send SMS to the provided address
	 */
	private void SendSMS() {
		SmsManager smsManager = SmsManager.getDefault();
		sendingSmsDialog = new ProgressDialog(mContext);
		sendingSmsDialog.setMessage(mContext.getString(R.string.sending_));
		sendingSmsDialog.show();

		try {
			if(edt_send_body.getText().toString().length() <= 140){
				PendingIntent sentPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(SMS_SENT), 0);
				PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(DELIVERED), 0);
				mContext.registerReceiver(smsSentReceiver, new IntentFilter(SMS_SENT));
				mContext.registerReceiver(smsDeliveredReceiver , new IntentFilter(DELIVERED));
				smsManager.sendTextMessage(address, null, edt_send_body.getText().toString(), sentPendingIntent, deliveredPendingIntent);
			} else {
				ArrayList<String> smsBodyParts = smsManager.divideMessage(edt_send_body.getText().toString());
				ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
				ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
				PendingIntent sentPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(SMS_SENT), 0);
				PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(DELIVERED), 0);
				mContext.registerReceiver(smsSentReceiver, new IntentFilter(SMS_SENT));
				mContext.registerReceiver(smsDeliveredReceiver , new IntentFilter(DELIVERED));
				for (int i = 0; i < smsBodyParts.size(); i++) {
					sentPendingIntents.add(sentPendingIntent);
					deliveredPendingIntents.add(deliveredPendingIntent);
				}
				smsManager.sendMultipartTextMessage(address, null, smsBodyParts, sentPendingIntents, deliveredPendingIntents);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// BroadcastReceiver for capturing SMS sent event
	private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(sendingSmsDialog != null && sendingSmsDialog.isShowing())
				sendingSmsDialog.dismiss();
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				if(composeDialog != null && composeDialog.isShowing())
					composeDialog.dismiss();
				// Insert Sent sms in SMS db
				// if(CommonUtils.hasKitKat())
					CommonUtils.insertSMSInDB(mContext, address, edt_send_body.getText().toString(), 0);
				// Send BroadCast to update SMS list
				Intent broadCastIntent = new Intent(AppConstants.SMS_RCVD_BROADCAST_ACTION);	
				context.sendBroadcast(broadCastIntent);
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
				break;
			}
			// unregister self
			try {
				context.unregisterReceiver(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// BroadcastReceiver for capturing SMS delivered event
	private BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {

			case Activity.RESULT_OK:
				Toast.makeText(mContext, "SMS delivered", Toast.LENGTH_SHORT).show();
				break;
			case Activity.RESULT_CANCELED:
				Toast.makeText(mContext, "SMS not delivered", Toast.LENGTH_SHORT).show();
				break;
			}
			// unregister self
			try {
				context.unregisterReceiver(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


}
