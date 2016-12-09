package cz.ak.odorak.api;

public class OdorakAPIException extends Exception {
	public OdorakAPIException() {
		super();
	}

	public OdorakAPIException(String message) {
		super(message);
	}

	public OdorakAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public OdorakAPIException(Throwable cause) {
		super(cause);
	}
}
