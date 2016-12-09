package cz.ak.odorak;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import cz.ak.odorak.R;
import cz.ak.odorak.activity.dialer.ContactVO;

public class MyContactList {
	private static Context mCtx;
	private static ContentResolver mCR;
	private static String mDisplayName;
	private static MyContactList mInstance;

	protected MyContactList() {
	}

	public static MyContactList getInstance(Context ctx) {
		if (mInstance == null) {
			mCtx = ctx.getApplicationContext();
			mCR = mCtx.getContentResolver();
			mDisplayName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.DISPLAY_NAME_PRIMARY
					: Contacts.DISPLAY_NAME;
			mInstance = new MyContactList();
		}
		return mInstance;
	}

	public ContactVO getDetail(String number) {

		ContactVO ret = new ContactVO();
		final String[] PROJECTION = new String[] { Contacts._ID, mDisplayName,
				Contacts.STARRED };

		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		final String ORDER = "contacts_view." + Contacts.STARRED + " DESC, "
				+ "contacts_view." + mDisplayName + " ASC";

		Cursor cur = mCR.query(contactUri, PROJECTION, null, null, ORDER);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				ret.name = cur.getString(1);
				ret.number = number;
				ret.numberLabel = "";
				ret.selected = false;
				ret.isStared = cur.getString(2).equals("1") ? true : false;
				return ret;
			}
		}
		return null;
	}

	public ArrayList<ContactVO> getContacts(String filter, int limit) {
		ArrayList<ContactVO> contacts = new ArrayList<ContactVO>();

		final String[] PROJECTION = new String[] { Contacts._ID, mDisplayName,
				Contacts.HAS_PHONE_NUMBER, Contacts.STARRED,
				Contacts.TIMES_CONTACTED, Contacts.LAST_TIME_CONTACTED };

		final String SELECTION = Contacts.HAS_PHONE_NUMBER + " = 1 AND "
				+ mDisplayName + " LIKE ?";

		final String[] ARGS = new String[] { "%" + filter + "%", };

		String ORDER;
		if (filter.length() == 0) {
			/*
			 * ORDER = Contacts.STARRED + " DESC, " + Contacts.TIMES_CONTACTED +
			 * " DESC, " + Contacts.LAST_TIME_CONTACTED + " DESC, " +
			 * mDisplayName + " ASC";
			 */
			ORDER = Contacts.STARRED + " DESC, " + mDisplayName + " ASC";
		} else {
			ORDER = Contacts.STARRED + " DESC, " + mDisplayName + " ASC";
		}

		Cursor cur = mCR.query(ContactsContract.Contacts.CONTENT_URI,
				PROJECTION, SELECTION, ARGS, ORDER);

		Log.d("Kontakt", "Pocet: " + cur.getCount());

		if (cur.getCount() > 0) {
			while (cur.moveToNext() && contacts.size() < limit) {
				String stared = cur.getString(3);
				boolean isStared = false;
				if (stared.equals("1")) {
					isStared = true;
				}
				Cursor pCur = mCR.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[] { Phone.NUMBER, Phone.TYPE, Phone.LABEL },
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { cur.getString(0) },
						Phone.TYPE + " ASC");
				while (pCur.moveToNext() && contacts.size() < limit) {
					String phoneLabel = getPhoneLabel(
							Integer.parseInt(pCur.getString(1)),
							pCur.getString(2));
					boolean selected = false;

					contacts.add(new ContactVO(cur.getString(1), pCur
							.getString(0), phoneLabel, selected, isStared, -1,
							cur.getString(0)));
					// if (contacts.size() >= limit) {
					// break;
					// }
				}
				pCur.close();
				// if (contacts.size() >= limit) {
				// break;
				// }
			}
		}
		cur.close();
		return contacts;
	}

	public String getPhoneLabel(int type, String label) {
		String s;
		switch (type) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			s = mCtx.getString(R.string.contact_home_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			s = mCtx.getString(R.string.contact_mobile_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			s = mCtx.getString(R.string.contact_work_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
			s = mCtx.getString(R.string.contact_fax_work_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
			s = mCtx.getString(R.string.contact_fax_home_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
			s = mCtx.getString(R.string.contact_pager_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
			s = mCtx.getString(R.string.contact_other_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
			s = mCtx.getString(R.string.contact_callback_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
			s = mCtx.getString(R.string.contact_car_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
			s = mCtx.getString(R.string.contact_company_main_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
			s = mCtx.getString(R.string.contact_isdn_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
			s = mCtx.getString(R.string.contact_main_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
			s = mCtx.getString(R.string.contact_other_fax_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
			s = mCtx.getString(R.string.contact_radio_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
			s = mCtx.getString(R.string.contact_telex_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
			s = mCtx.getString(R.string.contact_tty_tdd_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
			s = mCtx.getString(R.string.contact_work_mobile_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
			s = mCtx.getString(R.string.contact_work_pager_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
			s = mCtx.getString(R.string.contact_assistant_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
			s = mCtx.getString(R.string.contact_mms_phone);
			break;
		case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:

			if (label == null)
				s = mCtx.getString(R.string.contact_custom);
			else
				s = label;
			break;
		default:
			s = mCtx.getString(R.string.contact_default);
		}
		return s;
	}
}
