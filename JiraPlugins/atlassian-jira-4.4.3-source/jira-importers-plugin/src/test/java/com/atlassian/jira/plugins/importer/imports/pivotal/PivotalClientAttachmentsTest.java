/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.ClasspathResourceServer;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class PivotalClientAttachmentsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ClasspathResourceServer mockServer = new ClasspathResourceServer(ImmutableMap.of(
			"/resource/download/1209615", "/pivotal/stories.xml",	// serving random file as attachment
			"/tokens/active", "/pivotal/token.xml"));


	@Test
	public void testDownloadAttachments() throws Exception {
		StoryParser storyParser = new StoryParser();
		final Object o = XPath
				.selectSingleNode(ProjectParserTest.getRootElement("/pivotal/stories.xml"), "story[id=9972441]");
		final Element element = (Element) o;

		Element urlElement = (Element) XPath.selectSingleNode(element, "attachments/attachment/url");
		urlElement.setText(
				urlElement.getText().replace("http://www.pivotaltracker.com", mockServer.getBaseUri().toString()));

		final ExternalIssue issue = storyParser.parseStory(element);

		final PivotalClient pivotalClient = new PivotalClient(mockServer.getBaseUri()) {
			@Override
			protected File getTempDir() {
				return temporaryFolder.getRoot();
			}

			@Override
			protected void ensureLoggedInToWeb() throws IOException {
			}
		};
		pivotalClient.login("", "");
		final Collection<ExternalAttachment> attachmentsForIssue = pivotalClient
				.getAttachmentsForIssue(issue, Mockito.mock(ImportLogger.class));
		final ExternalAttachment attachment = Iterables.getOnlyElement(attachmentsForIssue);

		Assert.assertTrue(IOUtils.contentEquals(new FileInputStream(attachment.getAttachedFile()),
				getClass().getResourceAsStream("/pivotal/stories.xml")));
	}

	@Test
	public void testGetAttachmentsForIssue() throws Exception {

	}
}
