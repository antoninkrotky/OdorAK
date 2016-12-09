package cz.ak.odorak;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class GAHelper {
	public static final String CATEGORY_UI = "ui_action";
	public static final String CATEGORY_RESOURCES = "resources";
	public static final String CATEGORY_APP_LIFECYCLE = "app_lifecycle";
	public static final String CATEGORY_RECEIVER = "receiver";

	private Context mCtx;
	private InfoHelper mInfo;
	private PreferencesHelper mPrefs;
	private EasyTracker mET;
	static private GAHelper mInstance;

	private GAHelper(Context ctx) {
		this.mCtx = ctx.getApplicationContext();
		this.mInfo = new InfoHelper(mCtx);
		this.mPrefs = PreferencesHelper.getInstance(mCtx);
		this.mET = EasyTracker.getInstance(mCtx);
	}

	public static GAHelper getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new GAHelper(ctx);
		}
		return mInstance;
	}

	private String getDimOdorikMode(String mode) {
		String ret;
		if (mode == null || mode.length() == 0) {
			ret = mPrefs.getOdorikMode();
		} else {
			ret = mPrefs.getOdorikMode() + '#' + mode;
		}
		return ret;
	}

	private String getDimNetwork() {
		String ret = mInfo.getPhoneInfo().get("mobile_sim_operator") + "#"
				+ mInfo.getPhoneInfo().get("mobile_network_operator") + "#"
				+ mInfo.getPhoneInfo().get("mobile_network_country") + "#"
				+ mInfo.getPhoneInfo().get("mobile_is_roaming");
		return ret;
	}

	private String getDimConnection() {
		String ret = mInfo.getPhoneInfo().get("active_connection_type_cons");
		return ret;
	}

	private String getDimError(String isError) {
		if (isError != null) {
			return isError;
		} else {
			return "null";
		}
	}

	public void logEvent(String category, String action, String label,
			Long value, String mode, String isError) {
		mET.send(MapBuilder.createEvent(category, action, label, value)
				.set(Fields.customDimension(1), getDimOdorikMode(mode))
				.set(Fields.customDimension(2), getDimError(isError))
				.set(Fields.customDimension(3), getDimNetwork())
				.set(Fields.customDimension(4), getDimConnection()).build());
	}

	public void logTiming(String category, Long intervalMs, String name,
			String label, String mode, String isError) {
		mET.send(MapBuilder.createTiming(category, intervalMs, name, label)
				.set(Fields.customDimension(1), getDimOdorikMode(mode))
				.set(Fields.customDimension(2), getDimError(isError))
				.set(Fields.customDimension(3), getDimNetwork())
				.set(Fields.customDimension(4), getDimConnection()).build());
	}

	public void logActivityStart(Activity activity) {
		mET.activityStart(activity);
	}

	public void logActivityStop(Activity activity) {
		mET.activityStop(activity);
	}
}
