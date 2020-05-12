package crypto;

import java.security.GeneralSecurityException;

import res.Log;

public class InvalidCipherInitializationException extends RuntimeException {

	private static final long serialVersionUID = -5272245851393748559L;
	private static final String defaultErrorMessage = "Invalid Cipher construction arguments!";
	
	public InvalidCipherInitializationException() {
		super(defaultErrorMessage, new GeneralSecurityException());
		Log.error(defaultErrorMessage);
	}
	
	public InvalidCipherInitializationException(String errorMessage) {
		super(errorMessage, new GeneralSecurityException());
		Log.error(errorMessage);
	}

}
