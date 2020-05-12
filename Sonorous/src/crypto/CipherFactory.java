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
		Thread.currentThread().interrupt();
		if(this.isInterrupted()) {
			return (ThreadManager.unregister(threadID) == 0 ? 0 : -2);
		} else {
			return -1;
		}
	}
	
	public RSACipherStream buildRSAStream(CipherMode cipherMode, Key key) {
		if(cipherMode == CipherMode.ENCRYPT) {
			if(key instanceof PublicKey) {
				RSACipherStream cThread = new RSACipherStream(cipherMode, key);
				if(cThread.initialize() == 0) {
					cThread.exec();
					return cThread;
				} else {
					throw new InvalidCipherInitializationException("RSAThread initialization failed");
				}
			} else {
				throw new InvalidCipherInitializationException("RSA ENCRYPT requires PublicKey object");
			}
		} else if(cipherMode == CipherMode.DECRYPT) {
			if(key instanceof PrivateKey) {
				RSACipherStream cThread = new RSACipherStream(cipherMode, key);
				if(cThread.initialize() == 0) {
					cThread.exec();
					return cThread;
				} else {
					throw new InvalidCipherInitializationException("RSAThread initialization failed");
				}
			} else {
				throw new InvalidCipherInitializationException("RSA DECRYPT requires PrivateKey object");
			}
		} else {
			throw new InvalidCipherInitializationException("Invalid CipherMode for RSA threads");
		}
	}
	
	public AESCipherStream buildAESThread(AESParameters aesParams) {
		AESCipherStream cThread = new AESCipherStream(aesParams);
		if(cThread.initialize() == 0) {
			cThread.exec();
			return cThread;
		} else {
			throw new InvalidCipherInitializationException("Failed to initialize AESCipherStream!");
		}
	}
	
	public KeyPair generateKeyPair(int keySize) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keySize);
			KeyPair kp = kpg.genKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public HashMap<String, byte[]> formatKeyPairAsPKCS1(PublicKey pub, PrivateKey priv) {
		HashMap<String, byte[]> pkcs1map = new HashMap<String, byte[]>();
		
		if(pub != null) {
			try {
				byte[] pubBytes = pub.getEncoded();
				SubjectPublicKeyInfo spkInfo = SubjectPublicKeyInfo.getInstance(pubBytes);
				ASN1Primitive publicPrimitive = spkInfo.parsePublicKey();
				byte[] publicKeyPKCS1 = publicPrimitive.getEncoded();
				pkcs1map.put("PUBLIC", publicKeyPKCS1);
			} catch (IOException e1) {
				Log.error("Error while converting key pair to PKCS1");
				e1.printStackTrace();
				return null;
			}
		}

		if(priv != null) {
			try {
				byte[] privBytes = priv.getEncoded();
				PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(privBytes);
				ASN1Encodable encodable = pkInfo.parsePrivateKey();
				ASN1Primitive privatePrimitive = encodable.toASN1Primitive();
				byte[] privateKeyPKCS1 = privatePrimitive.getEncoded();
				pkcs1map.put("PRIVATE", privateKeyPKCS1);
			} catch (IOException e2) {
				Log.error("Error while converting key pair to PKCS1");
				e2.printStackTrace();
				return null;
			}
		}
		
		return pkcs1map;
	}

}
