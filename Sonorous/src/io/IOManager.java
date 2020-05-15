package io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import crypto.CipherMode;
import res.ManagedThread;

public class IOManager extends ManagedThread {
	
	private HashMap<Integer, IOTask> ACTIVE_TASKS;
	private HashMap<File, Integer> FILE_STREAM_ID;
	private HashMap<Integer, FileEncryptionStream> ENCRYPTION_STREAMS;
	private HashMap<Integer, FileDecryptionStream> DECRYPTION_STREAMS;
	
	private Random random;
	
	public IOManager() {
		super("IOManager");
	}
	
	@Override
	public int initialize() {
		ACTIVE_TASKS = new HashMap<Integer, IOTask>();
		FILE_STREAM_ID = new HashMap<File, Integer>();
		ENCRYPTION_STREAMS = new HashMap<Integer, FileEncryptionStream>();
		DECRYPTION_STREAMS = new HashMap<Integer, FileDecryptionStream>();
		
		this.random = new Random();
		return 0;
	}
	
	@Override
	public int halt() {
		if(ACTIVE_TASKS.size() == 0) {
			return this.unregister();
		} else {
			return -1;
		}
	}
	
	public int startTask(IOTask task) {
		int taskID = -1;
		do {
			taskID = random.nextInt(1000-1) + 1;
		} while(ACTIVE_TASKS.containsKey(taskID) && taskID == -1);
		
		ArrayList<Integer> streamIDs = new ArrayList<Integer>();
		for(File file : task.getFilesQueue()) {
			int streamID = -1;
			if(task.getCipherParams().getCipherMode() == CipherMode.ENCRYPT) {
				do {
					streamID = random.nextInt(10000-1) + 1;
				} while(ENCRYPTION_STREAMS.containsKey(streamID) && streamID == -1);
				
				FILE_STREAM_ID.put(file, streamID);
				File destinationFile = new File(task.getDestinationFolder().getAbsolutePath() + "/" + file.getName() + ".pack");
				FileEncryptionStream encryptionStream = new FileEncryptionStream(file, destinationFile, task.getCipherParams());
				int initCode = encryptionStream.initialize();
				
				/*
				 * TODO Alot left to do 
				 */
				
			} else if(task.getCipherParams().getCipherMode() == CipherMode.DECRYPT) {
				do {
					streamID = random.nextInt(10000-1) + 1;
				} while(DECRYPTION_STREAMS.containsKey(streamID) && streamID == -1);
				
				FILE_STREAM_ID.put(file, streamID);
				File destinationFile = new File(task.getDestinationFolder().getAbsolutePath() + "/" + file.getName() + ".pack");
				FileDecryptionStream decryptionStream = new FileDecryptionStream(file, destinationFile, task.getCipherParams());
			}
		}
		
		ACTIVE_TASKS.put(taskID, task);
	}

}
