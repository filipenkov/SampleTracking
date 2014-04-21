package com.atlassian.support.tools.salext;

import java.util.List;

import junit.framework.TestCase;

public class TestDefaultApplicationFileBundle extends TestCase
{
	/* Validation is tested as part of testing other classes. */

	public void testGetFiles()
	{
		DefaultApplicationFileBundle bundle = new DefaultApplicationFileBundle("key", "title", "description",
				"/tmp/foo", "/tmp/bar");
		List<String> files = bundle.getFiles();

		assertTrue("File bundle does not contain the number of entries passed on construction.", files.size() == 2);
	}
}
