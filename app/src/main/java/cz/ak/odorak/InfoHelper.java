package cz.ak.odorak;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class InfoHelper {
	private static String mApplicationVersionName;
	private static int mApplicationVersionCode;
	private static String mApplicationPackageName;
	private static InfoHelper instance;
	private static Context mCtx;

	protected InfoHelper(Context ctx) {
		mCtx = ctx.getApplicationContext();
		try {
			PackageInfo pInfo = mCtx.getPackageManager().getPackageInfo(
					mCtx.getPackageName(), 0);
			mApplicationVersionName = pInfo.versionName;
			mApplicationVersionCode = pInfo.versionCode;
			mApplicationPackageName = pInfo.packageName;
		} catch (NameNotFoundException e) {
			// do nothing
		}
	}

	public static InfoHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new InfoHelper(ctx);
		}
		return instance;
	}

	public String getApplicationVersionName() {
		return mApplicationVersionName;
	}

	public String getApplicationVersionCode() {
		return "" + mApplicationVersionCode;
	}

	public String getApplicationPackageName() {
		return mApplicationPackageName;
	}

	public HashMap<String, String> getPhoneInfo() {
		HashMap<String, String> ret = new HashMap<String, String>();

		// get connection_type wifi vs. mobile
		ConnectivityManager cm = (ConnectivityManager) mCtx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			ret.put("active_connection_type", netInfo.getTypeName());
			ret.put("active_connection_subtype", netInfo.getSubtypeName());
		} else {
			ret.put("active_connection_type", "not_connected");
			ret.put("active_connection_subtype", "not_connected");
		}

		// get mobile connection type
		TelephonyManager telManager;
		telManager = (TelephonyManager) mCtx
				.getSystemService(Context.TELEPHONY_SERVICE);
		int cType = telManager.getNetworkType();
		String cTypeString;
		switch (cType) {
		case 1:
			cTypeString = "GPRS";
			break;
		case 2:
			cTypeString = "EDGE";
			break;
		case 3:
			cTypeString = "UMTS";
			break;
		case 8:
			cTypeString = "HSDPA";
			break;
		case 9:
			cTypeString = "HSUPA";
			break;
		case 10:
			cTypeString = "HSPA";
			break;
		default:
			cTypeString = "unknown";
			break;
		}
		ret.put("mobile_connection_type", cTypeString);
		ret.put("mobile_is_roaming", "" + telManager.isNetworkRoaming());
		ret.put("mobile_sim_operator", telManager.getSimOperatorName());
		ret.put("mobile_network_operator", telManager.getNetworkOperatorName());
		ret.put("mobile_network_country", telManager.getNetworkCountryIso());
		ret.put("mobile_line_number", telManager.getLine1Number());
		//
		if (ret.get("active_connection_type").equalsIgnoreCase("WIFI")
				|| ret.get("active_connection_type").equalsIgnoreCase(
						"not_connected")) {
			ret.put("active_connection_type_cons",
					ret.get("active_connection_type"));
		} else {
			ret.put("active_connection_type_cons",
					ret.get("mobile_connection_type"));
		}

		return ret;
	}

	public HashMap<String, String> getAllMap() {
		HashMap<String, String> ret = getPhoneInfo();
		ret.put("ApplicationPackageName", getApplicationPackageName());
		ret.put("ApplicationVersionName", getApplicationVersionName());
		ret.put("ApplicationVersionCode", getApplicationVersionCode());
		return ret;
	}

	public boolean canResolveIntent(final Intent intent) {
		final PackageManager packageManager = mCtx.getPackageManager();
		// final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private Boolean mCanActionCall = null;

	public boolean canActionCall() {
		if (mCanActionCall == null) {
			Intent intentActionCall = new Intent(Intent.ACTION_CALL);
			intentActionCall.setData(Uri.fromParts("tel", "1", null));
			mCanActionCall = canResolveIntent(intentActionCall);
		}
		return mCanActionCall;
	}

	private Boolean mCanActionDial = null;

	public boolean canActionDial() {
		if (mCanActionDial == null) {
			Intent intentActionCall = new Intent(Intent.ACTION_DIAL);
			intentActionCall.setData(Uri.fromParts("tel", "1", null));
			mCanActionDial = canResolveIntent(intentActionCall);
		}
		return mCanActionDial;
	}

	public boolean canOutputGSMCall() {
		return (canActionCall() || canActionDial());
	}

}
