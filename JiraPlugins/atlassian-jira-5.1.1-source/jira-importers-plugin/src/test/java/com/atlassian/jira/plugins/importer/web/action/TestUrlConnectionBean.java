/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web.action;

import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class TestUrlConnectionBean {

	@Test
	public void testIsValidBugzillaWebUrl() throws Exception {
		Assert.assertTrue(new SiteConfiguration("http://localhost").isValidUrl());
		Assert.assertTrue(new SiteConfiguration("https://localhost").isValidUrl());
		Assert.assertTrue(new SiteConfiguration("https://localhost/bugs").isValidUrl());
		Assert.assertTrue(new SiteConfiguration("https://localhost:123/bugs/abc").isValidUrl());
		Assert.assertTrue(new SiteConfiguration("https://localhost/bugs/abc").isValidUrl());
		Assert.assertTrue(new SiteConfiguration("http://atlassian.com/bugs").isValidUrl());

		Assert.assertFalse(new SiteConfiguration("httpx://localhost").isValidUrl());
		Assert.assertFalse(new SiteConfiguration(":/httpx://localhost").isValidUrl());
	}
}
