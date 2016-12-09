package cz.ak.odorak;

import java.util.ArrayList;

import cz.ak.odorak.activity.dialer.ContactVO;

public interface AsyncResponse {
	void processFinish(String message, String type, String mode);
	void processFinishManageList(String filter, ArrayList<ContactVO> list);
}