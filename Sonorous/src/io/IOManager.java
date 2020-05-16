package io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import crypto.CipherMode;
import res.ErrorCode;
import res.InternalExceptionManager;
import res.ManagedThread;

public class IOManager extends ManagedThread {
	
	private HashMap<Integer, IOTask> ACTIVE_TASKS;
	private Queue<QueuedOperation> OPERATION_QUEUE;
	private LinkedList<Integer> ACTIVE_STREAMS;
	private HashMap<Integer, FileEncryptionStream> ENCRYPTION_STREAMS;
	private HashMap<Integer, FileDecryptionStream> DECRYPTION_STREAMS;
	
	public int MAX_SIMULTANEOUS_STREAMS = 4; //Default set to 4
	public int UPDATE_FREQUENCY = 2000; //Default every 2 seconds
	private boolean performIOLoop = false;
	
	private Random random;
	
	public IOManager() {
		super("IOManager");
	}
	
	@Override
	public int initialize() {
		ACTIVE_TASKS = new HashMap<Integer, IOTask>();
		OPERATION_QUEUE = new LinkedList<>();
		ACTIVE_STREAMS = new LinkedList<Integer>();
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
	
	@Override
	public void start() {
		this.performIOLoop = true;
		while(performIOLoop) {
			
			if(ACTIVE_STREAMS.size() > 0) {
				for(int streamID : ACTIVE_STREAMS) {
					
					if(ENCRYPTION_STREAMS.containsKey(streamID)) {
						int streamStatus = ENCRYPTION_STREAMS.get(streamID).getStage();
						if(streamStatus == 4) {
							ENCRYPTION_STREAMS.get(streamID).halt();
							ACTIVE_STREAMS.remove(streamID);
							ENCRYPTION_STREAMS.remove(streamID);
						} else if(streamStatus == 5) {
							ENCRYPTION_STREAMS.get(streamID).halt();
							InternalExceptionManager.handleException(this, ErrorCode.IO_STREAM_DELETE_ERR);
							ACTIVE_STREAMS.remove(streamID);
							ENCRYPTION_STREAMS.remove(streamID);
						}
					} else if(DECRYPTION_STREAMS.containsKey(streamID)) {
						int streamStatus = DECRYPTION_STREAMS.get(streamID).getStage();
						if(streamStatus == 4) {
							DECRYPTION_STREAMS.get(streamID).halt();
							ACTIVE_STREAMS.remove(streamID);
							ENCRYPTION_STREAMS.remove(streamID);
						} else if(streamStatus == 5) {
							DECRYPTION_STREAMS.get(streamID).halt();
							InternalExceptionManager.handleException(this, ErrorCode.IO_STREAM_DELETE_ERR);
							ACTIVE_STREAMS.remove(streamID);
							ENCRYPTION_STREAMS.remove(streamID);
						}
					} else {
						InternalExceptionManager.handleException(this, ErrorCode.IOMAN_INVALID_STREAM_ID);
						continue;
					}
				}
			}
			
			if(ACTIVE_STREAMS.size() < MAX_SIMULTANEOUS_STREAMS && OPERATION_QUEUE.size() > 0) {
				QueuedOperation nextOperation = OPERATION_QUEUE.poll();
				if(ENCRYPTION_STREAMS.containsKey(nextOperation.getStreamID())) {
					ENCRYPTION_STREAMS.get(nextOperation.getStreamID()).exec();
					ACTIVE_STREAMS.add(nextOperation.getStreamID());
				} else if(DECRYPTION_STREAMS.containsKey(nextOperation.getStreamID())) {
					DECRYPTION_STREAMS.get(nextOperation.getStreamID()).exec();
					ACTIVE_STREAMS.add(nextOperation.getStreamID());
				} else {
					InternalExceptionManager.handleException(this, ErrorCode.IOMAN_INVALID_STREAM_ID);
					
				}
			}
			
			try {
				Thread.currentThread().sleep(UPDATE_FREQUENCY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int startTask(IOTask task) {
		int taskID = -1;
		do {
			taskID = random.nextInt(1000-1) + 1;
		} while(ACTIVE_TASKS.containsKey(taskID) && taskID == -1);
		
		for(File file : task.getFilesQueue()) {
			int streamID = -1;
			if(task.getCipherParams().getCipherMode() == CipherMode.ENCRYPT) {
				do {
					streamID = random.nextInt(10000-1) + 1;
				} while(ENCRYPTION_STREAMS.containsKey(streamID) && streamID == -1);
				
				File destinationFile = new File(task.getDestinationFolder().getAbsolutePath() + "/" + file.getName() + ".pack");
				FileEncryptionStream encryptionStream = new FileEncryptionStream(file, destinationFile, task.getCipherParams());
				int initCode = encryptionStream.initialize();
				
				if(initCode != 0) {
					InternalExceptionManager.handleException(this, ErrorCode.IOMAN_STREAM_INIT_ERR);
					return -2;
				} else {
					ENCRYPTION_STREAMS.put(streamID, encryptionStream);
					OPERATION_QUEUE.add(new QueuedOperation(taskID, streamID));
				}
				
			} else if(task.getCipherParams().getCipherMode() == CipherMode.DECRYPT) {
				do {
					streamID = random.nextInt(10000-1) + 1;
				} while(DECRYPTION_STREAMS.containsKey(streamID) && streamID == -1);
				
				File destinationFile = new File(task.getDestinationFolder().getAbsolutePath() + "/" + file.getName() + ".pack");
				FileDecryptionStream decryptionStream = new FileDecryptionStream(file, destinationFile, task.getCipherParams());
				int initCode = decryptionStream.initialize();
				
				if(initCode != 0) {
					InternalExceptionManager.handleException(this, ErrorCode.IOMAN_STREAM_INIT_ERR);
					return -2;
				} else {
					DECRYPTION_STREAMS.put(streamID, decryptionStream);
					OPERATION_QUEUE.add(new QueuedOperation(taskID, streamID));
				}
			}
		}
		
		ACTIVE_TASKS.put(taskID, task);
		return 0;
	}

}
