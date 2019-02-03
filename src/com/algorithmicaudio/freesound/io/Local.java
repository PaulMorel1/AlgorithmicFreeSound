package com.algorithmicaudio.freesound.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class makes the saving/loading of API credentials a little easier.
 */
public class Local
{
	public static boolean writeTextToFile(String filename, String content)
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"))) 
		{
			writer.write(content);
		}
		catch(IOException ie)
		{
			return false;
		}
		return true;
	}
	
	public static String readTextFromFile(String filename)
	{
		String output = "";
		try
		{
			File file = new File(filename);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String nextLine = "";
			while ((nextLine = br.readLine()) != null) 
			{
				output = output + nextLine; 
			}
			br.close();
		}
		catch(IOException ie)
		{
			/*
			 * We are ignoring local io errors here in order to simplify the flow for inexperienced users.
			 * It may be preferable to uncomment the error printing code below or throw the exception to
			 * the calling class.
			 */
			//System.out.println("Error while reading " + filename);
			//System.out.println(ie.getMessage());
		}
		return output;
	}
}
