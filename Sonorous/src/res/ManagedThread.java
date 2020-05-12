package res;

public abstract class ManagedThread extends Thread {
	
	private int threadID;
	private String threadName;
	
	public ManagedThread(String className) {
		this.threadID = ThreadManager.register(this);
		this.threadName = className;
		Log.write("Registered new thread [ " + className + "] with ID [" + threadID + "]");
	}
	
	//Override all abstract functions
	public abstract int initialize();
	
	public void exec() {
		this.start();
	}
	
	public abstract int halt();
	

}
