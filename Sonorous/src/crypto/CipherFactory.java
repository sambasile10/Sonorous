package crypto;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.HashMap;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import res.ErrorCode;
import res.InternalExceptionManager;
import res.Log;
import res.ManagedThread;
import res.ThreadManager;

public class CipherFactory extends ManagedThread {
	
	private int threadID;
	private String className;
	
	public CipherFactory() {
		super("CipherFactory");
	}
	
	@Override
	public int initialize() {
		Security.addProvider(new BouncyCastleProvider());
		Security.setProperty("crypto.policy", "unlimited");
		
		return 0;
	}
	
	@Override
	public int halt() {
		return this.unregister();
	}
	
	public RSACipherStream buildRSAStream(CipherMode cipherMode, Key key, boolean useBase64) {
		if(cipherMode == CipherMode.ENCRYPT) {
			if(key instanceof PublicKey) {
				RSACipherStream cThread = new RSACipherStream(cipherMode, key, useBase64);
				if(cThread.initialize() == 0) {
					cThread.exec();
					return cThread;
				} else {
					InternalExceptionManager.handleException(this, ErrorCode.RSACIPHER_INIT_FAILED);
					return null;
				}
			} else {
				InternalExceptionManager.handleException(this, ErrorCode.RSACIPHER_KEY_MISMATCH);
				return null;
			}
		} else if(cipherMode == CipherMode.DECRYPT) {
			if(key instanceof PrivateKey) {
				RSACipherStream cThread = new RSACipherStream(cipherMode, key, useBase64);
				if(cThread.initialize() == 0) {
					cThread.exec();
					return cThread;
				} else {
					InternalExceptionManager.handleException(this, ErrorCode.RSACIPHER_INIT_FAILED);
					return null;
				}
			} else {
				InternalExceptionManager.handleException(this, ErrorCode.RSACIPHER_KEY_MISMATCH);
				return null;
			}
		} else {
			InternalExceptionManager.handleException(this, ErrorCode.RSACIPHER_INIT_FAILED);
			return null;
		}
	}
	
	public AESCipherStream buildAESStream(AESParameters aesParams, boolean useBase64) {
		AESCipherStream cThread = new AESCipherStream(aesParams, useBase64);
		if(cThread.initialize() == 0) {
			cThread.exec();
			return cThread;
		} else {
			InternalExceptionManager.handleException(this, ErrorCode.AESCIPHER_INIT_FAILED);
			return null;
		}
	}

}
