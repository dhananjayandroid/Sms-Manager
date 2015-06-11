package com.dk.smsmanager.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dk.smsmanager.R;
import com.dk.smsmanager.dao.SmsMsg;
import com.dk.smsmanager.utils.CommonUtils;


/**
 * Adapter Class for SMS list of a sender/receiver
 * @author Dhananjay Kumar
 *
 */
public class SmsListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<SmsMsg> sms_msg_list = null;


	public SmsListAdapter(Context mContext, ArrayList<SmsMsg> sms_msg_list) {

		this.mContext = mContext;
		this.sms_msg_list = sms_msg_list;
	}

	@Override
	public int getCount() {
		return sms_msg_list.size();
	}

	@Override
	public SmsMsg getItem(int position) {
		return sms_msg_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_sms_main, parent, false);
			holder = new ViewHolder();
			holder.tv_contact_name = (TextView) convertView.findViewById(R.id.tv_contact_name);
			holder.tv_sms_date = (TextView) convertView.findViewById(R.id.tv_sms_date);
			holder.tv_sms_body = (TextView) convertView.findViewById(R.id.tv_sms_body);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SmsMsg msg = sms_msg_list.get(position);
		new SetSMSRead().execute(msg);
		holder.tv_sms_date.setText(msg.getDate());
		holder.tv_sms_body.setText(msg.getMsg()/*+ " " + msg.getThreadId()+ " " + msg.getType()*/);


		if(msg.getType().equalsIgnoreCase("1"))
			holder.tv_contact_name.setText(msg.getName());
		else
			holder.tv_contact_name.setText("Me");

		return convertView;
	}


	/**
	 * Async to set msg as read in backGround thread
	 * @author Dhananjay Kumar
	 *
	 */
	private class SetSMSRead extends AsyncTask<SmsMsg, Void, String> {
		@Override
		protected String doInBackground(SmsMsg... msgs) {
			String response = "";
			SmsMsg msg = msgs[0];
			Log.e("Read: msgId: ", msg.get_id());
			CommonUtils.setMsgRead(mContext, msg.get_id(), msg.getThreadId()); 
			return response;
		}

		@Override
		protected void onPostExecute(String result) {}
	}

	class ViewHolder {
		TextView tv_contact_name;
		TextView tv_sms_date;
		TextView tv_sms_body;
	}

}
