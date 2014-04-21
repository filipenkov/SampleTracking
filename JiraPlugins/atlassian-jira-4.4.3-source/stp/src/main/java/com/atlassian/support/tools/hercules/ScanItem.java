package com.atlassian.support.tools.hercules;

public class ScanItem
{
	/**
	 * The key used in application log paths for the default log file
	 */
	private static final String DEFAULT_LOG = "stp.hercules.scanItem.default";
	
	private final String key;
	private final String path;
	
	public ScanItem(String key, String path)
	{
		this.key = key;
		this.path = path;
	}

	public String getKey()
	{
		return key;
	}

	public String getPath()
	{
		return path;
	}
	
	public static ScanItem createDefaultItem(String path)
	{
		return new ScanItem(DEFAULT_LOG, path);
	}
}