package cz.ak.odorak.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import cz.ak.odorak.GAHelper;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.TimerHelper;

public class CallEndReceiver extends BroadcastReceiver {
	private Context mCtx;
	private PreferencesHelper mPrefs;

	@Override
	public void onReceive(Context context, Intent intent) {
		mCtx = context;
		mPrefs = PreferencesHelper.getInstance(mCtx.getApplicationContext());
		TimerHelper mTime = new TimerHelper();
		mTime.start("CallEndReceiver");

		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String last_state = mPrefs.getLastTelState();
		String action = "";
		if (state.equals("IDLE") && last_state.equals("OFFHOOK")) {
			action = "insert";
		} else if (state.equals("IDLE") && last_state.equals("RINGING")) {
			action = "delete";
		}

		if (action != null && action.length() > 0) {
			ManageCallHistoryAsynch callHisAsynch = new ManageCallHistoryAsynch(
					mCtx.getApplicationContext());
			callHisAsynch.delegate = null;
			callHisAsynch.execute(action);
		}

		mPrefs.setLastTelState(state);

		long duration = mTime.end("CallEndReceiver");
		GAHelper.getInstance(context)
				.logTiming(GAHelper.CATEGORY_RECEIVER, duration,
						"CallReceiver", last_state + "->" + state, null, null);

		// ringing call
		/*
		 * if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) { String
		 * incomingNumber = intent
		 * .getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER); msg +=
		 * ". Incoming number is " + incomingNumber; // pick up phone call }
		 */
		//

	}
}
