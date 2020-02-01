package com.amazonaws.transcribestreaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Main {
	
    public static RecordInterpreter interpreter;
    private static myGUI window;
    public static int counter;
	private static String countFilePath; 
	private static String logFilePath;
	private static boolean DEBUG = false;

	public static void main(String[] args) {
		if (args.length > 0 && (args[0].equalsIgnoreCase("-debug")))
    	{
    		DEBUG = true;
    		String directoryPath = "./RSDebug";
    		countFilePath = directoryPath + "/count.out";
    		logFilePath = directoryPath + "/RSDebug.out";
    		
    		File countFile = new File(countFilePath);
    		File logFile = new File(logFilePath);
    		
    		if(!Files.exists(Paths.get(directoryPath)))
    		{
    			new File(directoryPath).mkdirs();
    			logFile = new File(logFilePath);
    			counter = 0;
    			try
    			{
    				logFile.createNewFile();
    				countFile.createNewFile();
    				Files.write(Paths.get(countFile.getPath()), "0".getBytes(),StandardOpenOption.WRITE);
    			} catch (IOException e)
    			{
    				System.out.println("Could not generate log and/or count files: ");
    				e.printStackTrace();
    			}
    		}
    		else
    		{
    			try
    			{
    				Scanner scanner = new Scanner(countFile);
    				counter = Integer.parseInt(scanner.next());
    				scanner.close();
    			} catch (FileNotFoundException e)
    			{
    				System.out.println("Error retrieving counter information. Counter reset to 0.");
    				e.printStackTrace();
    				counter = 0;
    			}
    		}
    	}
    	interpreter = new RecordInterpreter();
    	window = new myGUI();
    	window.render();
	}
	
	public static void writeToLog(String s)
    {
    	if (DEBUG == true)
    	{
        	try
        	{
    			Files.write(Paths.get(logFilePath), s.getBytes(), StandardOpenOption.APPEND);
    		} catch (IOException e)
        	{
    			System.out.println("Unable to write to log file");
    			e.printStackTrace();
    		}
    	}
    }
    
    
    public static void writeToCountFile(int i)
    {
    	if (DEBUG == true)
    	{
    		try
    		{
				Files.write(Paths.get(countFilePath), Integer.toString(i).getBytes(),StandardOpenOption.WRITE);
			} catch (IOException e)
    		{
				System.out.println("Unable to write to count file");
				e.printStackTrace();
			}
    	}
    }

}
