package io;

public class QueuedOperation {
	
	private int taskID, streamID;
	
	public QueuedOperation(int taskID, int streamID) {
		this.taskID = taskID;
		this.streamID = streamID;
	}

	public int getTaskID() {
		return taskID;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public int getStreamID() {
		return streamID;
	}

	public void setStreamID(int streamID) {
		this.streamID = streamID;
	}

}
