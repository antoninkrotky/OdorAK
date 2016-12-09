package cz.ak.odorak.activity.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.GoogleAnalytics;

import cz.ak.odorak.GAHelper;
import cz.ak.odorak.R;
import cz.ak.odorak.TimerHelper;

public class PreferencesActivity extends SherlockPreferenceActivity {
	TimerHelper mTime;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTime = new TimerHelper();
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		userPrefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener () {
		  @Override
		  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
		      String key) {
		    if (key.equals("integration_type")) {
		      GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(sharedPreferences.getBoolean(key, false));
		    } else {
		    // Any additional changed preference handling.
		    }
		  }
		});				
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		mTime.start("activity");
		super.onStart();
		GAHelper.getInstance(getApplicationContext()).logActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		GAHelper.getInstance(getApplicationContext()).logActivityStop(this);
		long duration = mTime.end("activity");
		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration,
				"PreferencesActivity", null, null, null);
	}
}
