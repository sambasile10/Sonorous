package crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import res.ManagedThread;
import res.ThreadManager;

public class AESCipherStream extends ManagedThread {

	private int threadID;
	private String threadName;
	
	private AESParameters cipherParams;
	private Cipher cipher;
	private SecureRandom secureRandom;
	private boolean isInitialized = false, useBase64 = false;
	
	public AESCipherStream(AESParameters cipherParams, boolean useBase64) {
		super("AESCipherThread");
		this.cipherParams = cipherParams;
		this.useBase64 = useBase64;
	}
	
	public byte[] pipe(byte[] data) {
		if(!this.isInitialized)
			return null;
		
		try {
			byte[] cData = cipher.doFinal(data);
			return useBase64 ? (cipherParams.getCipherMode() == CipherMode.ENCRYPT ? Base64.encodeBase64(cData) : Base64.decodeBase64(cData)) : cData;
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	@Override
	public int initialize() {
		try {
			this.secureRandom = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return -3;
		}
		
		SecretKeySpec keySpec = generateKey();
		if(keySpec == null) {
			return -2;
		}
		
		try {
			this.cipher = Cipher.getInstance(cipherParams.getBlockMode().getCipherModeString(), "BC");
			cipher.init(cipherParams.getCipherMode().getCipherMode(), keySpec, new IvParameterSpec(cipherParams.getIV()));
			this.isInitialized = true;
			return 0;
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
			return -1;
		}
		
		
	}
	
	public SecretKeySpec generateKey() {
		try {
			PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
			gen.init(cipherParams.getPassword().getBytes("UTF-8"), cipherParams.getSalt().getBytes(), 4096);
			byte[] dk = ((KeyParameter) gen.generateDerivedParameters(256)).getKey();
			return new SecretKeySpec(dk, "AES");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public int halt() {
		return this.unregister();
	}

}
