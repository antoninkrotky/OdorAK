package cz.ak.odorak.activity.dialer;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import cz.ak.odorak.AsyncResponse;
import cz.ak.odorak.GAHelper;
import cz.ak.odorak.Helper;
import cz.ak.odorak.LoggerHelper;
import cz.ak.odorak.MyCallLog;
import cz.ak.odorak.MyContactList;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.R;
import cz.ak.odorak.TimerHelper;

public class ManageListAsyn extends AsyncTask<String, String, String> {
	public AsyncResponse delegate = null;
	Context mCtx;
	TimerHelper mTime;
	PreferencesHelper mPrefs;
	ArrayList<ContactVO> mList = new ArrayList<ContactVO>();
	MyCallLog mCallLog;
	MyContactList mContactList;
	String mFilter;
	LoggerHelper l;

	public ManageListAsyn(Context ctx, String filter) {
		mCtx = ctx;
		mTime = new TimerHelper();
		mPrefs = PreferencesHelper.getInstance(mCtx.getApplicationContext());
		mContactList = MyContactList.getInstance(mCtx.getApplicationContext());
		mCallLog = MyCallLog.getInstance(mCtx.getApplicationContext());
		mFilter = filter;
		l = LoggerHelper.getInstance(mCtx.getApplicationContext());
	}

	@Override
	protected String doInBackground(String... params) {
		mTime.start("dial");
		// String filter = params[0];
		//
		mTime.start("manageList");
		mList.clear();
		long duration;
		// add entered text
		mTime.start("manageListSingle");
		if (mFilter.matches("^[\\d\\s\\+]+$")) {
			ContactVO contact = mContactList.getDetail(mFilter);
			if (contact != null) {
				mList.add(contact);
			} else {
				mList.add(new ContactVO(
						mCtx.getString(R.string.contact_custom), mFilter, mCtx
								.getString(R.string.contact_custom), false,
						false, -1, null));
			}
		}
		duration = mTime.end("manageListSingle");
		GAHelper.getInstance(mCtx.getApplicationContext()).logTiming(
				GAHelper.CATEGORY_RESOURCES, duration, "manageListSingle",
				"manageListSingle_len:" + mFilter.length(), null, null);
		l.println("manageListSingle: " + duration + "ms", false);
		// add history
		mTime.start("manageListHistory");
		if (mFilter.length() == 0) {
			mList.addAll(mCallLog.getLastItems(mPrefs.getDialerLogSize()));
		}
		duration = mTime.end("manageListHistory");
		GAHelper.getInstance(mCtx.getApplicationContext()).logTiming(
				GAHelper.CATEGORY_RESOURCES, duration, "manageListHistory",
				"manageListHistory_len:" + mFilter.length(), null, null);
		l.println("manageListHistory: " + duration + "ms", false);
		// add rest
		mTime.start("manageListContactList");
		mList.addAll(mContactList.getContacts(mFilter,
				mPrefs.getDialerListSize() - mList.size()));
		duration = mTime.end("manageListContactList");
		GAHelper.getInstance(mCtx.getApplicationContext()).logTiming(
				GAHelper.CATEGORY_RESOURCES, duration, "manageListContactList",
				"manageListContactList_len:" + mFilter.length(), null, null);
		l.println("manageListContactList: " + duration + "ms", false);
		//
		Helper.clearSelected(mList);
		Helper.setSelected(mList, 0);
		//
		duration = mTime.end("manageList");
		GAHelper.getInstance(mCtx.getApplicationContext()).logTiming(
				GAHelper.CATEGORY_RESOURCES, duration, "manageList",
				"manageList_len:" + mFilter.length(), null, null);
		l.println("manageList: " + duration + "ms", false);
		return "true";
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			delegate.processFinishManageList(mFilter, mList);
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected void onPreExecute() {
	}

}
