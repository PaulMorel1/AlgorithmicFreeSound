package com.algorithmicaudio.freesound.example;

import javax.security.auth.login.CredentialNotFoundException;
import com.algorithmicaudio.freesound.FreeSound;
import com.algorithmicaudio.freesound.response.SearchResponse;

/*
 * This example demonstrates how to search for sounds using a tag.
 */
public class SearchByKey
{
	public static void main(String[] args) throws CredentialNotFoundException
	{
		new SearchByKey();
	}
	
	public SearchByKey() throws CredentialNotFoundException
	{
		/*
		 * These are the FreeSound API credentials for your app.
		 * Go to this URL to apply: https://freesound.org/help/developers/
		 */
		String clientSecret = ""; // The client secret for YOUR freesound app.
		
		/*
		 * Initialize the FreeSound client.
		 */
		FreeSound freeSoundClient = new FreeSound(clientSecret);
		
		/*
		 * Send a text search request to the FreeSound API (website).
		 */
		SearchResponse results = freeSoundClient.searchByKey("C", "major");
		
		/*
		 * Print the results.
		 */
		for(int i = 0; i < results.count(); i++)
		{
			System.out.println(results.get(i).toString());
		}
	}
}
