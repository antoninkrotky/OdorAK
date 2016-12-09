package cz.ak.odorak.activity.dialer;

public class ContactVO {
	public static final int MISSED = 1;
	public static final int OUTGOING = 2;
	public static final int INCOMING = 3;

	public String name;
	public String number;
	public String numberLabel;
	public boolean selected;
	public boolean isStared;
	public int hisState;
	public String _ID;
	public boolean isProgress; // for listview - to show progress bar

	public ContactVO(ContactVO obj) {
		this.name = obj.name;
		this.number = obj.number;
		this.numberLabel = obj.numberLabel;
		this.selected = obj.selected;
		this.isStared = obj.isStared;
		this.hisState = obj.hisState;
		this._ID = obj._ID;
		this.isProgress = obj.isProgress;
	}

	public ContactVO(String name, String number, String numberLabel,
			boolean selected, boolean isStared, int hisState, String _ID) {
		this.name = name;
		this.number = number;
		this.numberLabel = numberLabel;
		this.selected = selected;
		this.isStared = isStared;
		this.hisState = hisState;
		this._ID = _ID;
		this.isProgress = false;
	}

	public ContactVO() {
	}

}