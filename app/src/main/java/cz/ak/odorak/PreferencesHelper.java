package cz.ak.odorak;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {
	protected final String prefsScopeName;
	protected Context context;
	protected SharedPreferences settings;
	protected static PreferencesHelper instance;

	private PreferencesHelper(Context ctx) {
		this.context = ctx.getApplicationContext();
		this.prefsScopeName = context.getPackageName() + "_preferences";
		this.settings = context.getSharedPreferences(prefsScopeName, 0);
	}

	public static PreferencesHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new PreferencesHelper(ctx);
		}
		return instance;
	}

	private String getPref(String name) {
		String prefStr = settings.getString(name, "");
		return prefStr;
	}

	private boolean getPrefBoolean(String name, boolean default_value) {
		boolean prefBool = settings.getBoolean(name, default_value);
		return prefBool;
	}

	private void setPref(String name, String value) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public String getOdorikPWD() {
		return getPref("odorik_pwd");
	}

	public String getOdorikPIN() {
		String pin = getPref("odorik_pin");
		if (pin.length() == 0) {
			return "ODORIK_PIN";
		} else {
			return pin;
		}
	}

	public String getOdorikMode() {
		return getPref("odorik_mode");
	}

	public String getOdorikID() {
		return getPref("odorik_id");
	}

	public String getOdorikNumber() {
		String ret = getPref("odorik_number");
		ret = Helper.getIntPhoneNum(ret);
		return ret;
	}

	public String getOdorikLine() {
		return getPref("odorik_line").replaceAll("\\*", "");
	}

	public String getMobileNumber() {
		String ret = getPref("mobile_number");
		ret = Helper.getIntPhoneNum(ret);
		return ret;
	}

	public String getOdorikSkypeName() {
		return getPref("skype_name");
	}

	public String getOdorikError() {
		return getPref("odorik_error");
	}

	public String getLastAction() {
		return getPref("last_action");
	}

	public String getLastCalledNumber() {
		return getPref("last_called_number");
	}

	public String getLastTelState() {
		return getPref("last_tel_state");
	}

	public long getSkipTimeout() {
		try {
			return Long.parseLong(getPref("skip_timeout"));
		} catch (Exception e) {
			return 0;
		}
	}

	public String getBypassList() {
		String ret = getPref("bypass_list");
		ret.replaceAll("\\s", "");
		return "#" + ret + "#";
	}

	public boolean getCsipsimple() {
		return getPrefBoolean("csipsimple", false);
	}

	public boolean getAutoCall() {
		return getPrefBoolean("auto_call", true);
	}

	public int getCsipsimpleCounter() {
		String retStr = getPref("csipsimple_counter");
		int ret = 0;
		if (retStr.length() > 0) {
			ret = Integer.parseInt(retStr);
		}
		return ret;
	}

	public int getDialerLogSize() {
		String retStr = getPref("dialer_log_size");
		int ret = 0;
		if (retStr.length() > 0) {
			ret = Integer.parseInt(retStr);
		} else {
			ret = 3;
		}
		if (ret < 0) {
			return 3;
		} else {
			return ret;
		}
	}

	public int getDialerListSize() {
		String retStr = getPref("dialer_list_size");
		int ret = 0;
		if (retStr.length() > 0) {
			ret = Integer.parseInt(retStr);
		} else {
			ret = 10;
		}
		if (ret < 0) {
			return 10;
		} else {
			return ret;
		}
	}

	public void setCsipsimpleCounter(int val) {
		setPref("csipsimple_counter", "" + val);
	}

	public void setOdorikMode(String val) {
		setPref("odorik_mode", val);
	}

	public void setLastAction(String val) {
		setPref("last_action", val);
	}

	public void setLastCalledNumber(String val) {
		setPref("last_called_number", val);
	}

	public void setLastTelState(String val) {
		setPref("last_tel_state", val);
	}

	public void setSkipTimeout(long val) {
		setPref("skip_timeout", "" + val);
	}

}
