package res;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class ThreadManager {
	
	//Thread ID mapped to the thread object
	private static HashMap<Integer, ManagedThread> ACTIVE_THREADS;
	private static LinkedList<Integer> USED_THREAD_IDS;
	private static Random random;
	
	protected static final int MINIMUM_ID = 10, MAXIMUM_ID = 999;
	
	public static boolean initialize() {
		ACTIVE_THREADS = new HashMap<Integer, ManagedThread>();
		USED_THREAD_IDS = new LinkedList<Integer>();
		random = new Random();
		
		return true;
	}
	
	public static int register(ManagedThread thread) {
		int assignedID = -1;
		boolean continueLoop = true;
		while(continueLoop) {
			assignedID = random.nextInt(MAXIMUM_ID - MINIMUM_ID) + MINIMUM_ID;
			if(!USED_THREAD_IDS.contains(assignedID)) {
				continueLoop = false;
				break;
			}
		}
		
		if(assignedID == -1) {
			return -2;
		}
		
		ACTIVE_THREADS.put(assignedID, thread);
		if(ACTIVE_THREADS.containsKey(assignedID)) {
			return assignedID;
		} else {
			return -1;
		}
	}
	
	public static int unregister(int threadID) {
		if(ACTIVE_THREADS.containsKey(threadID)) {
			ACTIVE_THREADS.remove(threadID);
			USED_THREAD_IDS.remove(threadID);
			return 0;
		} else {
			return -1;
		}
	}

}
