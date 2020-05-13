package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.zeroturnaround.zip.ZipUtil;

import crypto.AESCipherStream;
import crypto.AESParameters;
import res.Log;
import res.ManagedThread;
import res.Sonorous;
import res.ThreadManager;

public class FileDecryptionStream extends ManagedThread {
	
	protected final int BUFFER_SIZE = 4096 * 1024; //4MB buffer
	
	private int threadID;
	private String threadName;
	
	private File inputFile, destinationFile;
	private AESParameters cipherParams;
	private AESCipherStream cipherStream = null;
	
	private boolean isCompleted = false;
	
	public FileDecryptionStream(File inputFile, File destinationFile, AESParameters cipherParams) {
		super("FileDecryptionStream");
	}
	
	@Override
	public int initialize() {
		if(this.destinationFile.exists()) {
			Log.error("Given destination file cannot exist for file encryption!");
			return -4;
		}
		
		if(!this.inputFile.exists()) {
			Log.error("Given input file for encryption does not exist!");
			return -3;
		}
		
		this.cipherStream = Sonorous.getCipherFactory().buildAESStream(this.cipherParams, false);
		if(this.cipherStream == null) {
			return -2;
		}
		
		try {
			if(this.destinationFile.createNewFile()) {
				return 0;
			} else {
				return -1;
			}
		} catch (IOException e) {
			Log.error("Failed to create destination file!");
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public int halt() {
		if(!isCompleted) {
			return -3;
		}
		
		Thread.currentThread().interrupt();
		if(this.isInterrupted()) {
			return (ThreadManager.unregister(threadID) == 0 ? 0 : -2);
		} else {
			return -1;
		}
	}
	
	@Override
	public void start() {
		int returnCode = this.unpack();
	}
	
	private int unpack() {
		/*
		 * Stage 1 - Loop decryption and write to temp file
		 */

		// Create temporary file
		String systemTempPath = System.getProperty("java.io.tmpdir");
		File tempFile = new File(systemTempPath + "/" + this.inputFile.getName() + ".tmp");

		// Calculate stream properties
		long inputFileSize = inputFile.length();
		long numBlocks, finalBlockLength;
		if (inputFileSize <= BUFFER_SIZE) {
			// File is smaller than the buffer size, only use one block
			numBlocks = 1;
			finalBlockLength = inputFileSize;
		} else {
			// File is larger than the buffer size
			numBlocks = (long) Math.ceil((double) (inputFileSize / BUFFER_SIZE)); // Number of blocks to encrypt and write
			finalBlockLength = (long) (inputFileSize % BUFFER_SIZE); // Size of the final block in bytes
		}

		// Create file I/O streams
		FileInputStream inputStream;
		FileOutputStream outputStream;
		try {
			inputStream = new FileInputStream(this.inputFile);
			outputStream = new FileOutputStream(tempFile);
		} catch (IOException e) {
			e.printStackTrace();
			return -3;
		}

		// Read from temp file and encrypt then write to destination file for the number of blocks
		long actualBufferSize = BUFFER_SIZE;
		for (long index = 0L; index < numBlocks; index++) {
			if (index == numBlocks - 1) {
				// Final block, update buffer size
				actualBufferSize = finalBlockLength;
			}

			// Create read buffer and read from temp file
			byte[] readBuffer = new byte[(int) actualBufferSize];
			try {
				inputStream.read(readBuffer, 0, (int) actualBufferSize);
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			}

			// Encrypt read data then write to destination file
			byte[] cData = cipherStream.pipe(readBuffer);
			try {
				outputStream.write(cData);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		/*
		 * Stage 2 - Decompress temp file to the destination file
		 */
		
		ZipUtil.unpack(tempFile, destinationFile);
		
		/*
		 * Stage 3 - Cleanup and verify
		 */
		
		//Check if the destination file has a valid length
		if(destinationFile.length() == 0) {
			return -6;
		}
		
		//Close I/o streams
		try {
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return -5;
		}
		
		cipherStream.halt();
		
		//Delete temporary file
		if(tempFile.delete()) {
			//Successfully completed all tasks
			return 0;
		} else {
			//Failed to delete temporary file, but still wrote out
			return 1;
		}
	}

}
