package com.algorithmicaudio.freesound;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.security.auth.login.CredentialNotFoundException;

import com.algorithmicaudio.freesound.io.Local;
import com.algorithmicaudio.freesound.io.Remote;
import com.algorithmicaudio.freesound.response.AuthResponse;
import com.algorithmicaudio.freesound.response.SearchResponse;
import com.google.gson.Gson; // GSON is Google's JSON parsing library. It is the only external dependency. You can get it from github at https://github.com/google/gson.

/**
 *  FreeSound API client that is optimized for making algorithmic music.
 *  
 *  The FreeSound-Java client by Chris Rowe is more full-featured, and
 *  more complex, than this client. https://github.com/Sonoport/freesound-java.
 *  
 *  The goal of this library is twofold:
 *  1. Eliminate the cruft around operations that are not necessary for
 *     making generative music with audio files from FreeSound.
 *  2. Present a simple, streamlined library that can be modified by
 *     musicians who may not be experienced programmers.   
 */
public class FreeSound
{
	public String clientSecret = ""; // Your client secret (aka Api Key) from https://freesound.org/apiv2/apply/
	public String clientId = ""; // Your client id from https://freesound.org/apiv2/apply/
	public String userAccessToken = ""; // the access token from OAuth Step 3. See https://freesound.org/docs/api/authentication.html#oauth-authentication

	/*
	 * The fields returned on sounds from search endpoints
	 * 
	 * id, name, duration, tags, analysis, license, username
	 */
	public String[] searchFields = new String[] { "id", "name", "duration", "tags", "analysis" };
	
	/*
	 * the analysis descriptors returned on sounds from search endpoints.
	 * 
	 * For the full list, see https://freesound.org/docs/api/analysis_docs.html#analysis-docs
	 * 
	 *  
	 */
	public String[] descriptorFields = new String[] { "lowlevel.average_loudness", "rhythm.bpm", "lowlevel.pitch_salience", "tonal.key_strength", "tonal.key_key", "tonal.key_scale" };

	/**
	 * This simple constructor will only allow your app to search for files. It will not 
	 * allow your app to download files unless the user has previously authorized it and 
	 * stored a user access token. 
	 * 
	 * @param	newClientSecret	The string that uniquely identifies your app. 
	 * @throws CredentialNotFoundException 
	 * @see 					https://freesound.org/help/developers/
	 */
	public FreeSound(String newClientSecret) throws CredentialNotFoundException
	{
		this(newClientSecret, "", "");
	}

	/**
	 * Constructor
	 * 
	 * @param	newClientSecret		Your api key that permits you to use the FreeSound API. See https://freesound.org/apiv2/apply/
	 * @param	newClientId			The identifier that uniquely identifies your app.
	 * @param	oauthToken 			The code from the user that permits this app to request downloads for a user.
	 * @throws CredentialNotFoundException 
	 */
	public FreeSound(String newClientSecret, String newClientId, String newAuthorizationCode) throws CredentialNotFoundException
	{
		// ensure that we have the credentials necessary to use the FreeSound API at some level
		if( newClientSecret.length() == 0 )
		{
			throw new CredentialNotFoundException("You must obtain developer credentials from FreeSound in order to use the FreeSound API. It only takes a second. Go to https://freesound.org/help/developers/ and sign up.");
		}
		
		clientSecret = newClientSecret;
		clientId = newClientId;
		
		authorize(newAuthorizationCode);
	}
	
	/**
	 * Entrance point to the OAuth authorization flow that allows this client to download files.
	 * 
	 * @param	newAuthorizationCode	The code copied from the FreeSound site during the first step in authorization.
	 */
	private void authorize(String newAuthorizationCode)
	{
		// first try to load the user access token from a local file
		if(new File("UserAccessToken.txt").exists())
		{	
			userAccessToken = Local.readTextFromFile("UserAccessToken.txt");
		}
		// if there is an auth code then try to get a user access token (in order to download files)
		else if (newAuthorizationCode.length() > 0 && clientSecret.length() > 0 && clientId.length() > 0)
		{
			/*
			 *  The final OAuth flow for getting a user access token.
			 *  See Step 3 here: https://freesound.org/docs/api/authentication.html#oauth-authentication
			 */
			authorizeStepThree(newAuthorizationCode);
		}
		else
		{
			authorizeStepOne(clientId);
		}
	}

