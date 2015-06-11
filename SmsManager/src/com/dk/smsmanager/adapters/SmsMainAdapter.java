package com.dk.smsmanager.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dk.smsmanager.R;
import com.dk.smsmanager.dao.SmsMsg;

/**
 * Adapter Class for recent SMS list 
 * @author Dhananjay Kumar
 *
 */
public class SmsMainAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<SmsMsg> sms_msg_list = null;

	public SmsMainAdapter(Context mContext, ArrayList<SmsMsg> sms_msg_list) {

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

		holder.tv_contact_name.setText(msg.getName());
		holder.tv_sms_date.setText(msg.getDate());
		holder.tv_sms_body.setText(msg.getMsg());
		// setting bold text for unread msgs
		if(!msg.getReadStatus().equalsIgnoreCase("1"))
			holder.tv_sms_body.setTypeface(null, Typeface.BOLD);
		else
			holder.tv_sms_body.setTypeface(null, Typeface.NORMAL);
		// setting white background color if address is in contact 
		if(msg.isContact()){
			convertView.setBackgroundColor(Color.WHITE);
		} else {
			convertView.setBackgroundColor(Color.LTGRAY);
		}

		return convertView;
	}

	class ViewHolder {
		TextView tv_contact_name;
		TextView tv_sms_date;
		TextView tv_sms_body;
	}

}
