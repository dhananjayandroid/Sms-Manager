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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dk.smsmanager.adapters.SmsListAdapter;
import com.dk.smsmanager.dao.SmsMsg;
import com.dk.smsmanager.ui.LoadMoreListView;
import com.dk.smsmanager.ui.LoadMoreListView.OnLoadMoreListener;
import com.dk.smsmanager.ui.SmsComposer;
import com.dk.smsmanager.utils.AppConstants;
import com.dk.smsmanager.utils.CommonUtils;

/**
 * This Activity class is used to list all SMS of a sender/receiver
 * @author Dhananjay Kumar
 *
 */
public class SmsListActivity extends ListActivity implements OnClickListener{
	private ArrayList<SmsMsg> listSmsMsg = new ArrayList<SmsMsg>();
	private SmsListAdapter smsListAdapter;
	private TextView tv_number;
	private SmsMsg smsMsg;
	private String address;
	private long start_date;
	private SMSUpdateReceiver smsUpdtdReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_list);
		start_date = System.currentTimeMillis(); // time in milli-sec to perform paginated query on SMS db
		intUI();
		getIntentData();

		smsListAdapter = new SmsListAdapter(this, listSmsMsg);
		setListAdapter(smsListAdapter);
		setListenersOnViews();
	}

	/**
	 * Get data from getIntent() 
	 */
	private void getIntentData() {
		String intentAction = getIntent() == null ? null : getIntent().getAction();
		if(getIntent().hasExtra("smsMsg")){
			smsMsg = (SmsMsg) getIntent().getSerializableExtra("smsMsg");
			address = smsMsg.getAddress();
			tv_number.setText(smsMsg.getName());
		} else if (!TextUtils.isEmpty(intentAction) && (Intent.ACTION_SENDTO.equals(intentAction)
				|| Intent.ACTION_SEND.equals(intentAction))) {
			if(getIntent().getStringExtra("address") != null){
				address = getIntent().getStringExtra("address") == null ? null : getIntent().getStringExtra("address");
				tv_number.setText(CommonUtils.getContactName(this, address));
			}
			// TODO: Handle incoming SEND and SENDTO intents by pre-populating UI components
			Toast.makeText(this, "Handle SEND and SENDTO intents: " + getIntent().getDataString() + " " +getIntent().getData().toString(),
					Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Initialize UI Components
	 */
	private void intUI(){
		tv_number = (TextView) findViewById(R.id.tv_number);
		findViewById(R.id.iv_new).setOnClickListener(this);
	}
	
	private void setListenersOnViews() {
		((LoadMoreListView)getListView()).setOnLoadMoreListener(new OnLoadMoreListener() {

			public void onLoadMore() {
				loadSmsList(false);
			}
		});
	}

	/**
	 * Setting different listeners on Views
	 */
	@Override
	protected void onResume() {
		super.onResume();
		listSmsMsg.clear();
		loadSmsList(true);
		CommonUtils.checkAndAskForDefaultApp(this);
		smsUpdtdReceiver = new SMSUpdateReceiver();
		registerReceiver(smsUpdtdReceiver, new IntentFilter(AppConstants.SMS_RCVD_BROADCAST_ACTION));
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
	public void loadSmsList(boolean showDialog){
		new LoadSMSAsync(showDialog).execute();
	}

	/**
	 * Load SMS list from SMS db
	 * @return SMS list
	 */
	public ArrayList<SmsMsg> loadSms(String address) {
		ArrayList<SmsMsg> tempSmsList = new ArrayList<SmsMsg>();
		try {
			Log.e("start_date", ""+start_date);
			Cursor cursor = this.getContentResolver().query(AppConstants.SMS_URI, new String[] { "_id", "thread_id", "address", "date", "body",
				"type", "read"}, "address = '"+ address+"' AND date <'"+start_date+"'", null, "date desc LIMIT 30" );
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					tempSmsList.add(new SmsMsg(this, cursor));
				}
			}
			if(cursor != null) cursor.close();
			if(tempSmsList != null) 
				start_date =tempSmsList.get(tempSmsList.size()-1).getDateMillisec();
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
			dialog = new ProgressDialog(SmsListActivity.this);
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
			if(address != null)
				return loadSms(address);
			else
				return null;
		}

		@Override
		protected void onPostExecute(ArrayList<SmsMsg> result) {
			if(result != null)	listSmsMsg.addAll(result);
			if(smsListAdapter != null) smsListAdapter.notifyDataSetChanged();
			((LoadMoreListView)getListView()).onLoadMoreComplete();
			if(dialog != null && dialog.isShowing())
				dialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_new:
			new SmsComposer().showComposeMsgDialog(this, address);
			break;

		default:
			break;
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
			start_date = System.currentTimeMillis();
			listSmsMsg.clear();
			loadSmsList(true);
		}
	};

}
