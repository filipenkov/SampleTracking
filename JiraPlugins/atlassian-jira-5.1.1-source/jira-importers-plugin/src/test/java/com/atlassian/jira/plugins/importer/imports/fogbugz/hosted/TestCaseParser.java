/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalExternalAttachment;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestCaseParser {

	private final CaseParser caseParser = new CaseParser();

	private static Element rootElement;

	@BeforeClass
	public static void setUp() throws Exception {
		rootElement = com.atlassian.jira.plugins.importer.imports.pivotal.ProjectParserTest
				.getRootElement("/fogbugz/cases.xml");
	}

	@Test
	public void testAttachments() throws JDOMException {
		final Object o = XPath.selectSingleNode(rootElement, "//case[ixBug=12]/events");

		final List<ExternalAttachment> attachments = caseParser.parseAttachments((Element) o);
		assertNotNull(attachments);
		assertEquals(2, attachments.size());

		ExternalAttachment attachment = attachments.get(0);
		assertEquals("build.properties.sample", attachment.getName());
		assertEquals(new DateTime(2011, 01, 24, 11, 07, 23, 0, DateTimeZone.UTC).toInstant(),
				attachment.getCreated().toInstant());
		assertEquals("2", attachment.getAttacher());
		assertEquals("default.asp?pg=pgDownload&pgType=pgFile&ixBugEvent=45&ixAttachment=4&sFileName=build.properties.sample&sTicket=",
				((PivotalExternalAttachment)attachment).getUrl());

		attachment = attachments.get(1);
		assertEquals("build.properties.template", attachment.getName());
		assertEquals(new DateTime(2011, 01, 24, 11, 07, 23, 0, DateTimeZone.UTC).toInstant(),
				attachment.getCreated().toInstant());
		assertEquals("2", attachment.getAttacher());
	}
}
