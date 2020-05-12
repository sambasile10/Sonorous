package crypto;

public class AESParameters {
	
	private String password, salt;
	private byte[] iv;
	private int keySize;
	private CipherMode cipherMode;
	private CipherBlockMode blockMode;
	
	public AESParameters(String password, String salt, byte[] iv, int keySize, CipherMode cipherMode, CipherBlockMode blockMode) {
		this.password = password;
		this.salt = salt;
		this.iv = iv;
		this.keySize = keySize;
		this.cipherMode = cipherMode;
		this.blockMode = blockMode;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public byte[] getIV() {
		return iv;
	}

	public void setIV(byte[] iv) {
		this.iv = iv;
	}

	public int getKeySize() {
		return keySize;
	}

	public void setKeySize(int keySize) {
		this.keySize = keySize;
	}

	public CipherMode getCipherMode() {
		return cipherMode;
	}

	public void setCipherMode(CipherMode cipherMode) {
		this.cipherMode = cipherMode;
	}

	public CipherBlockMode getBlockMode() {
		return blockMode;
	}

	public void setBlockMode(CipherBlockMode blockMode) {
		this.blockMode = blockMode;
	}
	
	

}
