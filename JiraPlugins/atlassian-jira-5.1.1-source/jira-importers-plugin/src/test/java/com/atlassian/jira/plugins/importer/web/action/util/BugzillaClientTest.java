/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.web.action.util;

import com.atlassian.jira.plugins.importer.MockI18nHelper;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaClient;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class BugzillaClientTest {

	@Test
	public void testHasBugzillaTitle() throws IOException {
		final String s1 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
				"                      \"http://www.w3.org/TR/html4/loose.dtd\"> \n" +
				"<html> \n" +
				"  <head> \n" +
				"    <title>Bugzilla Main Page</title> ";
		final String s2 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
				"                      \"http://www.w3.org/TR/html4/loose.dtd\"> \n" +
				"<html> \n" +
				"  <head> \n" +
				"    <title>Bugzilla@Mozilla Main Page</title> ";

		final Collection<Pair<String, Boolean>> testData = Arrays.asList(
				Pair.of("fsddfs", false),
				Pair.of("<title>fdsfsd Main Page</title>", true),
				Pair.of("<title>fdsfsd Main Page</titl>", false),
				Pair.of("<title>fdsfsd xx Page</title>", true),
				Pair.of("<title>Bugzilla2 Main Page</title>", true),
				Pair.of("fsdfds<title>Bugzilla2 Main Page</title>fsfds", true),
				Pair.of("fsdfds<title>Bugzilla2 Main Page fsfsfs", false),
				Pair.of("", false),
				Pair.of(s1 + s1, true),
				Pair.of("\n\n" + s1, false), // too many lines
				Pair.of(s2, true),
				Pair.of(s1, true));

		for (Pair<String, Boolean> pair : testData) {
			Assert.assertEquals("Checking string [" + pair.first() + "]", pair.second(),
					BugzillaClient.hasBugzillaTitle(new ByteArrayInputStream(pair.first().getBytes("UTF-8")),
							"UTF-8", 5));
		}

	}

	@Test
	public void testValidatingBadUrlDoesNotThrowExceptions() throws Exception {
		SiteConfiguration sc = new SiteConfiguration(" url with space ");
		BugzillaClient bc = new BugzillaClient(sc) {
			@Override
			protected I18nHelper getI18nBean() {
				return new MockI18nHelper();
			}
		};
		final Map<String,String> errors = bc.validateBugzillaUrl();
		Assert.assertFalse(errors.isEmpty());
		Iterables.find(errors.keySet(), new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return input.contains("Illegal character in path");
			}
		});
	}
}
