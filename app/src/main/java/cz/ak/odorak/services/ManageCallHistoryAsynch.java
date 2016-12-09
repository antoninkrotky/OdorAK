package cz.ak.odorak.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import cz.ak.odorak.AsyncResponse;
import cz.ak.odorak.GAHelper;
import cz.ak.odorak.Helper;
import cz.ak.odorak.MyCallLog;
import cz.ak.odorak.MyContactList;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.TimerHelper;
import cz.ak.odorak.activity.dialer.ContactVO;

public class ManageCallHistoryAsynch extends AsyncTask<String, String, String> {
	private static final int BEFORE_CALLLOG_ACTION_SLEEP_MILISEC = 2000;
	public AsyncResponse delegate = null;
	Context mCtx;
	PreferencesHelper mPrefs;
	TimerHelper mTime;

	public ManageCallHistoryAsynch(Context ctx) {
		mCtx = ctx;
		mPrefs = PreferencesHelper.getInstance(mCtx);
		mTime = new TimerHelper();
	}

	private void processCallLog(String action) {
		String last_action = mPrefs.getLastAction();
		String last_called_number = mPrefs.getLastCalledNumber();
		if (last_action.length() == 0 || last_called_number.length() == 0) {
			// stop procession
			return;
		}
		String odorik_number = mPrefs.getOdorikNumber();
		String mobile_number = mPrefs.getMobileNumber();
		//
		String mask_odorik_number = Helper.maskNumber(odorik_number, 9);
		String mask_mobile_number = Helper.maskNumber(mobile_number, 9);
		//
		MyCallLog callLog = MyCallLog.getInstance(mCtx.getApplicationContext());
		ContactVO lastCallLog = callLog.getLastCallLog();
		ContactVO newCallLog = new ContactVO(lastCallLog);

		MyContactList conList = MyContactList.getInstance(mCtx
				.getApplicationContext());
		ContactVO lastCalledDetail = conList.getDetail(last_called_number);

		if (lastCalledDetail != null) {
			newCallLog.name = lastCalledDetail.name;
			newCallLog.numberLabel = lastCalledDetail.numberLabel;
			newCallLog.number = last_called_number;
		} else {
			newCallLog.name = "";
			newCallLog.numberLabel = "";
			newCallLog.number = last_called_number;
		}

		if (lastCallLog == null
				|| !lastCallLog.number.endsWith(mask_mobile_number)
				&& !lastCallLog.number.endsWith(mask_odorik_number)) {
			// do nothing
		} else if (action.equals("delete")
				&& last_action.startsWith("callback")) {
			callLog.deleteCallLog(lastCallLog);
		} else if (last_action.equals("redirect")) {
			callLog.updateLCallLog(newCallLog);
		} else if (last_action.equals("callback")) {
			newCallLog.hisState = ContactVO.OUTGOING;
			callLog.updateLCallLog(newCallLog);
		} else if (last_action.equals("callback_contra")) {
			newCallLog.hisState = ContactVO.OUTGOING;
			callLog.updateLCallLog(newCallLog);
		} else if (last_action.equals("callbacksms")) {
			newCallLog.hisState = ContactVO.OUTGOING;
			callLog.updateLCallLog(newCallLog);
		} else if (last_action.equals("callbackskype")) {
			// never happens
		} else {
			// do nothing
		}
		mPrefs.setLastAction("");
		mPrefs.setLastCalledNumber("");
	}

	@Override
	protected String doInBackground(String... params) {
		// String number, String mode, boolean bypassEnabled
		String action = params[0];
		SystemClock.sleep(BEFORE_CALLLOG_ACTION_SLEEP_MILISEC);
		//
		mTime.start("processCallHistory");
		processCallLog(action);
		long duration = mTime.end("processCallHistoryAsynch");
		GAHelper.getInstance(mCtx).logTiming(GAHelper.CATEGORY_RESOURCES,
				duration, "processCallHistory", mPrefs.getLastAction(), null,
				null);
		return "true";
	}

	@Override
	protected void onPostExecute(String result) {
		// do nothing
	}

	@Override
	protected void onPreExecute() {
	}

}
