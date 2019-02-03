package com.algorithmicaudio.freesound.response;
// FreeSoundSearchResults.java
// This class encapsulates the results from a FreeSound search request sent to the API.


public class AuthResponse
{
	public String access_token;
	public String scope;
	public long expires_in;
	public String refresh_token;

		
	@Override
	public String toString()
	{
		return "FreeSoundAuthResult [access_token=" + access_token + ", scope=" + scope + ", refresh_token=" + refresh_token + "]";
	}

}