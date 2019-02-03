package com.algorithmicaudio.freesound.example;

import javax.security.auth.login.CredentialNotFoundException;

import com.algorithmicaudio.freesound.FreeSound;
import com.algorithmicaudio.freesound.response.SearchResponse;
import com.algorithmicaudio.freesound.response.SoundResponse;

/**
 * This example demonstrates how to search and download sounds from FreeSound.
 * 
 * The authorization flow for this is slightly more complex, because you
 * must be a registered FreeSound user to download sounds. So this program
 * must request access to your FreeSound account in order to download sounds
 * on your behalf.
 * 
 * You must run this program once to obtain an authorization code. Then copy 
 * that authorization code into this program. Then run it again.
 */
public class SearchAndDownload
{
	public static void main(String[] args) throws CredentialNotFoundException
	{
		new SearchAndDownload();
	}
	
	public SearchAndDownload() throws CredentialNotFoundException
	{
		/*
		 * These are the FreeSound API credentials for your app.
		 * Go to this URL to apply: https://freesound.org/help/developers/
		 */
		String clientId = ""; // You must enter the client id for YOUR freesound app.
		String clientSecret = ""; // The client secret for YOUR freesound app.
		
		/*
		 * This is an authorization code for your personal FreeSound account.
		 * It authorized your app to download sounds on your behalf.
		 * 
		 * When you run this program, it should pop open a browser window
		 * that is on freesound.org. This will be a page requesting access
		 * to your FreeSound account. When you approve access, then it will
		 * show you a code. Copy that code into this variable.
		 */
		String authorizationCode = "";
		
		/*
		 * Initialize the FreeSound client.
		 */
		FreeSound freeSoundClient = new FreeSound(clientSecret, clientId, authorizationCode);
		
		/*
		 * Send a text search request to the FreeSound API (website).
		 */
		SearchResponse results = freeSoundClient.search("glass");
		
		/*
		 * Get the first response.
		 */
		SoundResponse sound = results.get(0);

		/*
		 * Warn the user that the download is beginning.
		 */
		System.out.println("Attempting to download " + sound.toString());
		
		/*
		 * Download the sound.
		 */
		String filename = freeSoundClient.downloadSound(sound.name, sound.id);
		
		/*
		 * Print the results.
		 */
		System.out.println("File downloaded to '" + filename + "'. This file is in the root of your current java project.");
	}
}
