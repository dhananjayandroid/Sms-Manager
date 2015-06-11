package com.dk.smsmanager.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony;
import android.provider.Telephony.Sms.Intents;
import android.util.Log;

import com.dk.smsmanager.dao.SmsMsg;

public class CommonUtils {
	/**
	 * Check if the default SMS app, ask to make if not
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	public static boolean checkAndAskForDefaultApp(Context context) {
		if (!isDefaultSmsApp(context)) {
			setDefaultSmsApp(context);
		}
		return true;
	}

	/**
	 * Check if the device runs Android 4.3 (JB MR2) or higher.
	 */
	public static boolean hasJellyBeanMR2() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
	}

	/**
	 * Check if the device runs Android 4.4 (KitKat) or higher.
	 */
	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	/**
	 * Check if your app is the default system SMS app.
	 * 
	 * @param context
	 *            The Context
	 * @return True if it is default, False otherwise. Pre-KitKat will always
	 *         return True.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean isDefaultSmsApp(Context context) {
		if (hasKitKat()) {
			return context.getPackageName().equals(
					Telephony.Sms.getDefaultSmsPackage(context));
		}
		return true;
	}

	/**
	 * Trigger the intent to open the system dialog that asks the user to change
	 * the default SMS app.
	 * 
	 * @param context
	 *            The Context
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void setDefaultSmsApp(Context context) {
		// This is a new intent which only exists on KitKat
		if (hasKitKat()) {
			Intent intent = new Intent(Intents.ACTION_CHANGE_DEFAULT);
			intent.putExtra(Intents.EXTRA_PACKAGE_NAME,
					context.getPackageName());
			context.startActivity(intent);
		}
	}

	/**
	 * Manullay insert SMS in db
	 * @param context
	 * @param address
	 * @param smsBody
	 * @param type
	 * @return success
	 */
	public static boolean insertSMSInDB(Context context, String address,
			String smsBody, int type) {
		boolean success;
		try {
			ContentValues values = new ContentValues();
			values.put("address", address);
			values.put("body", smsBody);
			values.put("type", type);
			context.getContentResolver().insert(AppConstants.SMS_URI,
					values);
			success = true;
		} catch (Exception e) {
			success = false;
		}
		return success;
	}

	/**
	 * Get the name if phone number exists in contact
	 * @param context
	 * @param phoneNumber
	 * @return name
	 */
	public static String getContactName(Context context, String phoneNumber) {
		String name = phoneNumber;
		try {
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(phoneNumber));
			Cursor c = resolver
					.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
							null, null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					name = c.getString(c
							.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
				}
				c.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return name;
	}

	/**
	 * Add Name to the SmsMsg if exist in Contact
	 * @param context
	 * @param smsMsg
	 * @return
	 */
	public static SmsMsg addContactName(Context context, SmsMsg smsMsg) {
		String name = smsMsg.getAddress();
		smsMsg.setName(name);
		try {
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(smsMsg.getAddress()));
			Cursor c = resolver
					.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
							null, null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					name = c.getString(c
							.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
					smsMsg.setName(name);
					smsMsg.setIsContact(true);
				}
				c.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return smsMsg;
	}

	/**
	 * Check if PhoneNumber exists in contact
	 * @param context
	 * @param phoneNumber
	 * @return isContact
	 */
	public static boolean isContact(Context context, String phoneNumber) {
		boolean isContact = false;
		try {
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(phoneNumber));
			Cursor c = resolver
					.query(uri, new String[] { PhoneLookup.DISPLAY_NAME },
							null, null, null);
			if (c != null && c.getCount() > 0) {
				isContact = true;
				c.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isContact;
	}

	/**
	 * Marks SMS as read
	 * @param context
	 * @param msg_id
	 * @param thread_id
	 */
	public static void setMsgRead(Context context, String msg_id, String thread_id) {
		Log.e("Read: msgId: ", msg_id);
		ContentValues values = new ContentValues();
		values.put("read", true);
		context.getContentResolver().update(
				AppConstants.SMS_URI,
				values,
				"_id=? and thread_id=?",
				new String[] { String.valueOf(msg_id),
						String.valueOf(thread_id) });
	}

	/**
	 * Deletes SMS form SMS db
	 * @param context
	 * @param msg_id
	 * @param thread_id
	 * @return
	 */
	public static boolean deleteSms(Context context, String msg_id, String thread_id) {
		Log.e("delete: msgId: ", msg_id);
		Log.e("delete: thread_id: ", thread_id);
		boolean isSmsDeleted = false;
		try {
			context.getContentResolver().delete(
					AppConstants.SMS_URI,
					"_id=? and thread_id=?",
					new String[] { String.valueOf(msg_id),
							String.valueOf(thread_id) });
			isSmsDeleted = true;
		} catch (Exception ex) {
			isSmsDeleted = false;
		}
		return isSmsDeleted;
	}
}
