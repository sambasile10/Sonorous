package res;

public abstract class ManagedThread extends Thread {
	
	private int threadID;
	private String className;
	
	public ManagedThread(String className) {
		this.threadID = ThreadManager.register(this);
		this.className = className;
		Log.write("Registered new thread [ " + className + "] with ID [" + threadID + "]");
	}
	
	//Override all abstract functions
	public abstract int initialize();
	
	public void exec() {
		this.start();
	}
	
	public abstract int halt();
	
	//Unregister function must be at the end of the halt() function
	public int unregister() {
		Thread.currentThread().interrupt();
		if(this.isInterrupted()) {
			return (ThreadManager.unregister(threadID) == 0 ? 0 : -2);
		} else {
			return -1;
		}
	}
	
	public int getThreadID() {
		return threadID;
	}
	

}
