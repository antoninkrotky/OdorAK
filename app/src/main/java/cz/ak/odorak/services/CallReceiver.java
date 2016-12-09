package cz.ak.odorak.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.analytics.tracking.android.Tracker;

import cz.ak.odorak.GAHelper;
import cz.ak.odorak.Helper;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.TimerHelper;

public class CallReceiver extends BroadcastReceiver {
	public static final int INTENT_VALIED_FOR_MILISEC = 11000;
	Tracker ga;

	@Override
	public void onReceive(Context context, Intent intent) {
		TimerHelper mTime = new TimerHelper();
		mTime.start("CallReceiver");
		// Try to read the phone number from previous receivers.
		String action = intent.getAction();
		String phoneNumberOrig = getResultData();
		if (phoneNumberOrig == null) {
			// We could not find any previous data. Use the original phone
			// number in this case.
			phoneNumberOrig = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		}
		String phoneNumber = phoneNumberOrig.replaceAll("\\s", "");
		PreferencesHelper pref = PreferencesHelper.getInstance(context);
		String odorik_mode = pref.getOdorikMode();
		long skip_timeout = pref.getSkipTimeout();

		// vypnuto NEBO na zaklade seznamu vyjimek NEBO moje vlastni voleni
		if (odorik_mode.equals("off")
				|| Helper.isBypassed(phoneNumber, pref.getBypassList())
				|| skip_timeout > System.currentTimeMillis()
				|| Helper.isSpecialNumber(phoneNumber)
				|| PhoneNumberUtils.isEmergencyNumber(phoneNumber)
				|| action == null
				|| !Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
			// do nothing
			int cSipsimpleCounter = pref.getCsipsimpleCounter();
			if (cSipsimpleCounter > 0) {
				pref.setCsipsimpleCounter(cSipsimpleCounter - 1);
			} else {
				// next time OdorAK will start
				pref.setSkipTimeout(0);
			}
			setResultData(phoneNumber);
		} else {
			Intent myIntent = new Intent(context,
					cz.ak.odorak.activity.dialer.DialerActivity.class);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			myIntent.putExtra("cz.ak.odorak.phonenumber", phoneNumber);
			long validto = System.currentTimeMillis()
					+ INTENT_VALIED_FOR_MILISEC;
			myIntent.putExtra("cz.ak.odorak.validto", "" + validto);
			context.startActivity(myIntent);
			setResultData(null);
		}
		//
		long duration = mTime.end("CallReceiver");
		GAHelper.getInstance(context).logTiming(GAHelper.CATEGORY_RECEIVER,
				duration, "CallReceiver",
				"CallReceiver_len:" + phoneNumber.length(), null, null);
	}
}