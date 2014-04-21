package com.atlassian.sisyphus;

import java.util.SortedSet;
import java.util.TreeSet;

public class PatternMatchSet
{
	private final SortedSet<Integer> matchedLines = new TreeSet<Integer>();
	private final SisyphusPattern pattern;
	
	public PatternMatchSet(SisyphusPattern pattern)
	{
		this.pattern = pattern;
	}
	
	public SisyphusPattern getPattern()
    {
    	return pattern;
    }

	public void lineMatched(int lineNo)
    {
		matchedLines.add(new Integer(lineNo));
    }
	
	public int getFirstMatchedLine()
	{
		return matchedLines.first().intValue();
	}
	
	public int getLastMatchedLine()
	{
		return matchedLines.last().intValue();
	}
	
	public int getMatchCount()
    {
	    return matchedLines.size();
    }
}