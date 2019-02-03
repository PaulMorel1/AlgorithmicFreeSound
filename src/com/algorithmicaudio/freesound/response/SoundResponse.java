package com.algorithmicaudio.freesound.response;
// Sound.java
// This class encapsulates a Sound from a freesound search result.

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SoundResponse
{
	public String analysis_stats;
	public String analysis_frames;

	public String url;
	@SerializedName("preview-hq-ogg") public String preview_hq_ogg;
	public String similarity;
	public String serve; // the download link for the actual file
	public String spectral_m;
	@SerializedName("preview-lq-mp3") public String preview_lq_mp3;

	
	public String spectral_l;
	public String type;

	public String waveform_l; // the waveform graphic
	public String waveform_m;
	public String ref;
	
	public long id;
	public String name;
	public String license;
	public String username;
	public float duration;
	public List<String> tags; // this might need a subclass
	
	@SerializedName("preview-lq-ogg") public String preview_lq_ogg;

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