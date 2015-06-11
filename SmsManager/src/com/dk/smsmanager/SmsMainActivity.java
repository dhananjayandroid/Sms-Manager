package com.dk.smsmanager;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.dk.smsmanager.adapters.SmsMainAdapter;
import com.dk.smsmanager.dao.SmsMsg;
import com.dk.smsmanager.ui.LoadMoreListView;
import com.dk.smsmanager.ui.LoadMoreListView.OnLoadMoreListener;
import com.dk.smsmanager.ui.SmsComposer;
import com.dk.smsmanager.utils.AppConstants;
import com.dk.smsmanager.utils.CommonUtils;


/**
 * This Activity class is used to list all recent SMS of contacts
 * @author Dhananjay Kumar
 *
 */
public class SmsMainActivity extends ListActivity implements OnClickListener {

	private ArrayList<SmsMsg> listSmsMsg = new ArrayList<SmsMsg>();
	private SmsMainAdapter smsMainAdapter;
	private int REL_SWIPE_MIN_DISTANCE;
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;
	private SMSUpdateReceiver smsUpdtdReceiver;
	private long start_date;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_main);
		start_date = System.currentTimeMillis(); // time in milli-sec to perform paginated query on SMS db
		
		// getting values for understanding swipe gesture
		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY = (int) (200.0f * dm.densityDpi / 160.0f + 0.5);

		smsMainAdapter = new SmsMainAdapter(this, listSmsMsg);
		setListAdapter(smsMainAdapter);

		setListenersOnViews();
	}

	/**
	 * Setting different listeners on Views
	 */
	private void setListenersOnViews() {
		findViewById(R.id.iv_new).setOnClickListener(this);
		((LoadMoreListView) getListView())
		.setOnLoadMoreListener(new OnLoadMoreListener() {
			public void onLoadMore() {
				loadMsgList(false);
			}
		});

		final GestureDetector gestureDetector = new GestureDetector(this,
				new ListViewGestureListener());
		View.OnTouchListener gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};
		getListView().setOnTouchListener(gestureListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		resetSmsList();
		CommonUtils.checkAndAskForDefaultApp(this);
		// registering broadcast for if any SMS comes or goes
		smsUpdtdReceiver = new SMSUpdateReceiver();
		registerReceiver(smsUpdtdReceiver, new IntentFilter(
				AppConstants.SMS_RCVD_BROADCAST_ACTION));
	}

	/**
	 * Reset Full SMS List
	 */
	private void resetSmsList() {
		listSmsMsg.clear();
		start_date = System.currentTimeMillis();
		loadMsgList(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(smsUpdtdReceiver);
	}

	/**
	 * Load SMS List
	 * @param showDialog
	 */
	public void loadMsgList(boolean showDialog) {
		new LoadSMSAsync(showDialog).execute();
	}

	/**
	 * Load SMS list from SMS db
	 * @return SMS list
	 */
	public ArrayList<SmsMsg> loadSms() {
		ArrayList<SmsMsg> tempSmsList = new ArrayList<SmsMsg>();
		try {
			Log.e("start_date", "" + start_date);
			Cursor cursor = getContentResolver().query(
					AppConstants.SMS_URI,
					new String[] { "DISTINCT address", "_id", "thread_id",
						"date", "body", "type", "read",
					"reply_path_present" }, // DISTINCT
					"address IS NOT NULL) AND date <'" + start_date + "'"
					+ " GROUP BY (address", // GROUP BY
					null, "date desc LIMIT 30");
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					tempSmsList.add(new SmsMsg(this, cursor));
				}
			}
			if (cursor != null)
				cursor.close();
			if (tempSmsList != null && tempSmsList.size() > 0)
				start_date = tempSmsList.get(tempSmsList.size() - 1)
				.getDateMillisec();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tempSmsList;
	}

	/**
	 * Async Task to Load SMS List in background
	 * @author Dhananjay Kumar
	 *
	 */
	private class LoadSMSAsync extends AsyncTask<String, Void, ArrayList<SmsMsg>> {
		private ProgressDialog dialog;
		private boolean showDialog;

		public LoadSMSAsync(boolean showDialog) {
			this.showDialog = showDialog;
			dialog = new ProgressDialog(SmsMainActivity.this);
			dialog.setMessage(getString(R.string.loading_));
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(showDialog)
				dialog.show();
		}
		@Override
		protected ArrayList<SmsMsg> doInBackground(String... objs) {
			return loadSms();
		}

		@Override
		protected void onPostExecute(ArrayList<SmsMsg> result) {
			if (result != null)
				listSmsMsg.addAll(result);
			if (smsMainAdapter != null)
				smsMainAdapter.notifyDataSetChanged();
			((LoadMoreListView) getListView()).onLoadMoreComplete();
			if(dialog != null && dialog.isShowing())
				dialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_new:
			// show SMS Composer dialog
			new SmsComposer().showComposeMsgDialog(this, null);
			break;

		default:
			break;
		}
	}

	private void myOnItemClick(int position) {
		Intent intent = new Intent(this, SmsListActivity.class);
		intent.putExtra("smsMsg", smsMainAdapter.getItem(position));
		startActivity(intent);
	}

	/**
	 * Own Gesture Listener class to capture swipe on ListView item 
	 * @author Dhananjay Kumar
	 *
	 */
	private class ListViewGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			ListView lv = getListView();
			int pos = lv.pointToPosition((int) e.getX(), (int) e.getY());
			myOnItemClick(pos);
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH)
				return false;
			if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
				Log.i("Right to left", "Right to left");
			} else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
				Log.i("left to Right", "left to Right");
				ListView lv = getListView();
				int pos = lv.pointToPosition((int) e1.getX(), (int) e1.getY());
				onLeftToRightSwipe(pos);
			}
			return false;
		}
	}

	/*
	 * perform action on Left To Right swipe on ListView items
	 */
	private void onLeftToRightSwipe(int position) {
		try {
			SmsMsg sms = smsMainAdapter.getItem(position);
			// setMsgRead(sms.get_id(), sms.getThreadId());
			if (CommonUtils.isContact(this, sms.getAddress()))
				CommonUtils.setMsgRead(this, sms.get_id(), sms.getThreadId());
			else
				CommonUtils.deleteSms(this, sms.get_id(), sms.getThreadId());
			resetSmsList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * BroadcastReceiver to Capture SMS send or receive and reset SMS list
	 * @author Dhananjay Kumar
	 *
	 */
	private class SMSUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			resetSmsList();
		}
	};
}
