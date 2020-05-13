package crypto;

public enum DigestAlgorithm {
	
	SHA_256("SHA-256", "SHA-256", 0), SHA3_256("SHA3_256", "SHA3-256", 1), 
		KSHA3_256("KECCAK_256", "KSHA3-256", 2);
	
	String hashAlgorithm, shortString;
	int id;
	
	DigestAlgorithm(String hashAlgorithm, String shortString, int id) {
		this.hashAlgorithm = hashAlgorithm;
		this.shortString = shortString;
		this.id = id;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public String getShortString() {
		return shortString;
	}

	public int getId() {
		return id;
	}

}
