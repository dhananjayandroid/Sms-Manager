package com.dk.smsmanager.service;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.dk.smsmanager.utils.CommonUtils;

/**
 * IntentService Class for handing "respond via message" quick reply
 * @author Dhananjay Kumar
 *
 */
public class HeadlessSmsSendService extends IntentService{

	private static final String TAG = "HeadlessSmsSendService";
    public HeadlessSmsSendService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (CommonUtils.hasJellyBeanMR2() && CommonUtils.isDefaultSmsApp(this) &&
                    // ACTION_RESPOND_VIA_MESSAGE was added in JB MR2
                    TelephonyManager.ACTION_RESPOND_VIA_MESSAGE.equals(intent.getAction())) {
                // TODO: Handle "respond via message" quick reply
            }
        }
    }

}
