package com.atlassian.sisyphus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;


public class RemoteXmlPatternSource extends MappedSisyphusPatternSource implements ReloadableSisyphusPatternSource
{
	static final Logger log = Logger.getLogger(RemoteXmlPatternSource.class);

	private final URL url;

	public RemoteXmlPatternSource(URL url) throws IOException, ClassNotFoundException
    {
		this.url = url;
		reload();
    }

	public void reload() throws IOException, ClassNotFoundException
	{
		regexMap = SisyphusPatternPersister.readPatternsIn(new InputStreamReader(url.openStream()));
    }
}