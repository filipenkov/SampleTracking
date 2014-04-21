package com.atlassian.sisyphus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class MappedSisyphusPatternSource implements SisyphusPatternSource
{
	protected Map<String, SisyphusPattern> regexMap = new HashMap<String, SisyphusPattern>();

	public SisyphusPattern getPattern(String patternID)
	{
		return regexMap.get(patternID);
	}

	public Iterator<SisyphusPattern> iterator()
	{
		return regexMap.values().iterator();
	}

	public Map<String, SisyphusPattern> getRegexMap()
    {
	    return regexMap;
    }
	
	public int size()
	{
		return regexMap.size();
	}
}
