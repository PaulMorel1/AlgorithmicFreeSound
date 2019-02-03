package com.algorithmicaudio.freesound.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 
 * Streamlined library for running basic web requests. The purpose of this
 * class is to abstract away the basic web request logic needed to use the
 * FreeSound API. This is not intended as a fully featured web request
 * library, and will probably break if exposed to a stiff breeze.
 *
 */
public class Remote
{
	/**
	 *  This function from from https://rest.elkstein.org/2008/02/using-rest-in-java.html.
	 *  There's probably a better way to do this, but I like having no additional
	 *  dependencies.
	 *  
	 *  @param	urlStr	The URL to GET.
	 *  @return			The content fetched from the URL.
	 */
	public static String httpGet(String urlStr)
	{
		try
		{
			URL url = new URL(urlStr);
		  
			// create the http connection
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000); // set the timeout to 5 seconds
			conn.setReadTimeout(30000); // set the read timeout to 30 seconds

			// hit the URL
			if (conn.getResponseCode() != 200)
			{
				System.out.println("Response Code " + conn.getResponseCode() + ": " + conn.getResponseMessage());
				return "";
			}
			else
			{
				// Buffer the result into a string
				BufferedReader rd = new BufferedReader( new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = rd.readLine()) != null)
				{
					sb.append(line);
				}
				rd.close();
				conn.disconnect();
		  
				return sb.toString();
			}
		}
		catch(Exception ex)
		{
			System.out.println("Error while trying to fetch " + urlStr);
			System.out.println(ex.getMessage());
		}
		return "";
	}
	
	public static boolean httpGetBinary(String urlStr, String newFilename, String userAccessToken)
	{
		try
		{
			URL url = new URL(urlStr);

			// Get an HttpURLConnection subclass object instead of URLConnection
			HttpURLConnection myHttpConnection = (HttpURLConnection) url.openConnection();

			myHttpConnection.setInstanceFollowRedirects(false);
			myHttpConnection.setRequestMethod("GET");
			myHttpConnection.setDoOutput(true);
			myHttpConnection.setRequestProperty("Authorization", "Bearer " + userAccessToken);

            // opens input stream from the HTTP connection
            InputStream inputStream = myHttpConnection.getInputStream();
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(newFilename);
 
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// See https://stackoverflow.com/questions/4205980/java-sending-http-parameters-via-post-method-easily
	public static String httpPost(String urlStr, HashMap<String, String> data)
	{
		try
		{
			URL url = new URL(urlStr);

			String formData = Remote.makeParameters(data);
			byte[] postData = formData.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;

			// Get an HttpURLConnection subclass object instead of URLConnection
			HttpURLConnection myHttpConnection = (HttpURLConnection) url.openConnection();

			myHttpConnection.setInstanceFollowRedirects(false);
			myHttpConnection.setRequestMethod("POST");
			myHttpConnection.setDoOutput(true);
			myHttpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myHttpConnection.setRequestProperty("charset", "utf-8");
			myHttpConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			myHttpConnection.setUseCaches(false);

			// write the post data to the connection
			DataOutputStream outputStream = new DataOutputStream(myHttpConnection.getOutputStream());
			outputStream.write(postData);
			outputStream.flush();

			// open the contents of the URL as an inputStream and print to stdout
			String output = "";
			String input = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(myHttpConnection.getInputStream()));
			while ((input = in.readLine()) != null) {
				output += input;
			}
			in.close();

			return output;
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Return a parameter string that is formatted for use in an HTTP request.
	 * 
	 * See https://stackoverflow.com/questions/14551194/how-are-parameters-sent-in-an-http-post-request
	 * 
	 * @param	data	A HashMap containing the key/value pairs to be used in the request.
	 * @return			A String that is formated like "a=1&b=2"
	 */
	public static String makeParameters(HashMap<String, String> data)
	{
		if( data.isEmpty() ) return "";
		
		String params = "";
		String joiner = "";
		for(String key : data.keySet())
		{
			if( params.length() != 0 ) joiner = "&";
			params += joiner + key + "=" + URLEncoder.encode(data.get(key));
		}
		return params;
	}
}
