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

public class FileEncryptionStream extends ManagedThread {
	
	protected final int BUFFER_SIZE = 4096 * 1024; //4MB buffer
	
	private int threadID;
	private String threadName;
	
	private File inputFile, destinationFile;
	private AESParameters cipherParams;
	private AESCipherStream cipherStream = null;
	
	private boolean isCompleted = false;
	
	public FileEncryptionStream(File inputFile, File destinationFile, AESParameters cipherParams) {
		super("FileEncryptionStream");
		this.inputFile = inputFile;
		this.destinationFile = destinationFile;
		this.cipherParams = cipherParams;
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
		int returnCode = this.createPackage();
	}
	
	private int createPackage() {
		/*
		 * Stage 1 - Compress the original file
		 */
		
		//Create temporary file
		String systemTempPath = System.getProperty("java.io.tmpdir");
		File tempFile = new File(systemTempPath + "/" + this.inputFile.getName() + ".tmp");
		
		//Compress input file to the temp file
		ZipUtil.packEntry(this.inputFile, tempFile);
		
		if(!tempFile.exists()) {
			//Compression process failed
			return -4;
		}
		
		/*
		 * Stage 2 - Encryption Stream to destination file
		 */
		
		//Calculate stream properties
		long tempFileSize = tempFile.length();
		long numBlocks, finalBlockLength;
		if(tempFileSize <= BUFFER_SIZE) {
			//File is smaller than the buffer size, only use one block
			numBlocks = 1;
			finalBlockLength = tempFileSize;
		} else {
			//File is larger than the buffer size
			numBlocks = (long) Math.ceil((double)(tempFileSize / BUFFER_SIZE)); //Number of blocks to encrypt and write
			finalBlockLength = (long)(tempFileSize % BUFFER_SIZE); //Size of the final block in bytes
		}
		
		//Create file I/O streams
		FileInputStream inputStream;
		FileOutputStream outputStream;
		try {
			inputStream = new FileInputStream(tempFile);
			outputStream = new FileOutputStream(destinationFile);
		} catch (IOException e) {
			e.printStackTrace();
			return -3;
		}
		
		//Read from temp file and encrypt then write to destination file for the number of blocks
		long actualBufferSize = BUFFER_SIZE;
		for(long index = 0L; index < numBlocks; index++) {
			if(index == numBlocks - 1) {
				//Final block, update buffer size
				actualBufferSize = finalBlockLength;
			}
			
			//Create read buffer and read from temp file
			byte[] readBuffer = new byte[(int)actualBufferSize];
			try {
				inputStream.read(readBuffer, 0, (int)actualBufferSize);
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			}
			
			//Encrypt read data then write to destination file
			byte[] cData = cipherStream.pipe(readBuffer);
			try {
				outputStream.write(cData);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		/*
		 * Stage 3 - Cleanup data
		 */
		
		//Check if the output file seems valid
		if(!(destinationFile.length() == 0)) {
			//Doesn't check out, length 0
			return -6;
		}
		
		//Close data streams
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
