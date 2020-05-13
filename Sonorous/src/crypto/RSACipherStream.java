package crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import res.Log;
import res.ManagedThread;
import res.ThreadManager;

public class RSACipherStream extends ManagedThread {

	protected static final String BASE_ALG = "RSA", ALG = "RSA/ECB/PKCS1Padding";
	protected static final int KEY_SIZE = 2048;
	
	private int threadID;
	private String className;
	
	private CipherMode cipherMode;
	private Cipher cipher;
	private SecureRandom secureRandom;
	private Key key = null;
	private boolean isInitialized = false, useBase64 = false;
	
	public RSACipherStream(CipherMode cipherMode, Key key, boolean useBase64) {
		super("RSACipherStream");
		this.cipherMode = cipherMode;
		this.key = key;
		this.useBase64 = useBase64;
	}
	
	public byte[] pipe(byte[] data) {
		if(!isInitialized)
			return null;
		
		try {
			byte[] cData = cipher.doFinal(data);
			return useBase64 ? (cipherMode == CipherMode.ENCRYPT ? Base64.encodeBase64(cData) : Base64.decodeBase64(cData)) : cData;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public int initialize() {
		if(cipherMode == CipherMode.ENCRYPT) {
			if(!(this.key instanceof PublicKey))
				return -1;
		} else if(cipherMode == CipherMode.DECRYPT) {
			if(!(this.key instanceof PrivateKey))
				return -1;
		} else {
			return -4;
		}
		
		try {
			this.secureRandom = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return -3;
		}
		
		try {
			cipher = Cipher.getInstance(ALG, "BC");
			cipher.init(cipherMode.getCipherMode(),
					cipherMode == CipherMode.ENCRYPT ? (PublicKey)this.key : (PrivateKey)this.key, secureRandom);
			this.isInitialized = true;
			Log.write("[" + className + "//" + threadID + "] initialized with " + cipherMode.getName());
			return 0;
		} catch (GeneralSecurityException e2) {
			Log.error("[" + className + "//" + threadID + "] failed to initialize with " + cipherMode.getName());
			e2.printStackTrace();
			return -2;
		}
	}
	
	@Override
	public int halt() {
		Thread.currentThread().interrupt();
		if(this.isInterrupted()) {
			return (ThreadManager.unregister(threadID) == 0 ? 0 : -2);
		} else {
			return -1;
		}
	}

}
