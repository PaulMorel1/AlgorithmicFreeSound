package com.algorithmicaudio.freesound.response;
// FreeSoundSearchResults.java
// This class encapsulates the results from a FreeSound search request sent to the API.

import java.util.List;

public class SearchResponse
{
	public int count;
	public List<SoundResponse> results;
	public int num_pages;
	public String next;
		
	@Override
	public String toString()
	{
		return "FreeSoundSearchResults [count=" + count + ", next=" + next + "]";
	}
	
	// empty constructor - should never be used
	public SearchResponse()
	{
		count = 0;
		results = null;
		num_pages = 0;
		next = "";
	}
	
	public int count()
	{
		return results.size();
	}
	
	public SoundResponse get(int index)
	{
		if( index < results.size())
		{
			return results.get(index);
		}
		return null;
	}
}