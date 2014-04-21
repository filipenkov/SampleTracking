package com.atlassian.sisyphus;

public interface MatchResultVisitor 
{
	public void patternMatched(String line, int lineNo, SisyphusPattern pattern);
	public boolean isCancelled();
}
