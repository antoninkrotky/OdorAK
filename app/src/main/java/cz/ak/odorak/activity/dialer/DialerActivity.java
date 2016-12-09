package cz.ak.odorak.activity.dialer;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;

import cz.ak.odorak.AsyncResponse;
import cz.ak.odorak.GAHelper;
import cz.ak.odorak.Helper;
import cz.ak.odorak.InfoHelper;
import cz.ak.odorak.LoggerHelper;
import cz.ak.odorak.PreferencesHelper;
import cz.ak.odorak.R;
import cz.ak.odorak.TimerHelper;
import cz.ak.odorak.activity.log.LogActivity;
import cz.ak.odorak.activity.preferences.PreferencesActivity;
import cz.ak.odorak.services.CallReceiver;

public class DialerActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener, AsyncResponse {
	// final int MAX_ITEMS = 10;
	// final int HIS_ITEMS = 3;
	ListView mListView;
	ContactListArrayAdapter mAdapter;
	EditText mEditText;
	ArrayList<ContactVO> mList = new ArrayList<ContactVO>();
	LoggerHelper l;
	TimerHelper mTime;
	String mLastEditTextRefreshed = null;
	private static String mNextAction;

	private ArrayList<String> mOdorikModeListKey;
	private ArrayList<String> mOdorikModeListValue;
	private ArrayList<String> mOdorikModeListValueLong;
	PreferencesHelper mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mTime = new TimerHelper();
		mTime.start("onCreate");
		mNextAction = "none";
		l = LoggerHelper.getInstance(getApplicationContext());

		super.onCreate(savedInstanceState);

		// create odorik mode list
		String[] tmpOdorikModeListKey = getResources().getStringArray(
				R.array.odorik_mode_key);
		String[] tmpOdorikModeListValue = getResources().getStringArray(
				R.array.odorik_mode_value);
		String[] tmpOdorikModeListValueLong = getResources().getStringArray(
				R.array.odorik_mode_value_long);
		// add odorik_mode list
		mOdorikModeListKey = new ArrayList<String>();
		mOdorikModeListValue = new ArrayList<String>();
		mOdorikModeListValueLong = new ArrayList<String>();
		InfoHelper info = InfoHelper.getInstance(this);
		for (int i = 0; i < tmpOdorikModeListKey.length; i++) {
			if (info.canOutputGSMCall()
					|| !tmpOdorikModeListKey[i].equalsIgnoreCase("redirect")) {
				mOdorikModeListKey.add(tmpOdorikModeListKey[i]);
				mOdorikModeListValue.add(tmpOdorikModeListValue[i]);
				mOdorikModeListValueLong.add(tmpOdorikModeListValueLong[i]);

			}
		}

		setContentView(R.layout.callback_fragment);

		mPrefs = PreferencesHelper.getInstance(getApplicationContext());

		mEditText = (EditText) findViewById(R.id.callback_edittext);

		try {
			if (savedInstanceState != null) {
				String mEditTextValuePrev = savedInstanceState
						.getString("mEditText");
				mEditText.setText(mEditTextValuePrev);
			}
		} catch (Exception e) {
			// ignore
		}

		mEditText
				.setOnTouchListener(new LeftDrawableOnTouchListener(mEditText) {
					@Override
					public boolean onDrawableTouch(final MotionEvent event) {
						mEditText.setText("");
						GAHelper.getInstance(getApplicationContext()).logEvent(
								GAHelper.CATEGORY_UI, "deleteSearchText", "",
								null, null, null);
						return true;
					}
				});
		mEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				mAdapter.setNotifyOnChange(false);
				manageList(s.toString());
				mAdapter.notifyDataSetChanged();
				GAHelper.getInstance(getApplicationContext()).logEvent(
						GAHelper.CATEGORY_UI, "editSearchText",
						"editSearchText_len:" + s.toString().length(), null,
						null, null);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// is also done onResume when an intent is received
		manageList(mEditText.getText().toString());

