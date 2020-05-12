package res;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	
	private static SimpleDateFormat timestampFormatShort;
	private static SimpleDateFormat timestampFormatLong;
	
	private static File logFile;
	private static BufferedWriter logWriter;
	private static boolean useLogFile;
	
	public static boolean initialize(boolean writeLog, File logFile) {
		useLogFile = writeLog;
		timestampFormatShort = new SimpleDateFormat("HH:mm:ss");
		timestampFormatLong = new SimpleDateFormat("dd.MM.yyyy.HH:mm:ss");
		
		if(writeLog && logFile != null) {
			try {
				if(!logFile.exists())
					logFile.createNewFile();
				
				logWriter = new BufferedWriter(new FileWriter(logFile, logFile.exists()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return (writeLog ? logWriter != null : true);
	}
	
	protected static String getTimestampShort() {
		return new String("[" + timestampFormatShort.format(new Date()) + "] ");
	}
	
	protected static String getTimestampLong() {
		return new String("[" + timestampFormatLong.format(new Date()) + "] ");
	}
	
	public static void write(String message) {
		System.out.println(getTimestampShort() + message + "\n");
		
		if(useLogFile) {
			try {
				logWriter.write(getTimestampLong() + message + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void error(String message) {
		System.out.println(getTimestampShort() + "[ERROR] " + message + "\n");
		
		if(useLogFile) {
			try {
				logWriter.write(getTimestampLong() + "[ERROR] " + message + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
