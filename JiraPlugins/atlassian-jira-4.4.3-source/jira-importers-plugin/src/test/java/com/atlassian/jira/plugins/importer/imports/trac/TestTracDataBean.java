/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestTracDataBean {

	@Test
	public void testRewriteBugLinkInText() {
		Map<String, String> keysLookup = new HashMap<String, String>();
		keysLookup.put("1", "PLE-34");
		keysLookup.put("2", "PLE-55");

		String text = DefaultJiraDataImporter.rewriteStringWithIssueKeys(TracDataBean.ISSUE_KEY_REGEX, keysLookup,
				"#1 (ticket), [1] (changeset), {1} (report)\n"
						+ "ticket:1, ticket:1#comment:1\n"
						+ "Ticket [ticket:1], [ticket:1 ticket one]\n"
						+ "Ticket [[ticket:1]], [[ticket:1|ticket one]]");
		Assert.assertEquals(
				"PLE-34 (ticket), [1] (changeset), {1} (report)\n"
						+ "PLE-34, PLE-34#comment:1\n"
						+ "Ticket [PLE-34], [PLE-34 ticket one]\n"
						+ "Ticket [[PLE-34]], [[PLE-34|ticket one]]",
				text);
	}

}
