/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class PivotalDataBeanTest {
	private static final String DEFAULT_RESOLUTION_ID = "123";

	@Mock
	PivotalClient pivotalClient;
	@Mock
	PivotalConfigBean configBean;
	@Mock
	PivotalSchemeManager pivotalSchemeManager;
	@Mock
	ImportLogger importLogger;
	@Mock
	ExternalProject externalProject;

	private PivotalDataBean bean;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(configBean.getPivotalClient()).thenReturn(pivotalClient);

		final Map<String, String> statusIds = Maps.newHashMap();
		for (String name : PivotalSchemeManager.getRegisteredStatusNames()) {
			statusIds.put(name, "ID for " + name);
		}
		Mockito.when(pivotalSchemeManager.getPTStatusNameToIdMapping()).thenReturn(statusIds);
		bean = new PivotalDataBean(configBean, pivotalSchemeManager, true);
		Mockito.when(configBean.getDefaultResolutionId()).thenReturn(DEFAULT_RESOLUTION_ID);
		Mockito.when(externalProject.getId()).thenReturn("0");
	}

	@Test
	public void testGetIssuesMapsKnownStatuses() throws Exception {

		final ExternalIssue bug = create("bug", "delivered");
		final ExternalIssue chore = create("chore", "started");
		final ExternalIssue feature = create("feature", "accepted");
		final ExternalIssue release = create("release", "finished");

		final ExternalIssue bug1 = create("bug", "rejected");
		final ExternalIssue bug2 = create("bug", "unstarted");
		final ExternalIssue bug3 = create("bug", "unscheduled");

		final ExternalIssue subtask1 = create("subtask", StoryParser.SUBTASK_STATUS_OPEN);
		final ExternalIssue subtask2 = create("subtask", StoryParser.SUBTASK_STATUS_FINISHED);

		bug.setSubtasks(Arrays.asList(subtask1, subtask2));

		final List<ExternalIssue> importedIssues = Arrays.asList(bug, chore, feature, release, bug1, bug2, bug3);
		Mockito.when(pivotalClient.getStories(Mockito.anyString(), Mockito.<ImportLogger>any())).thenReturn(importedIssues);
		Mockito.when(pivotalClient.getWorklog(0, importLogger)).thenReturn(Collections.<ExternalWorklog>emptyList());
		final Iterable<ExternalIssue> issues = bean.getIssues(externalProject, importLogger);

		assertEquals(importedIssues, ImmutableList.copyOf(issues));
		assertEquals(PivotalSchemeManager.BUG, bug.getIssueType());
		assertEquals(PivotalSchemeManager.CHORE, chore.getIssueType());
		assertEquals(PivotalSchemeManager.FEATURE, feature.getIssueType());
		assertEquals(PivotalSchemeManager.RELEASE, release.getIssueType());

		assertEquals(PivotalSchemeManager.SUBTASK, subtask1.getIssueType());
		assertEquals(PivotalSchemeManager.SUBTASK, subtask2.getIssueType());

		assertEquals("ID for " + PivotalSchemeManager.DELIVERED, bug.getStatus());
		assertEquals("ID for " + PivotalSchemeManager.STARTED, chore.getStatus());
		assertEquals("ID for " + PivotalSchemeManager.ACCEPTED, feature.getStatus());
		assertEquals("ID for " + PivotalSchemeManager.FINISHED, release.getStatus());

		assertEquals("ID for " + PivotalSchemeManager.REJECTED, bug1.getStatus());
		assertEquals("ID for " + PivotalSchemeManager.NOT_YET_STARTED, bug2.getStatus());
		assertEquals("ID for " + PivotalSchemeManager.NOT_YET_STARTED, bug3.getStatus());

		assertEquals("" + IssueFieldConstants.OPEN_STATUS_ID, subtask1.getStatus());
		assertEquals("" + IssueFieldConstants.CLOSED_STATUS_ID, subtask2.getStatus());

		for (ExternalIssue issue : ImmutableList.of(bug, chore, release, bug1, bug2, bug3, subtask1)) {
			assertNull("Expecting null resolution for " + issue.getStatus(), issue.getResolution());
		}
		for (ExternalIssue issue : ImmutableList.of(feature, subtask2)) {
			assertEquals("Expecting default resolution for " + issue.getStatus(), DEFAULT_RESOLUTION_ID, issue.getResolution());
		}
	}

	@Test
	public void testMappingUnknownValues() throws Exception {
		final ExternalIssue undefined = create("undefined", "unndefined");

		final List<ExternalIssue> importedIssues = Arrays.asList(undefined);
		Mockito.when(pivotalClient.getStories(Mockito.anyString(), Mockito.<ImportLogger>any())).thenReturn(importedIssues);
		bean.getIssues(externalProject, importLogger);

		assertEquals(PivotalSchemeManager.BUG, undefined.getIssueType());
		assertEquals("ID for " + PivotalSchemeManager.NOT_YET_STARTED, undefined.getStatus());
	}

	@Test
	public void testMappingOfProjectRoles() throws Exception {
		final List<ExternalUser> importedUsers = ImmutableList.of(
				createUser("user1", "Owner"),
				createUser("user2", "Member"),
				createUser("user3", "Viewer"),
				createUser("user4", "Unknown"));

		final Map<String, String> expectedRoles = ImmutableMap.of(
				"user1", "Administrators",
				"user2", "Developers",
				"user3", "Users",
				"user4", "Developers");

		Mockito.when(pivotalClient.getMembers(Mockito.anyString(), Mockito.<ImportLogger>any())).thenReturn(importedUsers);

		final Set<ExternalUser> allUsers = bean.getRequiredUsers(Arrays.asList(new ExternalProject("Project", "123456")),
				Mockito.mock(ImportLogger.class));
		assertEquals(4, allUsers.size());
		for (ExternalUser user : allUsers) {
			assertEquals(Collections.singleton(expectedRoles.get(user.getName())), user.getProjectRoles().get("123456"));
		}


	}

	private static ExternalIssue create(String type, String status) {
		final ExternalIssue issue = new ExternalIssue();
		issue.setIssueType(type);
		issue.setStatus(status);
		return issue;
	}

	private static ExternalUser createUser(String name, String role) {
		final ExternalUser user = new ExternalUser();
		user.setName(name);
		user.addRole("123456", role);
		return user;
	}
}