	/**
	 * The first step in the OAuth dance that allows this client app to download files. This method will redirect a user to a 
	 * login page on freesound. They will login and allow access to their account. Then they will get an authorization code,
	 * which must be saved for use when instantiating the FreeSound client library (this class).
	 * 
	 * This method is static so that it can be used before the FreeSound instance is constructed.
	 * 
	 * @param	clientId	The identifier of the client app that is being authorized.
	 */
	public static void authorizeStepOne(String clientId)
	{
		System.out.println("This program requires an authorization code from freesound.org.");
		System.out.println("It will now open a browser window on freesound.org to request access.");
		System.out.println("After you grant access, you must copy the authorization code and enter it into your program before running it again.");
		
		String authorizeUrl = "https://freesound.org/apiv2/oauth2/authorize/?client_id=" + clientId + "&response_type=code";
		try
		{
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
			{
				Desktop.getDesktop().browse(new URI(authorizeUrl));
			}
			else
			{
				System.out.println("This operation is not supported by your operating system. Go to " + authorizeUrl + " in your web browser and copy the authorization code.");
			}
		}
		catch(URISyntaxException | IOException e)
		{
			System.out.println("Error while attempting to open " + authorizeUrl + ".\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Third step in the OAuth handshake dance. See https://freesound.org/docs/api/authentication.html#oauth-authentication
	 * 
	 * Once the client app has an authorization code from step one, we can retrieve a user access token from FreeSound. That's what
	 * this method does.
	 * 
	 * @param	authorizationCode	The code copied from step one.
	 * @return						true on success. false on failure.
	 */
	private boolean authorizeStepThree(String authorizationCode)
	{
		if (authorizationCode.length() == 0)
		{
			System.out.println("Unable to request a user access token without an authorization code!");
			return false;
		}

		if (clientSecret.length() == 0)
		{
			System.out.println("Unable to request a user access token without an app access token (client secret)!");
			return false;
		}

		if (clientId.length() == 0)
		{
			System.out.println("Unable to request a user access token without a client id!");
			return false;
		}

		// make the hash of parameters for the HTTP post request
		HashMap<String, String> formData = new HashMap<String, String>();
		formData.put("client_id", clientId);
		formData.put("client_secret", clientSecret);
		formData.put("grant_type", "authorization_code");
		formData.put("code", authorizationCode);

		String authorizationJson = Remote.httpPost("https://freesound.org/apiv2/oauth2/access_token/", formData);

		Gson gson = new Gson();
		AuthResponse result = gson.fromJson(authorizationJson, AuthResponse.class);

		if (result.access_token.length() > 0)
		{
			userAccessToken = result.access_token;
			Local.writeTextToFile("UserAccessToken.txt", userAccessToken); // save the user access token to a local file
			return true;
		}
		return false;
	}

	/**
	 * General search method for searching by text.
	 * 
	 * @param	searchString	The text that you want to search for
	 * @return					The search response from FreeSound
	 */
	public SearchResponse search(String searchString)
	{
		return searchByText(searchString, "", 0, true);
	}	
	
	/**
	 * This search method returns only sounds with the given tag
	 * 
	 * @param	tag		The tag to search for
	 * @return			The search response from FreeSound
	 */
	public SearchResponse searchByTag(String tag)
	{
		return searchByText("", tag, 0, true);
	}

	/**
	 * Search for a sound by text.
	 * 
	 * @param	searchString				the text you want to search for
	 * @param 	maximumDurationInSeconds	the maximum duration of returned sounds. It may be desirable to exclude long ambient sounds.
	 * @param 	canonical					Return only canonical wav files?
	 * @return								The SearchResponse from FreeSound
	 */
	public SearchResponse searchByText(String searchString, String tag, int maximumDurationInSeconds, boolean canonical)
	{
		try
		{
			// make the hash of parameters for the HTTP post request
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("fields", String.join(",", searchFields));
			params.put("descriptors", String.join(",", descriptorFields));
			params.put("format", "json");
			params.put("token", clientSecret);
			
			// if there is a text search, then include - this method is also used to search by tag, so the text search may not be used
			if( searchString.length() > 0 )
				params.put("query", searchString);
			
			String filter = buildFilter(tag, canonical, maximumDurationInSeconds);
			if(filter.length() > 0)
				params.put("filter", filter);

			String query = getSearchUrl("search/text/?" + Remote.makeParameters(params));

			// query the FreeSound API
			String jsonString = Remote.httpGet(query);
			// if we managed to get a response
			if (jsonString.length() > 0)
			{
				// gson
				Gson gson = new Gson();
				return gson.fromJson(jsonString, SearchResponse.class);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error while searching freesound for " + searchString + "\n" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This uses content search to find sounds in a specific key.
	 * 
	 * @param 	key							for example, C
	 * @param 	scale						major or minor
	 * @return								SearchResponse
	 */
	public SearchResponse searchByKey(String key, String scale)
	{
		return searchByContent("(tonal.key_key:\"" + key + "\" AND tonal.key_scale:\"" + scale + "\")", true, 0);
	}
	
	/**
	 * Search based on numeric descriptions of the sounds. See https://freesound.org/docs/api/resources_apiv2.html#content-search
	 * 
	 * @param 	descriptorsFilter			The descriptors that describe the target sound
	 * @param 	canonical					Return only canonical wav files?
	 * @param 	maximumDurationInSeconds	The maximum duration for returned sound files.
	 * @return								SearchResponse
	 */
	public SearchResponse searchByContent(String descriptorsFilter, boolean canonical, int maximumDurationInSeconds)
	{
		try
		{
			// make the hash of parameters for the HTTP post request
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("fields", String.join(",", searchFields));
			params.put("descriptors", String.join(",", descriptorFields));
			params.put("format", "json");
			params.put("token", clientSecret);
			params.put("descriptors_filter", descriptorsFilter);
			
			String filter = buildFilter("", canonical, maximumDurationInSeconds);
			if(filter.length() > 0)
				params.put("filter", filter);

			String query = getSearchUrl("search/content/?" + Remote.makeParameters(params));
			
			String jsonString = Remote.httpGet(query);
			if (jsonString.length() > 0)
			{
				// gson
				Gson gson = new Gson();
				return gson.fromJson(jsonString, SearchResponse.class);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Search for sounds that are similar to the sound with the id passed in as argument. This version of this method uses the
	 * default arguments, with no maximum duration, maximum results set to 15, and only returning canonical wav files, which are
	 * easier to work with than the more complex formats.
	 * 
	 * @param SoundId The id of the base sound.
	 * 
	 * @return Nothing. The results are in the Results collection.
	 */
	public SearchResponse searchForSimilar(String SoundId)
	{
		return searchForSimilar(SoundId, 0, 15, true);
	}

	/**
	 * Search for sounds that are similar to a target sound.
	 * 
	 * @param 	SoundId						The id of the target sound
	 * @param 	MaximumDurationInSeconds	The maximum duration of returned sounds
	 * @param 	MaximumResults				The maximum number of results to return
	 * @param 	Canonical					Return only canonical wav files?
	 * @return								The SearchResponse from FreeSound
	 */
	public SearchResponse searchForSimilar(String SoundId, int MaximumDurationInSeconds, int MaximumResults, boolean Canonical)
	{
		try
		{
			
			// make the hash of parameters for the HTTP post request
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("target", SoundId);
			params.put("fields", String.join(",", searchFields));
			params.put("descriptors", String.join(",", descriptorFields));
			params.put("format", "json");
			params.put("token", clientSecret);
			
			String filter = buildFilter("", Canonical, MaximumDurationInSeconds);
			if(filter.length() > 0)
				params.put("filter", filter);

			/*
			 * The similar sounds endpoint is actually at sounds/<SOUND_ID>/similar/?etc
			 * but it doesn't support filters, while the combined search endpoint
			 * DOES support filters. So we use that.
			 */
			String query = getSearchUrl("search/combined/?" + Remote.makeParameters(params));

			String jsonString = Remote.httpGet(query);
			if (jsonString.length() > 0)
			{
				// gson
				Gson gson = new Gson();
				return gson.fromJson(jsonString, SearchResponse.class);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error while searching for sounds similar to " + SoundId + ".\n" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Download a sound from FreeSound.
	 * 
	 * @param newFilename the path to the file on your local system where the audio file will be saved
	 * @param soundId     the id of the sound to be downloaded
	 * @return String the path to the newly created file
	 * @throws CredentialNotFoundException 
	 */
	public String downloadSound(String newFilename, long soundId) throws CredentialNotFoundException
	{	
		// ensure that we have the credentials necessary to download sounds from the FreeSound API
		if( userAccessToken.length() == 0 )
		{
			throw new CredentialNotFoundException("You must obtain a user access token from FreeSound in order to download sounds. See https://freesound.org/docs/api/authentication.html#oauth-authentication.");
		}

		try
		{
			// only download the file if it doesn't already exist
			File f = new File(newFilename);
			if (!f.exists())
			{
				String url = getDownloadUrl(Long.toString(soundId));
				if (!Remote.httpGetBinary(url, newFilename, userAccessToken))
				{
					System.out.println("Unable to download " + newFilename + ".");
				}
			}
			return newFilename; // return the complete path to the sound file
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
		return "";
	}

	/**
	 * Get the URL for a request to a search endpoint.
	 * 
	 * @param	location	the path to the specific search resource
	 * @return				the string with the full URL
	 */
	private String getSearchUrl(String location)
	{
		return "https://www.freesound.org/apiv2/" + location;
	}

	/**
	 * Get the URL for a request to a download endpoint.
	 * 
	 * @param	soundId		the sound that you want to download
	 * @return				the string with the full URL
	 */
	private String getDownloadUrl(String soundId)
	{
		return "https://www.freesound.org/apiv2/sounds/" + soundId + "/download/";
	}
	
	/**
	 * Build the filter parameter for a search request.
	 * 
	 * @param	OnlyCanonicalWaveFiles		If this is true, then the search will only return monophonic wav files with a sample rate of 44.1kHz and a bit depth of 16 bits. These files are very easy to work with, so this option prevents having to understand many different audio file types.
	 * @param	MaximumDurationInSeconds	The maximum duration of returned audio files.
	 * @return								The filter as a parameterized String.
	 */
	private String buildFilter(String tag, boolean OnlyCanonicalWaveFiles, int MaximumDurationInSeconds)
	{
		String filter = "";
		if(OnlyCanonicalWaveFiles)
			filter += "type:wav bitdepth:16 samplerate:44100 channels:1 ";
		if(MaximumDurationInSeconds  > 0)
			filter += "duration:[0.1 TO " + MaximumDurationInSeconds + "] ";
		if(tag.length() > 0)
			filter += "tag:" + tag;
		return filter;
	}
}
