package cz.ak.odorak.activity.log;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.ak.odorak.GAHelper;
import cz.ak.odorak.LoggerHelper;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.R;
import cz.ak.odorak.TimerHelper;

public class LogActivity extends SherlockActivity {
	PreferencesHelper mPrefs;
	LoggerHelper l;
	TimerHelper mTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTime = new TimerHelper();
		super.onCreate(savedInstanceState);

		setContentView(R.layout.log_fragment);
		l = LoggerHelper.getInstance(getApplicationContext());

		mPrefs = PreferencesHelper.getInstance(getApplicationContext());

		TextView textView = (TextView) findViewById(R.id.textView_log);
		textView.setText(getLastLog());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "LogActivity", null,
				null, null);
	}

	@Override
	public void onResume() {
		TextView textView = (TextView) findViewById(R.id.textView_log);
		textView.setText(getLastLog());
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_log, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_log_email:
			String email_addr = "antoninkrotky@gmail.com";
			String email_subj = "Log from OdorAK";
			String email_body = l.getLog();
			//
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email_addr });
			intent.putExtra(Intent.EXTRA_SUBJECT, email_subj);
			intent.putExtra(Intent.EXTRA_TEXT, email_body);
			intent.setType("message/rfc822");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				startActivity(Intent.createChooser(intent,
						getString(R.string.send_email)));
				GAHelper.getInstance(getApplicationContext()).logEvent(
						GAHelper.CATEGORY_UI, "onOptionsItemSelected", "logEmail",
						null, null, "false");				
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(getApplicationContext(),
						"There are no email clients installed.",
						Toast.LENGTH_SHORT).show();
				GAHelper.getInstance(getApplicationContext()).logEvent(
						GAHelper.CATEGORY_UI, "onOptionsItemSelected",
						"logEmail", null, null, "true");
			}
			return true;
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public String getLastLog() {
		// String lastLog = mPrefs.getPref("last_log");
		String lastLog = l.getLog();
		if (lastLog.length() == 0) {
			return getString(R.string.empty_log);
		} else {
			return lastLog;
		}
	}

}
