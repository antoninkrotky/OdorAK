package cz.ak.odorak.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import cz.ak.odorak.Helper;
import cz.ak.odorak.LoggerHelper;
import cz.ak.odorak.R;

public class OdorikAPI {
	protected class UrlParamsVO {
		String name;
		String value;

		public UrlParamsVO(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	final String odorik_url_base = "https://www.odorik.cz/api/v1";
	final String odorik_url_callback = "/callback";
	final String odorik_url_get_credit = "/balance";
	final String odorik_url_par_ring = "/public_numbers/<verejne_cislo>/ringings.json";
	final String odorik_url_par_ring_del = "/public_numbers/<verejne_cislo>/ringings/<cilove_cislo>.json";
	final String odorik_url_routes = "/public_numbers/<verejne_cislo>/routes.json";
	final String odorik_url_routes_del = "/public_numbers/<verejne_cislo>/routes/<id>.json";
	final String url_externalEnable = "https://sites.google.com/site/odorakcz/home/OdorakEnable";
	String odorik_id;
	String odorik_pwd;
	LoggerHelper l;
	Context mCtx;

	public OdorikAPI(String odorik_id, String odorik_pwd, Context ctx) {
		this.odorik_id = odorik_id;
		this.odorik_pwd = odorik_pwd;
		this.l = LoggerHelper.getInstance(ctx);
		this.mCtx = ctx.getApplicationContext();
	}

	private void logParams(ArrayList<UrlParamsVO> params) {
		for (UrlParamsVO item : params) {
			l.println(" param: " + item.name + "=" + item.value, false);
		}
	}

	public ArrayList<UrlParamsVO> initParam(boolean auth) {
		ArrayList<UrlParamsVO> params = new ArrayList<UrlParamsVO>();
		if (auth == true) {
			params.add(new UrlParamsVO("user", odorik_id));
			params.add(new UrlParamsVO("password", odorik_pwd));
		}
		return params;
	}

	public String orderCallBack(String source_number, String target_number,
			String line) throws Exception {
		String url = odorik_url_base + odorik_url_callback;
		ArrayList<UrlParamsVO> params = initParam(true);
		params.add(new UrlParamsVO("caller", source_number));
		params.add(new UrlParamsVO("recipient", target_number));
		params.add(new UrlParamsVO("line", line));
		String resp = sendReq("POST", url, params);
		if (odorik_id.length() == 0
				|| odorik_pwd.length() == 0
				|| source_number.length() == 0
				|| source_number.equals("skype:")
				|| target_number.length() == 0
				|| line.length() == 0
				|| (!(resp.contains("callback_ordered") || resp
						.contains("successfully_enqueued")))) {
			throw new OdorakAPIException(
					mCtx.getString(R.string.alert_odorik_error_message_resp)
							+ resp
							+ "\n"
							+ mCtx.getString(R.string.alert_odorik_error_message_param)
							+ "\n" + "  " + mCtx.getString(R.string.odorik_id)
							+ "=" + odorik_id + ",\n " + " "
							+ mCtx.getString(R.string.odorik_pwd) + "="
							+ odorik_pwd + ",\n " + " "
							+ mCtx.getString(R.string.odorik_line) + "=" + line
							+ ",\n " + " "
							+ mCtx.getString(R.string.mobile_number) + "="
							+ source_number + ",\n " + " "
							+ mCtx.getString(R.string.target_number) + "="
							+ target_number + "\n");
		}
		return resp;
	}

	public String setRoutes(String odorik_number, String source_number,
			String target_number, String replace) throws Exception {
		String url = odorik_url_base
				+ odorik_url_routes
						.replaceAll("<verejne_cislo>", odorik_number);
		ArrayList<UrlParamsVO> params = initParam(true);

		params.add(new UrlParamsVO("source_number", source_number));
		params.add(new UrlParamsVO("ringing_number", target_number));
		params.add(new UrlParamsVO("replace_by_source_number", replace));
		String resp = "";
		if (odorik_number.length() > 0) {
			resp = sendReq("POST", url, params);
		}
		if (odorik_id.length() == 0 || odorik_pwd.length() == 0
				|| source_number.length() == 0 || target_number.length() == 0
				|| odorik_number.length() == 0 || resp.contains("error")) {
			throw new OdorakAPIException(
					mCtx.getString(R.string.alert_odorik_error_message_resp)
							+ resp
							+ "\n"
							+ mCtx.getString(R.string.alert_odorik_error_message_param)
							+ "\n" + "  " + mCtx.getString(R.string.odorik_id)
							+ "=" + odorik_id + ",\n " + " "
							+ mCtx.getString(R.string.odorik_pwd) + "="
							+ odorik_pwd + ",\n " + " "
							+ mCtx.getString(R.string.odorik_number) + "="
							+ odorik_number + ",\n " + " "
							+ mCtx.getString(R.string.mobile_number) + "="
							+ source_number + ",\n " + " "
							+ mCtx.getString(R.string.target_number) + "="
							+ target_number + "\n");
		}
		return resp;
	}

	public String getRoutes(String odorik_number) throws Exception {
		String url = odorik_url_base
				+ odorik_url_routes
						.replaceAll("<verejne_cislo>", odorik_number);
		ArrayList<UrlParamsVO> params = initParam(true);
		String resp = sendReq("GET", url, params);
		return resp;
	}

	public String delRoutes(String odorik_number, String id) throws Exception {
		String url = odorik_url_base
				+ odorik_url_routes_del.replaceAll("<verejne_cislo>",
						odorik_number).replaceAll("<id>", id);
		ArrayList<UrlParamsVO> params = initParam(true);
		String resp = sendReq("DELETE", url, params);
		return resp;
	}

	public String getParRing(String odorik_number) throws Exception {
		String url = odorik_url_base
				+ odorik_url_par_ring.replaceAll("<verejne_cislo>",
						odorik_number);
		ArrayList<UrlParamsVO> params = initParam(true);
		String resp = sendReq("GET", url, params);
		return resp;
	}

	public String setParRing(String odorik_number, String target_number)
			throws Exception {
		String url = odorik_url_base
				+ odorik_url_par_ring.replaceAll("<verejne_cislo>",
						odorik_number);
		ArrayList<UrlParamsVO> params = initParam(true);
		params.add(new UrlParamsVO("ringing_number", target_number));
		String resp = sendReq("POST", url, params);
		return resp;
	}

	public void delParRing(String odorik_number, String target_number)
			throws Exception {
		String url = odorik_url_base
				+ odorik_url_par_ring_del.replaceAll("<verejne_cislo>",
						odorik_number).replaceAll("<cilove_cislo>",
						target_number);
		ArrayList<UrlParamsVO> params = initParam(true);
		String resp = sendReq("DELETE", url, params);
		l.println("delParRing responce: " + resp);
		if (Helper.getLenJSON(resp) > 0) {
			throw new RuntimeException("Response returned: " + resp);
		}
	}

	public String getCredit() throws Exception {
		String url = odorik_url_base + odorik_url_get_credit;
		ArrayList<UrlParamsVO> params = initParam(true);
		String resp = sendReq("GET", url, params);
		l.println("getCredit responce: " + resp);
		if (odorik_id.length() == 0 || odorik_pwd.length() == 0
				|| resp.contains("error")) {
			throw new OdorakAPIException(
					mCtx.getString(R.string.alert_odorik_error_message_resp)
							+ resp
							+ "\n"
							+ mCtx.getString(R.string.alert_odorik_error_message_param)
							+ "\n" + "  " + mCtx.getString(R.string.odorik_id)
							+ "=" + odorik_id + ",\n " + " "
							+ mCtx.getString(R.string.odorik_pwd) + "="
							+ odorik_pwd + ",\n ");
		}
		return resp;
	}

	public void logGoogle(String log) {
		ArrayList<UrlParamsVO> params = initParam(false);
		params.add(new UrlParamsVO("entry_1187047230", log));
		l.println("Google form: sending");
		try {
			l.setEnabled(false);
			sendReq("POST",
					"https://docs.google.com/forms/d/1tKxf0lDx19pyyVnU06vaFqt6g7YLdJU-e0DmJNtnuRY/formResponse",
					params);
			l.setEnabled(true);
			l.println("Google form: sent");
		} catch (Exception e) {
			l.setEnabled(true);
			l.println("Google form: error sending");
		}
	}

	public boolean isRemotelyEnabled() throws Exception {
		String res = sendReq("GET", url_externalEnable,
				new ArrayList<UrlParamsVO>());
		if (res.trim().startsWith("1")) {
			return true;
		}
		return false;
	}

	/*
	 * @method: POST/GET/DELETE/PUT
	 */
	String sendReq(String method, String targetURL,
			ArrayList<UrlParamsVO> params) throws Exception {
		URL url;
		HttpsURLConnection connection = null;
		String urlParameters = "";

		try {
			for (UrlParamsVO s : params) {
				if (urlParameters.length() > 0) {
					urlParameters = urlParameters + "&";
				}
				urlParameters = urlParameters
						+ URLEncoder.encode(s.name, "UTF-8") + "="
						+ URLEncoder.encode(s.value, "UTF-8");
			}

			String urlStr = targetURL;
			if (!method.startsWith("POST") && urlParameters.length() > 0) {
				urlStr = urlStr + "?" + urlParameters;
			}
			// Create connection
			l.println(urlStr + "(" + method + ")");
			logParams(params);
			url = new URL(urlStr);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod(method);

			connection.setUseCaches(false);
			connection.setDoInput(true);

			// Send request
			if (method.startsWith("POST")) {
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Length",
						"" + Integer.toString(urlParameters.getBytes().length));
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Language", "en-US");
				DataOutputStream wr = new DataOutputStream(
						connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
			}

			// Get Response
			int status = connection.getResponseCode();
			l.println("Returned http status: " + status);

			if (status != 200) {
				throw new RuntimeException("HTTP STATUS: " + status);
			}

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			String resp = response.toString();
			l.println("Response: " + resp);
			return resp;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
