/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.ClasspathResourceServer;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.google.common.collect.ImmutableList;
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
import java.util.Date;
import java.util.List;

public class PivotalClientAttachmentsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ClasspathResourceServer mockServer = new ClasspathResourceServer(ImmutableMap.of(
			"/resource/download/1209615", "/pivotal/stories.xml",	// serving this random file as attachment
			"/tokens/active", "/pivotal/token.xml"));


	@Test
	public void testDownloadAttachments() throws Exception {
		StoryParser storyParser = new StoryParser(new MockUserNameMapper("wseliga", "Translated wseliga"));
		final Object o = XPath
				.selectSingleNode(ProjectParserTest.getRootElement("/pivotal/stories.xml"), "story[id=9972441]");
		final Element element = (Element) o;

		Element urlElement = (Element) XPath.selectSingleNode(element, "attachments/attachment/url");
		urlElement.setText(
				urlElement.getText().replace("http://www.pivotaltracker.com", mockServer.getBaseUri().toString()));

		final ExternalIssue issue = storyParser.parseStory(element);

		final PivotalClient pivotalClient = new MockPivotalClient();
		pivotalClient.login("", "");
		final Collection<ExternalAttachment> attachmentsForIssue = pivotalClient
				.getAttachmentsForIssue(issue, Mockito.mock(ImportLogger.class));
		final ExternalAttachment attachment = Iterables.getOnlyElement(attachmentsForIssue);

		Assert.assertEquals("Translated wseliga", attachment.getAttacher());
		Assert.assertTrue(IOUtils.contentEquals(new FileInputStream(attachment.getAttachment()),
				getClass().getResourceAsStream("/pivotal/stories.xml")));

		
	}

	@Test
	public void testAttachmentWithNullOrWrongUrl() throws Exception {
		final PivotalClient pivotalClient = new MockPivotalClient();
		pivotalClient.login("", "");

		final ExternalIssue issue = new ExternalIssue();
		final List<ExternalAttachment> attachments = ImmutableList.<ExternalAttachment>of(
			new PivotalExternalAttachment("no_url.txt", null, new Date()),
			new PivotalExternalAttachment("wrong_url_1.txt", "notanurl", new Date()),
			new PivotalExternalAttachment("wrong_url_2.txt", mockServer.getBaseUri().toString() + "/nonexistent", new Date()),
			new PivotalExternalAttachment("stories.xml", mockServer.getBaseUri().toString() + "/resource/download/1209615", new Date()));
		issue.setAttachments(attachments);

		final Collection<ExternalAttachment> result = pivotalClient.getAttachmentsForIssue(issue, new ConsoleImportLogger());
		Assert.assertEquals(1, result.size());
		final ExternalAttachment attachment = Iterables.getOnlyElement(result);
		Assert.assertTrue(IOUtils.contentEquals(new FileInputStream(attachment.getAttachment()),
				getClass().getResourceAsStream("/pivotal/stories.xml")));

	}

	private class MockPivotalClient extends PivotalClient {
		public MockPivotalClient() {
			super(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		}

		@Override
		protected File getTempDir() {
			return temporaryFolder.getRoot();
		}

		@Override
		protected void ensureLoggedInToWeb() throws IOException {
		}

		@Override
		protected String fixDownloadUrl(String url) {
			return url; // until we know how to serve https in test server
		}
	}
}
