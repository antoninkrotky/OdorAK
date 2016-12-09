package cz.ak.odorak;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import cz.ak.odorak.api.OdorikAPI;

public class LoggerHelper {
	private static LoggerHelper instance = null;
	private static String log;
	private static SimpleDateFormat dateFormat;
	private static PreferencesHelper prefs;
	private static Context context;
	private static boolean enabled = true;
	private static ArrayList<String> mToMask;

	protected LoggerHelper() {
	}

	public void setEnabled(boolean val) {
		enabled = val;
	}

	@SuppressLint("SimpleDateFormat")
	public static LoggerHelper getInstance(Context ctx) {
		if (instance == null) {
			context = ctx.getApplicationContext();
			prefs = PreferencesHelper.getInstance(context);
			instance = new LoggerHelper();
			log = "";
			dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
			//
			mToMask = new ArrayList<String>();
			mToMask.add(prefs.getOdorikPWD());
			mToMask.add(prefs.getOdorikPIN());
		}
		return instance;
	}

	public void println(String str, boolean addTime) {
		if (enabled) {
			if (addTime) {
				Date date = new Date();
				log = log + (dateFormat.format(date)) + "\n";
			}
			log = log + str + "\n";
			Log.i("Logger", str);
		}
	}

	public void println(String str) {
		println(str, true);
	}
	
	public void logVersion() {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			println("Package: "+ pInfo.packageName,false);
			println("Version: "+ pInfo.versionName,false);
		} catch (NameNotFoundException e) {
			// do nothing
		}		
	}

	public void clearSavedLog() {
		// prefs.setPref("last_log", "--- Action start ---");
		log = new String("");
		logVersion();
	}

	public String getLog() {
		String ret;
		ret = new String(log);
		// mask
		for (String s : mToMask) {
			if (s != null && s.length() > 0) {
				ret = ret.replaceAll(s, "hidden(lenght:" + s.length() + ")");
			}
		}
		return ret;
	}

	public void sendToGoogle() {
		try {
			String logToSent = getLog();
			OdorikAPI odorikAPI = new OdorikAPI(null, null, context);
			odorikAPI.logGoogle(logToSent);
		} catch (Exception e) {
			println("GoogleLog - Exception:\n" + Helper.stackTraceToString(e));
		}
	}
}
