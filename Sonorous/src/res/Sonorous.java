package res;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import crypto.CipherFactory;
import crypto.CipherUtil;
import io.IOManager;

public class Sonorous {
	
	/*
	 * Primary class for the Sonorous API
	 */
	
	//Static variables
	private static File currentDirectoryPath;
	private static HashMap<String, String> PROPERTIES;
	protected static File sonorousLogFilePath;
	protected static ArrayList<Module> loadedModules;
	
	//Sonorous non-static components
	private static CipherFactory thread_CipherFactory = null;
	private static CipherUtil thread_CipherUtil = null;
	private static IOManager thread_IOManager = null;
	
	public static void setProperty(String key, String value) {
		if(PROPERTIES == null) {
			PROPERTIES = new HashMap<String, String>();
		}
		
		PROPERTIES.put(key, value);
	}
	
	public static String getProperty(String key) {
		if(PROPERTIES != null) {
			if(PROPERTIES.containsKey(key)) {
				return PROPERTIES.get(key);
			}
		}
		
		return "";
	}
	
	public static int initialize(Module module) {
		if(PROPERTIES == null) {
			PROPERTIES = new HashMap<String, String>();
		}
		
		switch(module) {
			case BASE: return initializeSonorousBase(); 
			case CRYPTO: return initializeSonorousCrypto();
			case IO: return initializeSonorousIO();
			default: return -999;
		}
	}
	
	private static int initializeSonorousBase() {
		loadedModules = new ArrayList<Module>();
		
		try {
			//Get current working directory
			currentDirectoryPath = new File(Sonorous.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return -2;
		}
		
		boolean isLogInitialized = false;
		if(PROPERTIES.containsKey("USE_LOG_FILE")) {
			if(Boolean.parseBoolean(PROPERTIES.get("USE_LOG_FILE")) == true) {
				if(PROPERTIES.containsKey("LOG_FILE_PATH")) {
					sonorousLogFilePath = new File(PROPERTIES.get("LOG_FILE_PATH"));
					isLogInitialized = Log.initialize(true, sonorousLogFilePath);
				} else {
					sonorousLogFilePath = new File(currentDirectoryPath.getAbsolutePath() + "/sonorous.log");
					isLogInitialized = Log.initialize(true, sonorousLogFilePath);
				}
			} else {
				isLogInitialized = Log.initialize(false, null);
			}
		} else {
			isLogInitialized = Log.initialize(false, null);
		}
		
		boolean isThreadManagerInitialized = ThreadManager.initialize();
		boolean isExceptionManagerInitialized = InternalExceptionManager.initialize() == 0;
		if(!isLogInitialized || !isThreadManagerInitialized || !isExceptionManagerInitialized) {
			System.out.println("[Sonorous] Failed to initialized Sonorous base modules!");
			System.out.println("[Sonorous] Log module: " + isLogInitialized + ", Thread Manager module: " +
			isThreadManagerInitialized + ", Exception Manager module: " + isExceptionManagerInitialized + ".");
			return -1;
		} else {
			loadedModules.add(Module.BASE);
			Log.write("Registered base modules!");
			return 0;
		}
	}
	
	private static int initializeSonorousCrypto() {
		if(!loadedModules.contains(Module.BASE)) {
			return -2;
		}
		
		thread_CipherFactory = new CipherFactory();
		int isCipherFactoryInitialized = thread_CipherFactory.initialize();
		
		thread_CipherUtil = new CipherUtil();
		int isCipherUtilInitialized = thread_CipherUtil.initialize();
		if(isCipherFactoryInitialized != 0 || isCipherUtilInitialized != 0) {
			Log.error("Failed to initialize cryptographic modules!");
			Log.error("CipherFactory: " + isCipherFactoryInitialized + ", CipherUtil: " + isCipherUtilInitialized);
			return -1;
		} else {
			loadedModules.add(Module.CRYPTO);
			Log.write("Registered cryptographic modules!");
		}
		
		return 0;
	}
	
	private static int initializeSonorousIO() {
		thread_IOManager = new IOManager();
		int returnCode = thread_IOManager.initialize();
		if(returnCode != 0) {
			return returnCode;
		}
		
		if(getProperty("MAX_SIMULTANEOUS_STREAMS").equals("") == false) {
			thread_IOManager.MAX_SIMULTANEOUS_STREAMS = Integer.parseInt(getProperty("MAX_SIMULTANEOUS_STREAMS"));
		}
		
		if(getProperty("IO_UPDATE_FREQUENCY").equals("") == false) {
			thread_IOManager.UPDATE_FREQUENCY = Integer.parseInt(getProperty("IO_UPDATE_FREQUENCY"));
		}
		
		loadedModules.add(Module.IO);
		thread_IOManager.exec();
		return 0;
	}
	
	public static ArrayList<Module> getLoadedModules() {
		return loadedModules;
	}
	
	public static CipherFactory getCipherFactory() {
		return thread_CipherFactory;
	}
	
	public static CipherUtil getCipherUtil() {
		return thread_CipherUtil;
	}
	
	public static IOManager getIOManager() {
		return thread_IOManager;
	}

}
