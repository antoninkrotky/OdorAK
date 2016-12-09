package cz.ak.odorak;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.ak.odorak.activity.dialer.ContactVO;

public class Helper {
	public static String stackTraceToString(Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.getMessage());
		sb.append("\n");
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static int getLenJSON(String inputStr) throws JSONException {
		JSONObject json = new JSONObject(inputStr);
		return json.length();
	}

	public static ArrayList<String> getParRing(String inputStr)
			throws JSONException {
		ArrayList<String> arr = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray(inputStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			arr.add(jsonObject.getString("ringing_number"));
		}
		return arr;
	}

	public static ArrayList<String> getRoutsID(String inputStr)
			throws JSONException {
		ArrayList<String> arr = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray(inputStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			arr.add(jsonObject.getString("id"));
		}
		return arr;
	}

	public static String getIntPhoneNum(String phoneNum) {
		String res;
		res = phoneNum.trim().replaceAll("\\s", "").replaceFirst("^\\+", "00");
		if (res.startsWith("00")) {
			return res;
		} else if (res.length() == 9) {
			return "00420" + res;
		} else {
			return res;
		}
	}

	public static ArrayList<ContactVO> setSelected(ArrayList<ContactVO> list,
			int indexToSelect) {
		if (list.size() - 1 < indexToSelect) {
			return list; // out of bound
		}
		ContactVO item = list.get(indexToSelect);
		item.selected = true;
		list.set(indexToSelect, item);
		return list;
	}

	public static ArrayList<ContactVO> setProgress(ArrayList<ContactVO> list,
			int indexToProgress) {
		if (list.size() - 1 < indexToProgress) {
			return list; // out of bound
		}
		ContactVO item = list.get(indexToProgress);
		item.isProgress = true;
		list.set(indexToProgress, item);
		return list;
	}

	public static ArrayList<ContactVO> clearSelected(ArrayList<ContactVO> list) {
		for (int i = 0; i < list.size(); i++) {
			ContactVO item = list.get(i);
			item.selected = false;
			list.set(i, item);
		}
		return list;
	}

	public static ArrayList<ContactVO> clearProgress(ArrayList<ContactVO> list) {
		for (int i = 0; i < list.size(); i++) {
			ContactVO item = list.get(i);
			item.isProgress = false;
			list.set(i, item);
		}
		return list;
	}

	public static boolean isBypassed(String number, String list) {
		// bypass
		number = number.replaceAll("\\s", "");
		String maskPhoneNumber = Helper.maskNumber(number, 9);
		if (list.indexOf(maskPhoneNumber) > -1) {
			return true;
		}
		return false;
	}

	public static String maskNumber(String number, int len) {
		int mask_from = number.length() - len;
		String mask_number = number;
		if (mask_from > 0) {
			mask_number = number.substring(mask_from);
		}
		return mask_number;
	}

	public static boolean isSpecialNumber(String number) {
		if (number.contains("#") || number.contains("*")
				|| number.equals("112") || number.equals("150")
				|| number.equals("155") || number.equals("158")
				|| number.equals("156")) {
			return true;
		} else {
			return false;
		}
	}
}
