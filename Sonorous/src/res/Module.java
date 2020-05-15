package res;
import io.*;
import crypto.*;

/*
 * Sonorous library module
 */

public enum Module {
	
	BASE(0, "Required modules", "Initializes the Logging system, Thread Manager, and the Exception Handler.",
			new Class[] { Log.class, ThreadManager.class, InternalExceptionManager.class }),
	
	CRYPTO(1, "Cryptographic modules", "Initializes the cryptograhic modules required for most other modules", 
			new Class[] { AESCipherStream.class, CipherFactory.class, CipherUtil.class, 
					RSACipherStream.class }),
	
	IO(2, "I/O related modules", "Initializes the modules that utilize file streams", 
			new Class[] { FileEncryptionStream.class, FileDecryptionStream.class });
	
	private int id;
	private String name, description;
	private Class[] moduleObjects;
	
	Module(int id, String name, String description, Class[] moduleObjects) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.moduleObjects = moduleObjects;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Class[] getModuleObjects() {
		return moduleObjects;
	}

}
