package res;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import crypto.CipherFactory;
import crypto.CipherUtil;

public class Sonorous {
	
	/*
	 * Primary class for the Sonorous API
	 */
	
	//Static variables
	private static File currentDirectoryPath;
	private static HashMap<String, String> PROPERTIES;
	protected static File sonorousLogFilePath = new File("Sonorous.log");
	
	//Sonorous non-static components
	private static CipherFactory thread_CipherFactory = null;
	private static CipherUtil thread_CipherUtil = null;
	
	public static void setProperty() {
		
	}
	
	public static void getProperty() {
		
	}
	
	public static int initialize(Module module) {
		PROPERTIES = new HashMap<String, String>();
		
		switch(module) {
			case BASE: return initializeSonorousBase();
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
	
	private static int initializeSonorousBase() {
		try {
			//Get current working directory
			currentDirectoryPath = new File(Sonorous.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return -2;
		}
		
		boolean isLogInitialized = Log.initialize(true, sonorousLogFilePath);
		boolean isThreadManagerInitialized = ThreadManager.initialize();
		boolean isExceptionManagerInitialized = InternalExceptionManager.initialize() == 0;
		if(!isLogInitialized || !isThreadManagerInitialized || !isExceptionManagerInitialized) {
			System.out.println("[Sonorous] Failed to initialized Sonorous base modules!");
			System.out.println("[Sonorous] Log module: " + isLogInitialized + ", Thread Manager module: " +
			isThreadManagerInitialized + ", Exception Manager module: " + isExceptionManagerInitialized + ".");
			return -1;
		} else {
			Log.write("Registered base modules!");
			return 0;
		}
	}
	
	private static int initializeSonorousCrypto() {
		
	}
	
	public static CipherFactory getCipherFactory() {
		return thread_CipherFactory;
	}
	
	public static CipherUtil getCipherUtil() {
		return thread_CipherUtil;
	}

}
