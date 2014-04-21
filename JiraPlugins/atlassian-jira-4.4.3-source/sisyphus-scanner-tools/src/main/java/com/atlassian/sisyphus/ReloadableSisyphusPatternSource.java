package com.atlassian.sisyphus;


public interface ReloadableSisyphusPatternSource extends SisyphusPatternSource
{
	public void reload() throws Exception;
}
