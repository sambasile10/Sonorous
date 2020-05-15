package io;

import java.io.File;

import crypto.AESParameters;

/*
 * Used by IOManager to create I/O threads
 * 
 * Regarding AESParameters - CipherBlockMode will BE IGNORED, CTR only
 */

public class IOTask {
	
	private File[] filesQueue;
	private File destinationFolder;
	private AESParameters cipherParams;
	
	public IOTask(File[] filesQueue, File destinationFolder, AESParameters cipherParams) {
		this.filesQueue = filesQueue;
		this.destinationFolder = destinationFolder;
		this.cipherParams = cipherParams;
	}

	public File[] getFilesQueue() {
		return filesQueue;
	}

	public void setFilesQueue(File[] filesQueue) {
		this.filesQueue = filesQueue;
	}

	public File getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(File destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public AESParameters getCipherParams() {
		return cipherParams;
	}

	public void setCipherParams(AESParameters cipherParams) {
		this.cipherParams = cipherParams;
	}

}
