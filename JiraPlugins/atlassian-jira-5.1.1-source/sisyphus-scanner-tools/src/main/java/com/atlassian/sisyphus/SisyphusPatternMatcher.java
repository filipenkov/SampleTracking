package com.atlassian.sisyphus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public interface SisyphusPatternMatcher
{

	public abstract Map<String, PatternMatchSet> match(final BufferedReader reader) throws IOException, InterruptedException;
	public abstract void match(final BufferedReader reader, MatchResultVisitor visitor) throws IOException, InterruptedException;
}