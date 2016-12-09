package cz.ak.odorak;

import java.util.ArrayList;
import java.util.HashMap;

import cz.ak.odorak.activity.dialer.ContactVO;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

public class MyCallLog {

	private static Context mCtx;
	private static ContentResolver mCR;
	private static MyCallLog mInstance;
	private static MyContactList mContactList;

	protected MyCallLog() {
	}

	public static MyCallLog getInstance(Context ctx) {
		if (mInstance == null) {
			mCtx = ctx.getApplicationContext();
			mCR = mCtx.getContentResolver();
			mContactList = MyContactList.getInstance(mCtx);
			mInstance = new MyCallLog();
		}
		return mInstance;
	}

	public ContactVO getLastCallLog() {
		ArrayList<ContactVO> contacts = getLastItems(1);
		if (contacts.size() > 0) {
			ContactVO contact = contacts.get(0);
			return contact;
		} else {
			return null;
		}
	}

	private ContentValues convertFromContactVO(ContactVO data) {
		ContentValues ret = new ContentValues();
		if (data._ID != null) {
			ret.put(CallLog.Calls._ID, data._ID);
		}
		if (data.number != null) {
			ret.put(CallLog.Calls.NUMBER, data.number);
		}
		if (data.name != null) {
			ret.put(CallLog.Calls.CACHED_NAME, data.name);
		}
		if (data.hisState != -1) {
			ret.put(CallLog.Calls.TYPE, data.hisState);
		}
		if (data.numberLabel != null) {
			ret.put(CallLog.Calls.CACHED_NUMBER_LABEL, data.numberLabel);
		}
		return ret;
	}

	public void updateLCallLog(ContactVO data) {
		Uri allCalls = Uri.parse("content://call_log/calls");
		final String WHERE = CallLog.Calls._ID + " = " + data._ID;
		ContentValues updData = convertFromContactVO(data);
		ContentResolver cr = mCtx.getContentResolver();
		cr.update(allCalls, updData, WHERE, null);
	}

	public void insertCallLog(ContactVO data) {
		Uri allCalls = Uri.parse("content://call_log/calls");
		ContentValues insData = convertFromContactVO(data);
		insData.put(CallLog.Calls.DATE, System.currentTimeMillis());
		insData.put(CallLog.Calls.DURATION, 0);
		insData.put(CallLog.Calls.NEW, 1);
		ContentResolver cr = mCtx.getContentResolver();
		cr.insert(allCalls, insData);
	}

	public void deleteCallLog(ContactVO data) {
		Uri allCalls = Uri.parse("content://call_log/calls");
		final String WHERE = CallLog.Calls._ID + " = " + data._ID;
		ContentResolver cr = mCtx.getContentResolver();
		cr.delete(allCalls, WHERE, null);
	}

	public ArrayList<ContactVO> getLastItems(int limit) {
		ArrayList<ContactVO> ret = new ArrayList<ContactVO>();
		HashMap<String, String> map = new HashMap<String, String>();

		Uri allCalls = Uri.parse("content://call_log/calls");

		Cursor cur = mCR.query(allCalls, null, null, null, CallLog.Calls.DATE
				+ " DESC");

		Log.d("Historie", "Pocet: " + cur.getCount());

		if (cur.getCount() > 0) {
			while (cur.moveToNext() && ret.size() < limit) {
				ContactVO item = new ContactVO();
				item.name = cur.getString(cur
						.getColumnIndex(CallLog.Calls.CACHED_NAME));
				item.number = cur.getString(cur
						.getColumnIndex(CallLog.Calls.NUMBER));
				int type;
				if (cur.getString(cur
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE)) != null) {
					type = Integer.parseInt(cur.getString(cur
							.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE)));
				} else {
					type = -1;
				}
				String label = cur.getString(cur
						.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
				item.numberLabel = mContactList.getPhoneLabel(type, label);
				item.selected = false;
				String callType = cur.getString(cur
						.getColumnIndex(CallLog.Calls.TYPE));
				int dircode = Integer.parseInt(callType);
				switch (dircode) {
				case CallLog.Calls.OUTGOING_TYPE:
					item.hisState = ContactVO.OUTGOING;
					break;
				case CallLog.Calls.INCOMING_TYPE:
					item.hisState = ContactVO.INCOMING;
					break;
				case CallLog.Calls.MISSED_TYPE:
					item.hisState = ContactVO.MISSED;
					break;
				}
				item._ID = cur.getString(cur.getColumnIndex(CallLog.Calls._ID));
				//
				if (!map.containsKey(item.number)) {
					ret.add(item);
					map.put(item.number, item.name);
				}
			}
		}
		return ret;
	}
}
