/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis;

import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestMantisDataBean {

	/**
	 * Verify if there are valid Mantis Bug links and there are issues matching text will be transformed
	 */
	@Test
	public void testRewriteBugLinkInText() {
		Map<String, String> keysLookup = new HashMap<String, String>();
		keysLookup.put("1", "PLE-34");
		keysLookup.put("2", "PLE-55");

		String text = DefaultJiraDataImporter.rewriteStringWithIssueKeys(MantisDataBean.ISSUE_KEY_REGEX, keysLookup, "#1");
		Assert.assertEquals("PLE-34", text);

		text = DefaultJiraDataImporter.rewriteStringWithIssueKeys(MantisDataBean.ISSUE_KEY_REGEX, keysLookup,
				"#1 bug#2");
		Assert.assertEquals("PLE-34 PLE-55", text);
	}
}
