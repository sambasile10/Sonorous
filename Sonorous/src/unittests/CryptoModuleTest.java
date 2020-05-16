package unittests;

import java.security.KeyPair;
import java.util.Random;

import org.apache.commons.codec.Charsets;

import crypto.AESCipherStream;
import crypto.AESParameters;
import crypto.CipherBlockMode;
import crypto.CipherMode;
import crypto.RSACipherStream;
import io.IOManager;
import res.ErrorCode;
import res.InternalExceptionManager;
import res.Module;
import res.Sonorous;

public class CryptoModuleTest {
	
	public static void main(String[] args) {
		Sonorous.initialize(Module.BASE);
		Sonorous.initialize(Module.CRYPTO);
		
		
		
		aesTest();
		System.out.println("-------------------------------------------");
		rsaTest();
	}
	
	public static void aesTest() {
		String testString = "test1 test2 test 3 ....... test 4 test 5 abcdefg 1234567890"; 
		String password = "test12345", salt = "abcdefghjk";
		
		Random random = new Random();
		byte[] iv = new byte[16];
		random.nextBytes(iv);
		
		AESParameters encryptionParams = new AESParameters(password, salt,
				iv, 256, CipherMode.ENCRYPT, CipherBlockMode.CBC);
		
		AESParameters decryptionParams = new AESParameters(password, salt,
				iv, 256, CipherMode.DECRYPT, CipherBlockMode.CBC);
		
		AESCipherStream encryptionStream = Sonorous.getCipherFactory().buildAESStream(encryptionParams, true);
		AESCipherStream decryptionStream = Sonorous.getCipherFactory().buildAESStream(decryptionParams, true);
		
		System.out.println("input: " + testString);
		byte[] cData = encryptionStream.pipe(testString.getBytes(Charsets.UTF_8));
		System.out.println("encrypted: " + new String(cData, Charsets.UTF_8));
		byte[] dFinal = decryptionStream.pipe(cData);
		System.out.println("final: " + new String(dFinal, Charsets.UTF_8));
	}
	
	public static void rsaTest() {
		KeyPair keyPair = Sonorous.getCipherUtil().generateKeyPair(2048);
		
		RSACipherStream encryptionStream = Sonorous.getCipherFactory().buildRSAStream(CipherMode.ENCRYPT, keyPair.getPublic(), true);
		RSACipherStream decryptionStream = Sonorous.getCipherFactory().buildRSAStream(CipherMode.DECRYPT, keyPair.getPrivate(), true);
		
		String testString = "test1 test2 test 3 ....... test 4 test 5 abcdefg 1234567890";
		System.out.println("input: " + testString);
		byte[] cData = encryptionStream.pipe(testString.getBytes(Charsets.UTF_8));
		System.out.println("encrypted: " + new String(cData, Charsets.UTF_8));
		byte[] dFinal = decryptionStream.pipe(cData);
		System.out.println("final: " + new String(dFinal, Charsets.UTF_8));
		
	}

}
