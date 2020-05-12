package crypto;

public enum CipherBlockMode {
	
	CBC("AES/CBC/PKCS5Padding", "CBC", 0), CTR("AES/CTR/NoPadding", "CTR", 1);
	
	String modeFullString, shortString;
	int id;
	
	CipherBlockMode(String modeFullString, String shortString, int id) {
		this.modeFullString = modeFullString;
		this.shortString = shortString;
		this.id = id;
	}
	
	String getCipherModeString() {
		return this.modeFullString;
	}
	
	String getShortString() {
		return this.shortString;
	}
	
	int getID() {
		return this.id;
	}

}
