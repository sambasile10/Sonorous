package crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.IOTask;
import res.ErrorCode;
import res.InternalExceptionManager;
import res.Log;
import res.ManagedThread;
import res.Module;
import res.Sonorous;
import res.ThreadManager;

public class CipherUtil extends ManagedThread {
	
	private int threadID;
	private String className;
	
	public CipherUtil() {
		super("CipherUtil");
	}
	
	@Override
	public int initialize() {
		Security.addProvider(new BouncyCastleProvider());
		return 0;
	}
	
	@Override
	public int halt() {
		return this.unregister();
	}
	
	public byte[] hash(byte[] data, DigestAlgorithm algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm.getHashAlgorithm());
			final byte[] hash = digest.digest(data);
			return Base64.encodeBase64(hash);
		} catch (NoSuchAlgorithmException e) {
			InternalExceptionManager.handleException(e, this, ErrorCode.CUTIL_NO_SUCH_HASHALG);
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] sign(byte[] messageData, PrivateKey privateKey) {
		try {
			Signature rsaSign = Signature.getInstance("SHA256withRSA", "BC");
			rsaSign.initSign(privateKey);
	        rsaSign.update(messageData);
	        byte[] signature = rsaSign.sign();
	        return signature;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
			InternalExceptionManager.handleException(e, this, ErrorCode.CUTIL_SIGN_ERROR);
			e.printStackTrace();
			return null;
		}
        
	}
	
	public boolean verify(byte[] signedData, PublicKey publicKey) {
		try {
			Signature rsaVerify = Signature.getInstance("SHA256withRSA", "BC");
	        rsaVerify.initVerify(publicKey);
	        rsaVerify.update(signedData);
	        return rsaVerify.verify(signedData);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
			InternalExceptionManager.handleException(e, this, ErrorCode.CUTIL_VERIFY_ERROR);
			e.printStackTrace();
			return false;
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
	
	/*
	 * If a key is null it will not be written
	 * If the AESParameters are null encryption will not be used
	 */
	public int writeRSAKeyPair(File destinationFile, PublicKey publicKey, PrivateKey privateKey, AESParameters cipherParams) {
		PemWriter pemWriter = null;
		if(!destinationFile.exists()) {
			try {
				destinationFile.createNewFile();
				pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(destinationFile.getAbsolutePath(), true)));
			} catch (IOException e) {
				InternalExceptionManager.handleException(e, this, ErrorCode.GEN_IO_ERROR);
				e.printStackTrace();
				return -3;
			}
		}
		
		if(publicKey != null) {
			PemObject pemObject = new PemObject("RSA PUBLIC KEY", ((RSAPublicKey) publicKey).getEncoded());
			try {
				pemWriter.writeObject(pemObject);
			} catch (IOException e) {
				InternalExceptionManager.handleException(e, this, ErrorCode.GEN_IO_ERROR);
				e.printStackTrace();
				return -2;
			}
		}
		
		if(privateKey != null) {
			PemObject pemObject = new PemObject("RSA PRIVATE KEY", ((RSAPublicKey) privateKey).getEncoded());
			try {
				pemWriter.writeObject(pemObject);
			} catch (IOException e) {
				InternalExceptionManager.handleException(e, this, ErrorCode.GEN_IO_ERROR);
				e.printStackTrace();
				return -1;
			}
		}
		
		if(cipherParams != null) {
			//Encrypt file
			if(!Sonorous.getLoadedModules().contains(Module.CRYPTO)) {
				InternalExceptionManager.handleException(this, ErrorCode.MODULE_NOT_LOADED);
				return -4;
			}
			
			IOTask writeTask = new IOTask(new File[] { destinationFile }, destinationFile.getParentFile().getAbsoluteFile(), cipherParams);
			int returnCode = Sonorous.getIOManager().startTask(writeTask);
			return returnCode;
		}
		
		return 0;
	}
	
	@Deprecated
	public int writeRSAKeyPair(PublicKey publicKey, PrivateKey privateKey, File outFile, boolean usePKCS1) {
		HashMap<String, String> keyPair = new HashMap<String, String>();
		if(usePKCS1) {
			HashMap<String, byte[]> keyPairPKCS1 = formatKeyPairAsPKCS1(publicKey, privateKey);
			if(publicKey != null) {
				keyPair.put("PUBLIC", Base64.encodeBase64String(keyPairPKCS1.get("PUBLIC")));
			}
			
			if(privateKey != null) {
				keyPair.put("PRIVATE", Base64.encodeBase64String(keyPairPKCS1.get("PRIVATE")));
			}
		} else {
			if(publicKey != null) {
				keyPair.put("PUBLIC", Base64.encodeBase64String(publicKey.getEncoded()));
			}
			
			if(privateKey != null) {
				//Convert to PKCS8
				try {
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
			        KeyFactory kf = KeyFactory.getInstance("RSA");
			        byte[] pkcs8data = kf.generatePrivate(spec).getEncoded();
					keyPair.put("PRIVATE", Base64.encodeBase64String(privateKey.getEncoded()));
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
					return -4;
				}
			}
		}
		
		if(keyPair.size() == 0)
			return -3;
		
		if(outFile.exists()) {
			return -2;
		} else {
			try {
				boolean success = outFile.createNewFile();
				if(!success)
					return -2;
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			}
		}
		
		try {
			BufferedWriter pemWriter = new BufferedWriter(new FileWriter(outFile));
			if(keyPair.containsKey("PUBLIC")) {
				String header = (usePKCS1 ? "---BEGIN PKCS1 PUBLIC KEY---" : "---BEGIN X509 PUBLIC KEY---");
				String footer = (usePKCS1 ? "---END PKCS1 PUBLIC KEY---" : "---END X509 PUBLIC KEY---");
				
				pemWriter.write(header + "\n");
				pemWriter.write(keyPair.get("PUBLIC"));
				pemWriter.write(footer + "\n");
			}
			
			if(keyPair.containsKey("PRIVATE")) {
				String header = (usePKCS1 ? "---BEGIN PKCS1 PRIVATE KEY---" : "---BEGIN PKCS8 PRIVATE KEY---");
				String footer = (usePKCS1 ? "---END PKCS1 PRIVATE KEY---" : "---END PKCS8 PRIVATE KEY---");
				
				pemWriter.write(header + "\n");
				pemWriter.write(keyPair.get("PRIVATE"));
				pemWriter.write(footer + "\n");
			}
			
			pemWriter.close();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

}
