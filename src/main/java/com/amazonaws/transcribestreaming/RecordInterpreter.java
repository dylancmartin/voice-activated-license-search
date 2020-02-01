package com.amazonaws.transcribestreaming;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class RecordInterpreter {
	
	private String recordType;
	private String state;
	private String recordInfo;
	private HashMap<String, String> states;
	private HashMap<String, String> phoneticAlphabet;
	
	
	public RecordInterpreter()
	{
		this.recordType = "null";
		this.state = "null";
		this.recordInfo = "null";
		this.states = fillMap("states");
		this.phoneticAlphabet = fillMap("phoneticAlphabet");
	}
	
	
	private HashMap<String, String> fillMap(String fileName)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		try
		{
			BufferedReader reader;
			String line;
			String[] split;
			
			reader = new BufferedReader(new FileReader("vocabulary/" + fileName + ".txt"));
			
			while ((line = reader.readLine()) != null)
			{
				split = line.split(",");
				map.put(split[0], split[1]);
			}
			reader.close();
		} catch (FileNotFoundException e1)
		{
			System.out.println("The states.txt or phoneticAlphabet.txt is not present within the ./vocabulary/ directory");
		} catch (IOException e2)
		{
			e2.printStackTrace();
		}
		return map;
	}
	
	
	public String format(String record) throws Exception
	{
		String cleanRecord = clean(record);
		parse(cleanRecord);
		return constructRecordQuery();
	}
	
	
	private void parse(String s) throws Exception
	{
		String[] words = s.split(" ");
		if (s.length() <= 4) {
			displayError();
		}
		state = getState(words[1]);
		if ((state = getState(words[1])) != null)
		{
			parseRecordInfo(words, 0);
		}
		//In case Transcribe misses the command start "run"
		else if ((state = getState(words[0])) != null)
		{
			parseRecordInfo(words, -1);
		}
		else
		{
			displayError();
		}
	}
	
	
	private void parseRecordInfo(String[] words, int delta) throws Exception
	{
		String record;
		record = words[2 + delta].concat(" ".concat(words[3 + delta]));
		if (record.contentEquals("driver's license"))
		{
			setRecordType("OLN");
		}
		else if (record.contentEquals("license plate"))
		{
			setRecordType("LIS");
		}
		else
		{
			displayError();
		}
		
		constructRecordInfo(words, 4 + delta);
	}
	
	
	private void constructRecordInfo(String[] words, int start) throws Exception
	{
		int i;
		String letter, record = "";
		
		for (i = start; i < words.length; i++)
		{
			letter = getLetter(words[i]);
			try
			{
				if (letter == null && Integer.parseInt(words[i]) != -1)
				{
					letter = words[i];
				}
			} catch (NumberFormatException e1) {}//parseInt throws this exception if the string isn't a number
			if (letter != null)
			{
				record += letter;
			}
			else
			{
				displayError();
			}
		}
		recordInfo = record;
	}
	
	
	private String constructRecordQuery()
	{
		String str = "NCI Query: QQ.ORI/US0123456." + recordType + "/" + recordInfo + ".OLS/" + state;
		return str;
	}
	
	
	private String clean(String s)
	{
		return s.replace(",", "").replace(".", "").replace("?", "").replace("!", "").toLowerCase();
	}
	
	
	private void displayError() throws Exception
	{
		//used to stop the parsing process and display the error message on the GUI
		throw new Exception();
	}
	
	
	private String getState(String word)
	{
		return states.getOrDefault(word, null);
	}
	
	
	private String getLetter(String word)
	{
		return phoneticAlphabet.getOrDefault(word, null);
	}
	
	
	private void setRecordType(String type)
	{
		recordType = type;
	}
}