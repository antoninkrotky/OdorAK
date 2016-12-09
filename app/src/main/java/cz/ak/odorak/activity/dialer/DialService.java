package cz.ak.odorak.activity.dialer;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;
import cz.ak.odorak.Helper;
import cz.ak.odorak.InfoHelper;
import cz.ak.odorak.LoggerHelper;
import cz.ak.odorak.MyCallLog;
import cz.ak.odorak.MyContactList;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.R;
import cz.ak.odorak.api.OdorakAPIException;
import cz.ak.odorak.api.OdorikAPI;

public class DialService {
	public final static long MAX_SKIP_TIME_MILISEC = 11000;
	Context mCtx;
	OdorikAPI mOdorikAPI;
	PreferencesHelper mPrefs;
	String mOdorik_id;
	String mOdorik_pwd;
	String mOdorik_number;
	String mOdorik_line;
	String mMobile_number;
	String mSkype_name;
	String mOdorik_pin;
	String mOdorik_mode;
	String mOdorik_error;
	String mBypass_list;
	boolean mCsipsimple;
	LoggerHelper l;

	public DialService(Context ctx) {
		this.mCtx = ctx;
		this.l = LoggerHelper.getInstance(mCtx);
		this.mPrefs = PreferencesHelper.getInstance(mCtx);
		this.mOdorik_id = mPrefs.getOdorikID();
		this.mOdorik_pwd = mPrefs.getOdorikPWD();
		this.mOdorik_number = mPrefs.getOdorikNumber();
		this.mOdorik_line = mPrefs.getOdorikLine();
		this.mMobile_number = mPrefs.getMobileNumber();
		this.mSkype_name = mPrefs.getOdorikSkypeName();
		this.mOdorik_pin = mPrefs.getOdorikPIN();
		this.mOdorik_mode = mPrefs.getOdorikMode();
		this.mOdorik_error = mPrefs.getOdorikError();
		this.mBypass_list = mPrefs.getBypassList();
		this.mCsipsimple = mPrefs.getCsipsimple();
		this.mOdorikAPI = new OdorikAPI(mOdorik_id, mOdorik_pwd, mCtx);
	}

	private void logDetail() {
		InfoHelper info = InfoHelper.getInstance(mCtx);
		for (Map.Entry<String, String> entry : info.getAllMap().entrySet()) {
			l.println(entry.getKey() + ":" + entry.getValue(), false);
		}
		//
		l.println("OdorikID: " + mOdorik_id, false);
		l.println("OdorikPWD: " + mOdorik_pwd, false);
		l.println("OdorikNUMBER: " + mOdorik_number, false);
		l.println("OdorikLINE: " + mOdorik_line, false);
		l.println("MobileNUMBER: " + mMobile_number, false);
		l.println("BypassList: " + mBypass_list, false);
		l.println("CSipSimple: " + mCsipsimple, false);
		l.println("CanActionDial: " + info.canActionDial(), false);
		l.println("CanActionCall: " + info.canActionCall(), false);
		l.println("CanGMS: " + info.canOutputGSMCall(), false);
	}

	private void storeActionForCallEnd(String callingNumber, String mode) {
		mPrefs.setLastAction(mode);
		mPrefs.setLastCalledNumber(callingNumber);
	}

	public Pair<String, String> dial(String number, boolean bypassEnabled) {
		return dial(number, mOdorik_mode, bypassEnabled);
	}

	public Pair<String, String> dial(String number, String mode,
			boolean bypassEnabled) {
		return dial_process(number, mode, bypassEnabled);
	}

