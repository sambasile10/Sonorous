package res;

public enum ErrorCode {
	
	//Sonorous general errors
	
	//General crypto errors
	CIPHER_UNINITIALIZED(1, 0, "Cipher uninitialized"), CIPHER_SECRANDOM_FAILED(1, 1, "SecureRandom failed to initialize"),
	CIPHER_DFINAL_FAILED(1, 2, "Cipher.doFinal() threw an exception"),
	
	//AESCipherStream errors
	AESCIPHER_GENKEY_FAILED(2, 0, "AES key generation failed"), AESCIPHER_INIT_FAILED(2, 1, "AES Cipher initialization failed"),
	
	
	//RSACipherStream errors
	RSACIPHER_INIT_FAILED(3, 0, "RSA Cipher initialization failed"), RSACIPHER_KEY_MISMATCH(3, 1, "RSA given key does not match cipher mode"),
	
	
	//CipherFactory errors
	
	
	//CipherUtil errors
	CUTIL_NO_SUCH_HASHALG(5, 0, "Invalid hash algorithm"), CUTIL_SIGN_ERROR(5, 1, "RSA Sign function threw an exception"),
	CUTIL_VERIFY_ERROR(5, 2, "RSA Verify function threw an exception"),
	
	//IOManager errors
	IOMAN_STREAM_INIT_ERR(6, 0, "Stream returned error during initialization"), IO_CIPHER_ERR(6, 1, "Cipher threw an exception."),
	IO_STREAM_INIT_ERR(6, 2, "Failed to build file I/O streams."), IO_STREAM_ZIP_ERR(6, 3, "ZipUtil failed an operation."),
	IOMAN_INVALID_STREAM_ID(6, 4, "Invalid stream ID"), IO_STREAM_DELETE_ERR(6, 5, "Stream failed to delete temporary file(s)");
	
	
	private int majorID, minorID;
	private String description;
	
	ErrorCode(int majorID, int minorID, String description) {
		this.majorID = majorID;
		this.minorID = minorID;
		this.description = description;
	}

	public int getMajorID() {
		return majorID;
	}

	public void setMajorID(int majorID) {
		this.majorID = majorID;
	}

	public int getMinorID() {
		return minorID;
	}

	public void setMinorID(int minorID) {
		this.minorID = minorID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
