package com.algorithmicaudio.freesound.response;
// Sound.java
// This class encapsulates a Sound from a freesound search result.

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

public class SoundResponse
{	
	public long id;
	public String name;
	public String license;
	public String username;
	public float duration;
	public LinkedTreeMap<Object, Object> analysis; // TODO: Provide more ways to access this data. See https://stackoverflow.com/questions/35120548/gson-json-to-generic-object, but there has to be a better way, without making dozens of classes or using reflection.
	public List<String> tags; // this might need a subclass

	public double distance; // this is only filled in similarity searches
	
	@Override
	public String toString()
	{
		return "Sound [id=" + id + ", name=" + name + ", duration=" + duration + "]";
	}

	public float getDurationInMilliseconds()
	{
		return duration / 1000.0f;
	}
}