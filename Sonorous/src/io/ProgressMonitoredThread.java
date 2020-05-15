package io;

public interface ProgressMonitoredThread {
	
	//Returns progress from 0.0 to 100.0%
	public float getProgress();
	
	//Returns the 'stage' of the thread
	public int getStage();

}
