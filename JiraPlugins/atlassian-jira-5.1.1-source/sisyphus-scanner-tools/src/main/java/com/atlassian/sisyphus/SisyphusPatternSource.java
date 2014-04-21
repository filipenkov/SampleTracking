package com.atlassian.sisyphus;

public interface SisyphusPatternSource extends Iterable<SisyphusPattern>
{
	SisyphusPattern getPattern(String patternID);
	int size();
}