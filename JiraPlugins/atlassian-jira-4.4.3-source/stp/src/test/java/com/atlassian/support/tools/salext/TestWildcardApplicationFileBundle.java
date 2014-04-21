package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class TestWildcardApplicationFileBundle extends TestCase
{
	public void testGetFiles()
	{
		File tempFile = new File("/tmp/foobar.txt");
		if( ! tempFile.exists())
		{
			try
			{
				tempFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		WildcardApplicationFileBundle bundleWithFiles = new WildcardApplicationFileBundle("key", "title",
				"description", "/tmp", ".*");
		assertTrue("Bundle with a really broad wildcard doesn't return any files.",
				bundleWithFiles.getFiles().size() > 0);

		File tmpDir = new File("/tmp");
		assertTrue("Bundle with a really broad wildcard doesn't return all existing files.", bundleWithFiles.getFiles()
				.size() == tmpDir.listFiles().length);

		WildcardApplicationFileBundle bundleWithNoFiles = new WildcardApplicationFileBundle("key", "title",
				"description", "/tmp", "potzrebie");
		assertFalse("Bundle with an overly restrictive wildcard returns files.",
				bundleWithNoFiles.getFiles().size() > 0);

	}
}