		mList = new ArrayList<ContactVO>();

		mListView = (ListView) findViewById(R.id.callback_listview);

		mAdapter = new ContactListArrayAdapter(this, mList);

		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactVO selectedItem = (ContactVO) parent
						.getItemAtPosition(position);
				Helper.clearSelected(mList);
				int index = mList.indexOf(selectedItem);
				Helper.setSelected(mList, index);
				mAdapter.notifyDataSetChanged();
				view.showContextMenu(); // show context menu after short click
				GAHelper.getInstance(getApplicationContext()).logEvent(
						GAHelper.CATEGORY_UI, "itemContactClick",
						"itemContactClick_pos:" + position, null, null, null);
			}
		});

		registerForContextMenu(mListView);

		ArrayAdapter<String> odorikModeAdapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.sherlock_spinner_item,
				mOdorikModeListValue.toArray(new String[mOdorikModeListValue
						.size()]));

		odorikModeAdapter
				.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(odorikModeAdapter,
				this);
		getSupportActionBar().setSelectedNavigationItem(
				mOdorikModeListKey.indexOf(mPrefs.getOdorikMode()));
		//
		long duration = mTime.end("onCreate");
		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "onCreate", null,
				null, null);
		l.println("onCreate: " + duration + "ms", false);
	}

	@Override
	public void onStart() {
		mTime.start("onStart");
		mTime.start("activity");
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
		GAHelper.getInstance(getApplicationContext()).logActivityStart(this);
		long duration = mTime.end("onStart");
		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "onStart", null,
				null, null);
		l.println("onStart: " + duration + "ms", true);
	}

	private String getSelectedNumber() {
		for (ContactVO o : mList) {
			if (o.selected) {
				return o.number;
			}
		}
		return "";
	}

	private void dial(String number, String mode, boolean bypassEnabled) {
		// run asynch tack
		DialServiceAsyn dialAsyn = new DialServiceAsyn(getApplicationContext());
		dialAsyn.delegate = this;
		dialAsyn.execute(number, mode, "" + bypassEnabled);
	}

	@Override
	public void onResume() {
		mTime.start("onResume");
		super.onResume();
		// Get the message from the intent
		Intent intent = getIntent();
		// check if it's not intent from app chooser
		if (Intent.ACTION_CALL.equals(intent.getAction())
				|| "android.intent.action.CALL_PRIVILEGED"
						.equalsIgnoreCase(intent.getAction())) {
			Uri uri = intent.getData();
			String phoneNumber = uri.getSchemeSpecificPart();
			// resend intent
			Intent resendIntent = new Intent(this,
					cz.ak.odorak.activity.dialer.DialerActivity.class);
			//resendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//resendIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			resendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			resendIntent.putExtra("cz.ak.odorak.phonenumber", phoneNumber);
			long validto = System.currentTimeMillis()
					+ CallReceiver.INTENT_VALIED_FOR_MILISEC;
			resendIntent.putExtra("cz.ak.odorak.validto", "" + validto);
			startActivity(resendIntent);
			
			finish();
		}
		// process my intent
		String message = intent.getStringExtra("cz.ak.odorak.phonenumber");
		if (message != null && message.length() > 0) {
			intent.removeExtra("cz.ak.odorak.phonenumber");
			intent.putExtra("cz.ak.odorak.phonenumber.dial", "NOW");
			mEditText.setText(message);
		}
		long duration = mTime.end("onResume");
		String label = "no_intent";
		if (message != null) {
			label = "intent_len: " + message.length();
		}

		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "onResume", label,
				null, null);
		l.println("onResume: " + duration + "ms", false);
	}

	@Override
	public void onStop() {
		mTime.start("onStop");
		super.onStop();
		GAHelper.getInstance(getApplicationContext()).logActivityStop(this);
		long duration = mTime.end("onStop");
		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "onStop", null,
				null, null);
		l.println("onStop: " + duration + "ms", true);
		//
		duration = mTime.end("activity");
		GAHelper.getInstance(getApplicationContext()).logTiming(
				GAHelper.CATEGORY_APP_LIFECYCLE, duration, "DialerActivity",
				null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.overflow, menu);
		GAHelper.getInstance(getApplicationContext()).logEvent(
				GAHelper.CATEGORY_UI, "onCreateOptionsMenu", null, null, null,
				null);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// if (v.getId() == R.id.l) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		ContactVO selectedItem = (ContactVO) mAdapter.getItem(info.position);
		int index = mList.indexOf(selectedItem);
		//
		Helper.clearSelected(mList);
		Helper.setSelected(mList, index);
		mAdapter.notifyDataSetChanged();
		//
		menu.setHeaderTitle(selectedItem.name + "\n" + selectedItem.number);
		for (int i = 0; i < mOdorikModeListValueLong.size(); i++) {
			menu.add(Menu.NONE, i, i, mOdorikModeListValueLong.get(i));
		}
		GAHelper.getInstance(getApplicationContext()).logEvent(
				GAHelper.CATEGORY_UI, "onCreateContextMenu",
				"onCreateContextMenu_pos:" + info.position, null, null, null);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int menuItemIndex = item.getItemId();
		String menuItemKey = mOdorikModeListKey.get(menuItemIndex);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		ContactVO selectedItem = (ContactVO) mAdapter.getItem(info.position);
		//
		mAdapter.setNotifyOnChange(false);
		selectedItem.isProgress = true;
		mAdapter.notifyDataSetChanged();
		//
		dial(selectedItem.number, menuItemKey, false);
		//
		GAHelper.getInstance(getApplicationContext()).logEvent(
				GAHelper.CATEGORY_UI, "onContextItemSelected", menuItemKey,
				null, null, null);
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_about:
			String url = "https://sites.google.com/site/odorakcz/";
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "about",
					null, null, null);
			return true;
		case R.id.menu_version:
			InfoHelper infoHelper = InfoHelper
					.getInstance(getApplicationContext());
			Toast.makeText(
					this,
					"\n  Ver. " + infoHelper.getApplicationVersionName()
							+ " \n", Toast.LENGTH_LONG).show();
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "version",
					null, null, null);
			return true;
		case R.id.menu_setting:
			intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "setting",
					null, null, null);
			return true;
		case R.id.menu_log:
			intent = new Intent(this, LogActivity.class);
			startActivity(intent);
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "log", null,
					null, null);
			return true;
		case R.id.menu_credit:
			// show credit
			DialServiceAsyn dialAsynCredit = new DialServiceAsyn(
					getApplicationContext());
			dialAsynCredit.delegate = this;
			dialAsynCredit.execute("", "getCredit", "false");
		case R.id.menu_refresh:
			mLastEditTextRefreshed = null; // force refresh
			manageList(mEditText.getEditableText().toString());
			// show credit
			DialServiceAsyn dialAsynRefresh = new DialServiceAsyn(
					getApplicationContext());
			dialAsynRefresh.delegate = this;
			dialAsynRefresh.execute("", "getCredit", "false");
			//
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "refresh",
					null, null, null);
			return true;
		default:
			GAHelper.getInstance(getApplicationContext()).logEvent(
					GAHelper.CATEGORY_UI, "onOptionsItemSelected", "default",
					null, null, null);
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		GAHelper.getInstance(getApplicationContext()).logEvent(
				GAHelper.CATEGORY_UI,
				"odorikModeChange",
				mPrefs.getOdorikMode() + "->"
						+ mOdorikModeListKey.get(itemPosition), null, null,
				null);
		mPrefs.setOdorikMode(mOdorikModeListKey.get(itemPosition));
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String editTextStr = mEditText.getText().toString();
		outState.putString("mEditText", editTextStr);
	}

	@Override
	protected void onPause() {
		super.onPause();
		l.println("onPause()", true);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		l.println("onResume()", true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void processFinish(String message, String errType, String mode) {
		if (errType.contains("alert")) {
			// show dialog
			try {
				showAlertDialog(message);
			} catch (Exception e) {
				// pokud se nepovede vytvorit alert dialog na nekterych
				// zarizenich, ukaze se log
				Toast.makeText(this, "\n" + message + "\n", Toast.LENGTH_LONG)
						.show();
			}
		} else if (errType != null && !errType.equalsIgnoreCase("no_error")) {
			// an error, but only Toast
			Toast.makeText(this, "\n" + message + "\n", Toast.LENGTH_LONG)
					.show();
		} else if (mode.equalsIgnoreCase("getcredit")) {
			// toast and subtitle
			message = message.trim();
			Toast.makeText(
					this,
					"\n  " + getString(R.string.toast_balance) + message.trim()
							+ getString(R.string.toast_credit_czk) + "  \n",
					Toast.LENGTH_LONG).show();
			getSupportActionBar().setSubtitle(
					message.trim() + getString(R.string.toast_credit_czk));
		} else {
			// all others toast
			Toast.makeText(this, "\n" + message + "\n", Toast.LENGTH_LONG)
					.show();
		}
		Helper.clearProgress(mList);
		mAdapter.notifyDataSetChanged();
	}

	public void manageList(String filter) {
		if (mLastEditTextRefreshed == null
				|| !mLastEditTextRefreshed.equals(filter)) {
			// do mange list only if anything has changed
			mLastEditTextRefreshed = filter;
			ManageListAsyn manageListAsyn = new ManageListAsyn(this, filter);
			manageListAsyn.delegate = this;
			manageListAsyn.execute();
		}
	}

	@Override
	public void processFinishManageList(String filter, ArrayList<ContactVO> list) {
		// update only if no chage happend, else ignore
		if (filter.equalsIgnoreCase(mEditText.getText().toString())) {
			mAdapter.setNotifyOnChange(false);
			mList.clear();
			mList.addAll(list);
			//
			Intent intent = getIntent();
			String message = intent
					.getStringExtra("cz.ak.odorak.phonenumber.dial");
			intent.removeExtra("cz.ak.odorak.phonenumber.dial");
			// limit validity of the intent
			String validToStr = intent.getStringExtra("cz.ak.odorak.validto");
			long validTo;
			if (validToStr != null) {
				validTo = Long.parseLong(validToStr);
			} else {
				validTo = 0;
			}
			intent.putExtra("cz.ak.odorak.validto", "0");
			if (message != null && message.equals("NOW") && mList.size() > 0
					&& mPrefs.getAutoCall()
					&& validTo >= System.currentTimeMillis()) {
				mList.get(0).isProgress = true; // calling to the first item in
												// list
				dial(mList.get(0).number, mPrefs.getOdorikMode(), true);
			}
			//
			mAdapter.notifyDataSetChanged();
		}
	}

	public void showAlertDialog(String message) {
		DialogFragment newFragment = MyAlertDialogFragment.newInstance(message);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void doPositiveClick() {
		Intent intent = new Intent(this, PreferencesActivity.class);
		startActivity(intent);
		GAHelper.getInstance(getApplicationContext()).logEvent(
				GAHelper.CATEGORY_UI, "onOptionsItemSelected", "setting", null,
				null, null);
	}

	public void doNegativeClick() {
		// Do nothing
	}

	public static class MyAlertDialogFragment extends SherlockDialogFragment {

		public static MyAlertDialogFragment newInstance(String message) {
			MyAlertDialogFragment frag = new MyAlertDialogFragment();
			Bundle args = new Bundle();
			args.putInt("title", R.string.alert_odorik_error_title);
			args.putString("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int title = getArguments().getInt("title");
			String message = getArguments().getString("message");

			AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton(R.string.alert_dialog_settings,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((DialerActivity) getActivity())
											.doPositiveClick();
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((DialerActivity) getActivity())
											.doNegativeClick();
								}
							}).create();
			return alertDialog;
		}
	}
}
