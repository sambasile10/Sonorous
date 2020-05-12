package crypto;

import javax.crypto.Cipher;

public enum CipherMode {
	
	ENCRYPT(Cipher.ENCRYPT_MODE, "ENCRYPT"), DECRYPT(Cipher.DECRYPT_MODE, "DECRYPT");
	
	int cipherMode;
	String modeName;
	
	CipherMode(int cipherMode, String modeName) {
		this.cipherMode = cipherMode;
	}
	
	int getCipherMode() {
		return this.cipherMode;
	}
	
	String getName() {
		return this.modeName;
	}

}
