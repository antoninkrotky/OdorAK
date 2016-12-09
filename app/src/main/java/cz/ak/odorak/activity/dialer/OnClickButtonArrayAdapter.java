package cz.ak.odorak.activity.dialer;

import android.view.View.OnClickListener;

public abstract class OnClickButtonArrayAdapter implements OnClickListener {
	ContactListArrayAdapter mAdapter;

	OnClickButtonArrayAdapter(ContactListArrayAdapter adapter) {
		super();
		this.mAdapter = adapter;
	}

}
