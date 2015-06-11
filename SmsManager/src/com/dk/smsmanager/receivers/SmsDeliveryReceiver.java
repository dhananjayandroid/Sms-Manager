package com.dk.smsmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.dk.smsmanager.utils.AppConstants;
import com.dk.smsmanager.utils.CommonUtils;

/**
 * BroadcastReceiver class for SMS api higher than or equal to kitkat  
 * @author Dhananjay Kumar
 *
 */
public class SmsDeliveryReceiver extends BroadcastReceiver {
	public static final String SMS_BUNDLE = "pdus";

	public SmsDeliveryReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle intentExtras = intent.getExtras();
		String smsMessageStr = "";
		String address = "";
		if (intentExtras != null && CommonUtils.isDefaultSmsApp(context)) {
			Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
			for (int i = 0; i < sms.length; ++i) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

				String smsBody = smsMessage.getMessageBody().toString();
				address = smsMessage.getOriginatingAddress();
				smsMessageStr += smsBody;
			}
			
			// Inserting SMS in db 
			CommonUtils.insertSMSInDB(context, address, smsMessageStr, 1);
		}
		// Sending broadcast to change SMS lists
		Intent broadCastIntent = new Intent(AppConstants.SMS_RCVD_BROADCAST_ACTION);	
		context.sendBroadcast(broadCastIntent);
	}

}