package com.algorithmicaudio.freesound.example;

import javax.security.auth.login.CredentialNotFoundException;
import com.algorithmicaudio.freesound.FreeSound;
import com.algorithmicaudio.freesound.response.SearchResponse;
import com.algorithmicaudio.freesound.response.SoundResponse;

/*
 * This example demonstrates how to search for similar sounds from FreeSound.
 */
public class SearchSimilar
{
	public static void main(String[] args) throws CredentialNotFoundException
	{
		new SearchSimilar();
	}
	
	public SearchSimilar() throws CredentialNotFoundException
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
		SearchResponse results = freeSoundClient.search("glass");
		
		/*
		 * Get the first response and print it.
		 */
		SoundResponse sound = results.get(0);
		System.out.println("Found " + sound.toString() + "\n");

		/*
		 * Search for similar sounds. This query can take a long time.
		 */
		System.out.println("Searching for similar sounds. Please wait...");
		SearchResponse similarResults = freeSoundClient.searchForSimilar(Long.toString(sound.id));
		
		/*
		 * Print the results.
		 */
		for(int i = 0; i < results.count(); i++)
		{
			System.out.println(similarResults.get(i).toString());
		}
	}
}