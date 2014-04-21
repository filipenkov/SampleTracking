package com.atlassian.sisyphus;

import java.io.File;

import junit.framework.TestCase;

public class TestXStreamIO extends TestCase
{
	public void testLoading() throws Exception 
	{
		RemoteXmlPatternSource source = new RemoteXmlPatternSource(new File("src/test/resources/bamboo_regex.xml").toURI().toURL());
		assertEquals(102, source.size());
		source = new RemoteXmlPatternSource(new File("src/test/resources/confluence_regex.xml").toURI().toURL());
		assertEquals(649, source.size());
		source = new RemoteXmlPatternSource(new File("src/test/resources/jira_regex.xml").toURI().toURL());
		assertEquals(438, source.size());
		source = new RemoteXmlPatternSource(new File("src/test/resources/fisheye_regex.xml").toURI().toURL());
		assertEquals(89, source.size());
	}
}
