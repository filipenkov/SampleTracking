/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class StoryParserTest {
	private final StoryParser storyParser = new StoryParser(new MockUserNameMapper(ImmutableMap.of(
			"Pawel Niewiadomski", "Translated Pawel Niewiadomski",
			"wseliga", "Translated wseliga")));
	private static Element storiesRootElement;

	@BeforeClass
	public static void setUp() throws Exception {
		storiesRootElement = ProjectParserTest.getRootElement("/pivotal/stories.xml");
	}

	@Test
	public void testParseStory() throws Exception {
		final ExternalIssue externalIssue = storyParser.parseStory(storiesRootElement.getChild("story"));
		assertEquals("Translated Pawel Niewiadomski", externalIssue.getAssignee());
		assertEquals(new DateTime(2011, 2, 14, 14, 17, 59, 0, DateTimeZone.UTC).getMillis(),
				externalIssue.getCreated().getMillis());
		assertEquals(new DateTime(2011, 2, 14, 15, 47, 10, 0, DateTimeZone.UTC).getMillis(),
				externalIssue.getUpdated().getMillis());
		final List<ExternalCustomFieldValue> customFieldValues = externalIssue.getExternalCustomFieldValues();
		assertEquals(2, customFieldValues.size());
		final ExternalCustomFieldValue customFieldValue = customFieldValues.get(0);
		assertEquals("2", customFieldValue.getValue());
		assertEquals("Story Points", customFieldValue.getFieldName());
		assertEquals(CustomFieldConstants.NUMBER_FIELD_TYPE, customFieldValue.getFieldType());
		assertEquals(CustomFieldConstants.NUMBER_RANGE_FIELD_SEARCHER, customFieldValue.getSearcherType());

		assertEquals("9971857", externalIssue.getExternalId());

		final ExternalCustomFieldValue storyUrl = customFieldValues.get(1);
		assertEquals("http://www.pivotaltracker.com/story/show/9971857", storyUrl.getValue());
		assertEquals(DefaultJiraDataImporter.EXTERNAL_ISSUE_URL, storyUrl.getFieldName());
		assertEquals(CustomFieldConstants.URL_FIELD_TYPE, storyUrl.getFieldType());
		assertEquals(CustomFieldConstants.EXACT_TEXT_SEARCHER, storyUrl.getSearcherType());
	}

	@Test
	public void testParseDateTime() {
		assertEquals(new DateTime(2011, 1, 10, 11, 0, 0, 0, DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST"))).toInstant(),
				storyParser.parseDateTime("2011/01/10 11:00:00 CST").toInstant());
		assertEquals(new DateTime(1998, 3, 20, 22, 23, 24, 0, DateTimeZone.UTC).toInstant(),
				storyParser.parseDateTime("1998/03/20 22:23:24 UTC").toInstant());
	}

	@Test
	public void testParseStoryNoNotes() throws Exception {
		final List<ExternalComment> externalComments = storyParser.parseNotes(null);
		assertTrue(externalComments.isEmpty());
	}

	@Test
	public void testParseStoryWithNotes() throws Exception {
		final Object o = XPath.selectSingleNode(storiesRootElement, "story[id=9972441]/notes");
		assertNotNull("Precondition: need test story with notes", o);

		final List<ExternalComment> externalComments = storyParser.parseNotes((Element) o);
		assertEquals(2, externalComments.size());
		verifyComment(externalComments.get(0), "Translated wseliga", "2011/02/14 14:32:48 UTC", "a comment added while uploading a file");
		verifyComment(externalComments.get(1), "Translated wseliga", "2011/02/14 14:33:46 UTC", "another comment here");
	}

	private void verifyComment(ExternalComment comment, final String username, final String date, final String commentText) {
		final DateTime expectedDate = parseDate(date);
		assertEquals(commentText, comment.getBody());
		assertEquals(username, comment.getAuthor());
		assertEquals(expectedDate, comment.getCreated());
	}

	@Test
	public void testParseAttachments() throws Exception {
		final Object o = XPath.selectSingleNode(storiesRootElement, "story[id=9972441]/attachments");
		assertNotNull("Precondition: need test story with attachments", o);

		final List<ExternalAttachment> externalAttachments = storyParser.parseAttachments((Element) o);
		final ExternalAttachment attachment = Iterables.getOnlyElement(externalAttachments); // barfs if size != 1
		assertEquals("1.png", attachment.getName());
		assertEquals("a description of this attachment", attachment.getDescription());
		assertEquals("Translated wseliga", attachment.getAttacher());
		final DateTime expectedDate = parseDate("2011/02/14 14:32:37 UTC");
		assertEquals(expectedDate, attachment.getCreated());

		assertEquals("http://www.pivotaltracker.com/resource/download/1209615", ((PivotalExternalAttachment)attachment).getUrl());
	}

	@Test
	public void testSubtasksHaveReporterAndOwnerTheSameAsParent() throws JDOMException {
		final Object o = XPath.selectSingleNode(storiesRootElement, "story[id=9974265]");
		assertNotNull("Precondition: need test story with tasks", o);

		final ExternalIssue externalIssue = storyParser.parseStory((Element) o);
		final List<ExternalIssue> subtasks = externalIssue.getSubtasks();
		assertNotNull(subtasks);
		assertEquals(2, subtasks.size());
		for(ExternalIssue subtask : subtasks) {
			assertEquals(externalIssue.getReporter(), subtask.getReporter());
			assertEquals(externalIssue.getAssignee(), subtask.getAssignee());
		}
	}

	@Test
	public void testParseSubtasks() throws Exception {
		final Object o = XPath.selectSingleNode(storiesRootElement, "story[id=9974265]/tasks");
		assertNotNull("Precondition: need test story with tasks", o);

		final List<ExternalIssue> externalIssues = storyParser.parseTasks(null, null, (Element) o);
		assertEquals(2, externalIssues.size());
		verifySubtask(externalIssues.get(0), "my another task - also simple?", "2011/02/14 15:06:40 UTC", true);
		verifySubtask(externalIssues.get(1), "my first task - see how Pivotal works here", "2011/02/14 15:06:30 UTC", false);
	}


	@Test
	public void testParseLabels() throws Exception {
		final Object o = XPath.selectSingleNode(storiesRootElement, "story[id=9971879]/labels");
		assertNotNull("Precondition: need test story with tasks", o);

		assertEquals(ImmutableSet.of("this_is_a_long_text", "coma", "xcs", "impossible"), storyParser.parseLabels((Element) o));

		final Set<String> emptyLabels = storyParser.parseLabels(null);
		assertNotNull(emptyLabels);
		assertEquals(0, emptyLabels.size());
	}

	@Test
	public void testParseSubtasksOrder() throws Exception {
		final Element o = ProjectParserTest.getRootElement("/pivotal/subtasks_ordered.xml");

		final List<ExternalIssue> externalIssues = storyParser.parseTasks(null, null, o);
		assertEquals(3, externalIssues.size());
		verifySubtask(externalIssues.get(0), "First Task", "2011/02/14 15:06:30 UTC", false);
		verifySubtask(externalIssues.get(1), "Second Task", "2011/02/14 15:06:40 UTC", true);
		verifySubtask(externalIssues.get(2), "Third Task", "2011/02/14 15:06:50 UTC", true);
	}

	private void verifySubtask(ExternalIssue subtask, String subject, String createdOn, boolean finished) {
		assertEquals(subject, subtask.getSummary());
		assertEquals(parseDate(createdOn), subtask.getCreated());
		assertEquals("subtask", subtask.getIssueType());

		final String expectedStatus = String.valueOf(finished ? StoryParser.SUBTASK_STATUS_FINISHED : StoryParser.SUBTASK_STATUS_OPEN);
		assertEquals(expectedStatus, subtask.getStatus());
	}

	@Test
	public void testParseCETZone() throws Exception {
		final DateTime dateTime = storyParser.parseDateTime("2009/03/12 01:00:00 CET");
		assertEquals(parseDate("2009/03/12 00:00:00 UTC"), dateTime);

	}

	private DateTime parseDate(String date) {
		return storyParser.parseDateTime(date);
	}
}
