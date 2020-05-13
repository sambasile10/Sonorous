package res;

import java.io.File;

import crypto.CipherFactory;
import crypto.CipherUtil;

public class Sonorous {
	
	/*
	 * Primary class for the Sonorous API
	 */
	
	//Static variables
	protected static File sonorousLogFilePath = new File("Sonorous.log");
	
	//Sonorous non-static components
	private static CipherFactory thread_CipherFactory = null;
	private static CipherUtil thread_CipherUtil = null;
	
	public static int initialize() {
		boolean isLogInitialized = Log.initialize(true, sonorousLogFilePath);
		boolean isThreadManagerInitialized = ThreadManager.initialize();
		if(!isLogInitialized || !isThreadManagerInitialized) {
			System.out.println("[Sonorous] Failed to initialized Sonorous base modules!");
			System.out.println("[Sonorous] Log module: " + isLogInitialized + ", Thread Manager module: " + isThreadManagerInitialized);
			return -3;
		} else {
			Log.write("Registered base modules!");
		}
		
		thread_CipherFactory = new CipherFactory();
		int isCipherFactoryInitialized = thread_CipherFactory.initialize();
		
		thread_CipherUtil = new CipherUtil();
		int isCipherUtilInitialized = thread_CipherUtil.initialize();
		if(isCipherFactoryInitialized != 0 || isCipherUtilInitialized != 0) {
			Log.error("Failed to initialize cryptographic modules!");
			Log.error("CipherFactory: " + isCipherFactoryInitialized + ", CipherUtil: " + isCipherUtilInitialized);
			return -2;
		} else {
			Log.write("Registered cryptographic modules!");
		}
		
		return 0;
	}
	
	public static CipherFactory getCipherFactory() {
		return thread_CipherFactory;
	}
	
	public static CipherUtil getCipherUtil() {
		return thread_CipherUtil;
	}

}