	private Pair<String, String> dial_process(String callingNumber,
			String mode, boolean bypassEnabled) {
		String errorType = "no_error";
		String retStr = mCtx.getString(R.string.toast_unknown_mode);
		String resp;
		long startTime = System.currentTimeMillis();
		// clear log
		logDetail();
		l.println("Calling: " + callingNumber, false);
		// remove spaces
		callingNumber = callingNumber.replaceAll("\\s", "");
		l.println("Calling(no_space): " + callingNumber, false);
		l.println("Calling mode: " + mode, false);
		// bypass
		boolean bypased = false;
		if (bypassEnabled
				&& Helper.isBypassed(callingNumber, mPrefs.getBypassList())) {
			l.println("Bypassing. Calling " + callingNumber + " bypassList "
					+ mPrefs.getBypassList());
			mode = "off";
			bypased = true;
		}

		/*
		 * // Turn off check on Internet access on main thread
		 * StrictMode.ThreadPolicy policy = new
		 * StrictMode.ThreadPolicy.Builder() .permitAll().build();
		 * StrictMode.setThreadPolicy(policy);
		 */

		// dial
		try {
			if (mode.equals("redirect")) {
				// checkRemotelyEnabled();
				resp = processRedirect(callingNumber);
				storeActionForCallEnd(callingNumber, mode);
				processDial(mOdorik_number);
				retStr = mCtx.getString(R.string.toast_redirect_ok);
			} else if (mode.equals("callback")) {
				// checkRemotelyEnabled();
				resp = processCallBack(callingNumber, mMobile_number);
				storeActionForCallEnd(callingNumber, mode);
				retStr = mCtx.getString(R.string.toast_callback_ok);
			} else if (mode.equals("callback_contra")) {
				// checkRemotelyEnabled();
				resp = processCallBack(mMobile_number, callingNumber);
				storeActionForCallEnd(callingNumber, mode);
				retStr = mCtx.getString(R.string.toast_callback_ok);
			} else if (mode.equals("callbacksms")) {
				processCallBackSMS(callingNumber);
				storeActionForCallEnd(callingNumber, mode);
				retStr = mCtx.getString(R.string.toast_callbacksms_ok);
			} else if (mode.equals("callbackskype")) {
				resp = processCallBackSkype(callingNumber);
				// storeActionForCallEnd(callingNumber, mode);
				MyContactList conList = MyContactList.getInstance(mCtx
						.getApplicationContext());
				// insert into call log
				ContactVO lastCalledDetail = conList.getDetail(callingNumber);
				if (lastCalledDetail == null) {
					lastCalledDetail = new ContactVO();
				}
				lastCalledDetail._ID = null;
				lastCalledDetail.hisState = ContactVO.OUTGOING;
				lastCalledDetail.number = callingNumber;
				MyCallLog callLog = MyCallLog.getInstance(mCtx
						.getApplicationContext());
				callLog.insertCallLog(lastCalledDetail);
				//
				retStr = mCtx.getString(R.string.toast_callbackskype_ok);
			} else if (mode.equals("off")) {
				processDial(callingNumber);
				if (bypased) {
					retStr = mCtx.getString(R.string.toast_off_bypass_list_ok);
				} else {
					retStr = mCtx.getString(R.string.toast_off_ok);
				}
			} else if (mode.equals("getCredit")) {
				retStr = getCredit();
			}
		} catch (OdorakAPIException e) {
			// error in api call
			l.println("Exception:\n" + Helper.stackTraceToString(e));
			if (mOdorik_error.equals("ignore") && !mode.startsWith("get")) {
				mode = "off";
				retStr = e.getMessage();
				try {
					processDial(callingNumber);
					errorType = "error_ignore_alert";
				} catch (Exception e2) {
					retStr = e2.getMessage();
					errorType = "error_terminate_alert";
				}
			} else {
				retStr = e.getMessage();
				errorType = "error_terminate_alert";
			}
		} catch (Exception e) {
			// chyba primo z api bez hlasky (nejspise v siti)
			l.println("Exception:\n" + Helper.stackTraceToString(e));
			if (mOdorik_error.equals("ignore") && !mode.startsWith("get")) {
				mode = "off";
				retStr = mCtx.getString(R.string.toast_off_bypass_net_error_ok);
				try {
					processDial(callingNumber);
					errorType = "error_ignore_toast";
				} catch (Exception e2) {
					retStr = e2.getMessage();
					errorType = "error_terminate_alert";
				}
				errorType = "error_ignore_toast";
			} else {
				retStr = mCtx.getString(R.string.toast_off_bypass_net_error);
				errorType = "error_terminate_toast";
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		l.println("Call init. duration(milisec): " + duration);
		l.println(retStr, false);
		return new Pair<String, String>(errorType, retStr);
	}

	private String processRedirect(String callingNumber) throws Exception {
		callingNumber = callingNumber.replaceAll("\\+", "00");
		String res = mOdorikAPI.setRoutes(mOdorik_number, mMobile_number,
				callingNumber, "true");
		return res;
	}

	@SuppressWarnings("unused")
	private void processRedirect_old(String callingNumber) throws Exception {
		String res;
		// delete all existing
		res = mOdorikAPI.getParRing(mOdorik_number);
		for (String s : Helper.getParRing(res)) {
			mOdorikAPI.delParRing(mOdorik_number, s);
		}
		// add calling number
		mOdorikAPI.setParRing(mOdorik_number, callingNumber);
		// check if only one calling is present
		res = mOdorikAPI.getParRing(mOdorik_number);
		ArrayList<String> resArr = Helper.getParRing(res);
		if (resArr.size() != 1) {
			throw new RuntimeException(
					"Final check. Not only just 1 parallel ringing set. Actual: "
							+ resArr.size());
		}
		String callingNumberInt = Helper.getIntPhoneNum(callingNumber);
		l.println("CallingInt: " + callingNumberInt, false);
		for (String s : Helper.getParRing(res)) {
			if (!s.matches(callingNumberInt)) {
				throw new RuntimeException(
						"Final check. Parallel ringing not set correctly. Expected "
								+ callingNumberInt + " but set " + s);
			}
		}
	}

	private String processCallBack(String callingNumber, String mobile_number)
			throws Exception {
		// order redirect
		String ret = mOdorikAPI.orderCallBack(mobile_number, callingNumber,
				mOdorik_line);
		return ret;
	}

	private String processCallBackSkype(String callingNumber) throws Exception {
		// order redirect
		String ret = mOdorikAPI.orderCallBack("skype:" + mSkype_name,
				callingNumber, mOdorik_line);
		return ret;
	}

	private void processCallBackSMS(String callingNumber) {
		callingNumber = callingNumber.replaceAll("\\s", "");
		callingNumber = callingNumber.replaceAll("\\+", "00");
		// prepare sms
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
				+ "+420773080830"));
		String message = mOdorik_pin + "#" + mMobile_number + "#"
				+ callingNumber;
		intent.putExtra("sms_body", message);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(intent);
	}

	private void processDial(String callingNumber) {
		if (mCsipsimple) {
			mPrefs.setCsipsimpleCounter(1);
			mPrefs.setSkipTimeout(System.currentTimeMillis()
					+ MAX_SKIP_TIME_MILISEC * 2);
		} else {
			mPrefs.setCsipsimpleCounter(0);
			mPrefs.setSkipTimeout(System.currentTimeMillis()
					+ MAX_SKIP_TIME_MILISEC);
		}
		//
		Uri uri = Uri.fromParts("tel", callingNumber, null);
		InfoHelper info = InfoHelper.getInstance(mCtx);
		if (info.canActionCall()) {
			l.println("processDial: ActionCall", false);
			Intent intent = new Intent(Intent.ACTION_CALL, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mCtx.startActivity(intent);
		} else if (info.canActionDial()) {
			l.println("processDial: ActionDial", false);
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mCtx.startActivity(intent);
		} else {
			l.println("processDial: none", false);
			throw new RuntimeException(
					"processDial: no dialling method supported");
		}
	}

	private String getCredit() throws Exception {
		String ret = mOdorikAPI.getCredit();
		return ret;
	}
}
