package com.atlassian.support.tools.salext.bundle;

import java.util.List;

import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;

import junit.framework.TestCase;

public class TestDefaultApplicationFileBundle extends TestCase
{
	/* Validation is tested as part of testing other classes. */

	public void testGetFiles()
	{
		DefaultApplicationFileBundle bundle = new DefaultApplicationFileBundle(BundleManifest.APPLICATION_LOGS, "title", "description",
				"/tmp/foo", "/tmp/bar");
		List<String> files = bundle.getFiles();

		assertTrue("File bundle does not contain the number of entries passed on construction (it contains " + files.size() + " files)", files.size() == 2);
	}
}
