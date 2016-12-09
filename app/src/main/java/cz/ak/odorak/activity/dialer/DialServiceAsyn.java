package cz.ak.odorak.activity.dialer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import cz.ak.odorak.AsyncResponse;
import cz.ak.odorak.GAHelper;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.TimerHelper;

public class DialServiceAsyn extends AsyncTask<String, String, String> {
	public AsyncResponse delegate = null;
	Context mCtx;
	String mResErrorType = "";
	String mResMessage = "";
	String mMode = "";
	TimerHelper mTime;
	PreferencesHelper mPrefs;

	public DialServiceAsyn(Context ctx) {
		mCtx = ctx;
		mTime = new TimerHelper();
		mPrefs = PreferencesHelper.getInstance(mCtx);
	}

	@Override
	protected String doInBackground(String... params) {
		mTime.start("dial");
		// String number, String mode, boolean bypassEnabled
		String number = params[0];
		String mode = params[1];
		boolean bypass = Boolean.valueOf(params[2]);
		//
		DialService dialService = new DialService(mCtx);
		Pair<String, String> ret;
		if (mode == null || mode.length() == 0) {
			ret = dialService.dial(number, bypass);
		} else {
			ret = dialService.dial(number, mode, bypass);
			mMode = mode;
		}
		mResErrorType = ret.first;
		mResMessage = ret.second;
		//
		long duration = mTime.end("dial");
		GAHelper.getInstance(mCtx).logTiming(GAHelper.CATEGORY_RESOURCES,
				duration, "dialAsynch", null, mode, mResErrorType);
		return "" + ret;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			delegate.processFinish(mResMessage, mResErrorType, mMode);
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected void onPreExecute() {
	}
}
