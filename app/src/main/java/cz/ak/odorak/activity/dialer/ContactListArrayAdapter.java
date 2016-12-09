package cz.ak.odorak.activity.dialer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cz.ak.odorak.AsyncResponse;
import cz.ak.odorak.Helper;
import cz.ak.odorak.R;

public class ContactListArrayAdapter extends ArrayAdapter<ContactVO> 
/*implements AsyncResponse*/ 
		{
	private int mListItemLayoutResId;
	private ArrayList<ContactVO> mMyList;
	private Context mCtx;

	public ContactListArrayAdapter(Context context, List<ContactVO> objects) {
		this(context, R.layout.contact_list_item, objects);
	}

	public ContactListArrayAdapter(Context context,
			int listItemLayoutResourceId, List<ContactVO> objects) {
		super(context, listItemLayoutResourceId, objects);
		mListItemLayoutResId = listItemLayoutResourceId;
		mMyList = (ArrayList<ContactVO>) objects;
		mCtx = context;
	}

	@Override
	public android.view.View getView(int position, View convertView,
			ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View listItemView = convertView;
		if (null == convertView) {
			listItemView = inflater
					.inflate(mListItemLayoutResId, parent, false);
		}

		// The ListItemLayout must use the standard text item IDs.
		TextView lineNameView = (TextView) listItemView
				.findViewById(R.id.list_contact_name);
		TextView lineNumberView = (TextView) listItemView
				.findViewById(R.id.list_contact_number);
		TextView lineNumberLabelView = (TextView) listItemView
				.findViewById(R.id.list_contact_number_label);

		ImageView lineIcon = (ImageView) listItemView
				.findViewById(R.id.list_contact_icon);

		ImageView lineBtn = (ImageView) listItemView
				.findViewById(R.id.list_contact_btn_call);

		ProgressBar progressBar = (ProgressBar) listItemView
				.findViewById(R.id.list_contact_progress_call);

		LinearLayout lineBtnWrap = (LinearLayout) listItemView
				.findViewById(R.id.list_contact_btn_wrap);

		ContactVO t = (ContactVO) getItem(position);

		lineBtnWrap.setTag(R.id.id_contactVO, t);
		lineBtnWrap.setTag(R.id.id_position, Integer.valueOf(position));

		lineBtnWrap.setOnClickListener(new OnClickButtonArrayAdapter(this) {
			@Override
			public void onClick(View v) {
				ContactVO con = (ContactVO) v.getTag(R.id.id_contactVO);
				// Integer position = (Integer) v.getTag(R.id.id_position);
				//
				Helper.clearSelected(mAdapter.mMyList);
				int index = mAdapter.mMyList.indexOf(con);
				Helper.setSelected(mAdapter.mMyList, index);
				Helper.setProgress(mAdapter.mMyList, index);
				mAdapter.notifyDataSetChanged();
				//
				DialServiceAsyn dialAsyn = new DialServiceAsyn(getContext());
				dialAsyn.delegate = (DialerActivity) mCtx;
				dialAsyn.execute(con.number, "", "true");
				/*
				 * DialService dialer = new DialService(mAdapter.getContext()
				 * .getApplicationContext()); dialer.dial(con.number, true);
				 */
			}
		});

		lineNameView.setText(lineNameText(t));
		lineNumberView.setText(lineNumberText(t));
		lineNumberLabelView.setText(lineNumberLabelText(t));

		// stars
		if (t.isStared) {
			lineNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.rate_star_small_on_holo_dark, 0);
		} else {
			lineNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}

		// progress
		if (t.isProgress) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			lineBtn.setVisibility(ImageView.GONE);
			lineBtnWrap.setBackgroundColor(Color.DKGRAY);
		} else {
			lineBtn.setVisibility(ImageView.VISIBLE);
			progressBar.setVisibility(ProgressBar.GONE);
			lineBtnWrap.setBackgroundColor(Color.BLACK);
			// progressBar.setVisibility(ProgressBar.INVISIBLE);
		}

		// call log
		switch (t.hisState) {
		case ContactVO.INCOMING:
			lineIcon.setImageResource(android.R.drawable.sym_call_incoming);
			break;
		case ContactVO.OUTGOING:
			lineIcon.setImageResource(android.R.drawable.sym_call_outgoing);
			break;
		case ContactVO.MISSED:
			lineIcon.setImageResource(android.R.drawable.sym_call_missed);
			break;
		default:
			lineIcon.setImageDrawable(null);
			break;
		}

		listItemView.setBackgroundResource(0);
		if (t.selected) {
			listItemView.setBackgroundResource(R.color.selected_list_item);
		}

		return listItemView;
	}

	public String lineNameText(ContactVO line) {
		return line.name;
	}

	public String lineNumberText(ContactVO line) {
		return line.number;
	}

	public String lineNumberLabelText(ContactVO line) {
		return line.numberLabel;
	}

	/*
	@Override
	public void processFinish(String message, String type) {
		if (type.contains("alert")) {
			((DialerActivity) mCtx).showAlertDialog(message);
		} else {
			Toast.makeText(getContext(), "\n" + message + "\n",
					Toast.LENGTH_LONG).show();
		}
		Helper.clearProgress(mMyList);
		notifyDataSetChanged();
	}
	*/
}
