/**
 * 
 */
package com.atlassian.sisyphus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMatchResultVisitor implements MatchResultVisitor 
{
	private final Map<String, PatternMatchSet> matches = new ConcurrentHashMap<String, PatternMatchSet>();
	private boolean isCancelled;

	public void patternMatched(String line, int lineNo, SisyphusPattern pattern) 
	{
	    PatternMatchSet match = matches.get(pattern.getId());
	    if (match == null)
	    {
	        match = new PatternMatchSet(pattern);
	    }
	    match.lineMatched(lineNo);
	    matches.put(pattern.getId(), match);
	}

	public Map<String, PatternMatchSet> getResults()
	{
		return matches;
	} 

	public void setCancelled() 
	{
		isCancelled = true;
	}
	
	public boolean isCancelled() 
	{
		return isCancelled;
	}	
}