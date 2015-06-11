package com.dk.smsmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dk.smsmanager.utils.AppConstants;

/**
 * BroadcastReceiver class for SMS api lower than kitkat  
 * @author Dhananjay Kumar
 *
 */
public class SmsReceiver extends BroadcastReceiver {

	public SmsReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Sending broadcast to change SMS lists
		Intent broadCastIntent = new Intent(AppConstants.SMS_RCVD_BROADCAST_ACTION);	
		context.sendBroadcast(broadCastIntent);
	}

}