package res;

public class InternalExceptionManager {
	
	public static int initialize() {
		return 0; //TODO
	}
	
	//TODO extend handling
	
	public static int handleException(Class classType, ErrorCode errorCode) {
		String errorMessage = generateErrorMessage(classType, null, errorCode, null);
		System.out.println(errorMessage);
		return 0;
	}
	
	public static int handleException(ManagedThread thread, ErrorCode errorCode) {
		String errorMessage = generateErrorMessage(thread.getClass(), thread, errorCode, null);
		System.out.println(errorMessage);
		return 0;
	}
	
	public static int handleException(Exception e, Class classType, ErrorCode errorCode) {
		String errorMessage = generateErrorMessage(classType, null, errorCode, e);
		System.out.println(errorMessage);
		return 0;
	}
	
	public static int handleException(Exception e, ManagedThread thread, ErrorCode errorCode) {
		String errorMessage = generateErrorMessage(thread.getClass(), thread, errorCode, e);
		System.out.println(errorMessage);
		return 0;
	}
	
	private static String generateErrorMessage(Class classType, ManagedThread thread, ErrorCode errorCode, Exception ex) {
		StringBuilder sb = new StringBuilder();
		sb.append("========================================================= \n");
		sb.append("InternalExceptionManager caught an exception! \n");
		sb.append("Exception major ID: " + errorCode.getMajorID() + ", minor ID: " + errorCode.getMinorID());
		sb.append("Exception description: " + errorCode.getDescription() + "\n");
		
		if(thread != null) {
			sb.append("Thrown by an instance of [" + classType.getName() + "] with ID: " + thread.getThreadID() + "\n");
		} else {
			sb.append("Thrown by static [" + classType.getName() + "] \n");
		}
		
		if(ex != null) {
			sb.append("--------------------------------------------------------- \n");
			sb.append("Java will catch this exception after the report: " + ex.getCause() + "\n");
			sb.append("Java stack trace begins here \n");
			
			for(int i = 0; i < ex.getStackTrace().length; i++) {
				sb.append(ex.getStackTrace()[i].toString() + "\n");
			}
		}
		
		sb.append("--------------------------------------------------------- \n");
		sb.append("This exception may cause errors to occur or cause a crash of this process. \n");
		sb.append("Continued execution may continue after this report. \n");
		sb.append("========================================================= \n");
		
		return sb.toString();
	}

}
