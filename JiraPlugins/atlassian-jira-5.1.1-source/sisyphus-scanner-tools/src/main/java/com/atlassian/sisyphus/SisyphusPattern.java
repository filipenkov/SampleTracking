package com.atlassian.sisyphus;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SisyphusPattern
{
	private static final Logger log = Logger.getLogger(SisyphusPattern.class);
	
	private String pageName;
	private String regex;
	private String URL;
	private String Id;
	private String sourceID;
	
	private transient Pattern pattern = null;
	private transient boolean isBrokenPattern = false;
	
	public SisyphusPattern()
    {
    }

	// used in unit testing
	public SisyphusPattern(String id)
	{
		this.Id = id;
	}

	public SisyphusPattern(SisyphusPattern other)
	{
		setId(other.getId());
		setPageName(other.getPageName());
		setURL(other.getURL());
		setRegex(other.getRegex());
	}

	public String getPageName()
	{
		return pageName;
	}

	public String getRegex()
	{
		return regex;
	}

	public String getURL()
	{
		return URL;
	}

	public String getId()
	{
		return Id;
	}

	public void setPageName(String pn)
	{
		this.pageName = pn;
	}

	public void setRegex(String r)
	{
	    this.regex = r;
	    compile();
	}

	private void compile()
    {
	    if(StringUtils.isEmpty(regex))
	    {
	    	// ignore broken patterns
	    	this.isBrokenPattern = true;
	    	return;
	    }
    	
	    try 
    	{
    		this.pattern = Pattern.compile(regex);
    		// Exclude problematic patterns we already know about
    		this.isBrokenPattern =	 (
    				regex.equals("") || 
    				regex.equals("$body"));
    				/* 
    				regex.startsWith("(") ||
    				regex.startsWith("[") */
    	}
    	catch (PatternSyntaxException e) 
    	{
    		// Exclude problematic patterns we haven't thought of yet
    		this.isBrokenPattern = true;
    		log.error("Failed to compile pattern '"+getPageName()+"' at "+getURL(), e);
    	}
    }
	
	public Pattern getPattern()
    {
		if(isBrokenPattern)
			return null;
		
		if(pattern == null)
		{
			compile();
		}
    	return pattern;
    }

	public boolean isBrokenPattern()
    {
    	return isBrokenPattern;
    }

	public void setURL(String u)
    {
    	this.URL = u;
    }

	public void setId(String id)
	{
		this.Id = id;
	}

	public void setSourceID(String sourceID)
    {
	    this.sourceID = sourceID;
    }

	public String getSourceID()
    {
	    return sourceID;
    }

	public boolean equals(Object obj)
	{
		SisyphusPattern other = (SisyphusPattern) obj;

		if(!other.Id.equals(Id))
			return false;
		if(!other.pageName.equals(pageName))
			return false;
		if(!other.URL.equals(URL))
			return false;
		if(!other.regex.equals(regex))
			return false;
		return true;
	}
}