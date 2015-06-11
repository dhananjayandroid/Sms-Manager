package com.dk.smsmanager.dao;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;

import com.dk.smsmanager.utils.CommonUtils;

public class SmsMsg implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9161214163314625453L;
	private String _id = null;
	private String address = null;
	private String threadId = null;
	private String date = null;
	private long dateMillisec = 0l;
	private String msg = null;
	private String type = null;
	private String readStatus = null;
	private String name = null;
	private boolean isContact;
	private final SimpleDateFormat sdf = new SimpleDateFormat();
	private Date dt = new Date();

	public SmsMsg(Context context, Cursor cursor) {
		sdf.applyPattern("dd MMM h:mm a");
		dt.setTime(cursor.getLong(cursor.getColumnIndex("date")));
		
		this.set_id(cursor.getString(cursor.getColumnIndex("_id"))); 
		this.setAddress(cursor.getString(cursor.getColumnIndex("address")));
		this.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));
		this.setDateMillisec(cursor.getLong(cursor.getColumnIndex("date")));
		this.setDate(sdf.format(dt));
		this.setMsg(cursor.getString(cursor.getColumnIndex("body")));
		this.setType(cursor.getString(cursor.getColumnIndex("type"))); 
		this.setReadStatus(cursor.getString(cursor.getColumnIndex("read"))); 
		CommonUtils.addContactName(context, this);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(String status) {
		this.readStatus = status;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDateMillisec() {
		return dateMillisec;
	}

	public void setDateMillisec(long dateMillisec) {
		this.dateMillisec = dateMillisec;
	}

	public boolean isContact() {
		return isContact;
	}

	public void setIsContact(boolean isContact) {
		this.isContact = isContact;
	}

}
