/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.ClasspathResourceServer;
import com.atlassian.jira.plugins.importer.IterableMatcher;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;

public class CachingPivotalClientTest {
	private static final String[] SAMPLE_PROJECT_NAMES = new String[]{"My Sample Project", "My Test Project"};
	final Function<ExternalProject, String> PROJECT_NAME = new Function<ExternalProject, String>() {
		@Override
		public String apply(@Nullable ExternalProject input) {
			return input.getName();
		}
	};

	@Rule
	public ClasspathResourceServer mockServer = new ClasspathResourceServer(ImmutableMap.of(
			"/projects/123456/stories", "/pivotal/stories.xml",
			"/projects", "/pivotal/projects.xml",
			"/projects/232313/memberships", "/pivotal/memberships.xml",
			"/projects/123456/iterations", "/pivotal/iterations.xml"));

	@Test
	public void testRetrievingStories() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<ExternalIssue> stories = client.getStories("123456", ConsoleImportLogger.INSTANCE);
		Assert.assertEquals(10, stories.size());
	}

	@Test
	public void testRetrievingIterations() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<PivotalIteration> stories = client.getIterations("123456", ConsoleImportLogger.INSTANCE);
		Assert.assertEquals(9, stories.size());
	}

	@Test
	public void testCacheRetrievedStories() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<ExternalIssue> stories = client.getStories("123456", ConsoleImportLogger.INSTANCE);
		int requestCount = mockServer.statistics.getRequests();
		Collection<ExternalIssue> stories2 = client.getStories("123456", ConsoleImportLogger.INSTANCE);

		Assert.assertNotSame(stories, stories2);
		//Assert.assertTrue(EqualsBuilder.reflectionEquals(stories, stories2)); this will not work due to the missing equals in ExternalIssue
		Assert.assertEquals("No additional web requests after the first one", requestCount, mockServer.statistics.getRequests());
	}

	@Test
	public void testRetrievingProjectNames() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<String> names = client.getAllProjectNames();
		Assert.assertThat(names, IterableMatcher.hasOnlyElements(SAMPLE_PROJECT_NAMES));
	}

	@Test
	public void testRetrievingAllProjects() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);

		Collection<ExternalProject> projects = client.getAllProjects(ConsoleImportLogger.INSTANCE);

		Collection<String> names = Collections2.transform(projects, PROJECT_NAME);
		Assert.assertThat(names, IterableMatcher.hasOnlyElements(SAMPLE_PROJECT_NAMES));
	}

	@Test
	public void testCacheRetrievedProjectNames() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);

		Collection<String> names = client.getAllProjectNames();

		Assert.assertThat(names, IterableMatcher.hasOnlyElements(SAMPLE_PROJECT_NAMES));

		final int requestCount = mockServer.statistics.getRequests();
		Collection<String> names2 = client.getAllProjectNames();
		Assert.assertThat(names2, IterableMatcher.hasOnlyElements(SAMPLE_PROJECT_NAMES));
		Assert.assertEquals("No additional web requests after the first one", requestCount, mockServer.statistics.getRequests());
	}

	@Test
	public void testCacheRetrievedAllProjects() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);

		Collection<ExternalProject> projects = client.getAllProjects(ConsoleImportLogger.INSTANCE);

		Collection<String> projectNames = Collections2.transform(projects, PROJECT_NAME);
		Assert.assertThat(projectNames, IterableMatcher.hasOnlyElements(SAMPLE_PROJECT_NAMES));

		final int requestCount = mockServer.statistics.getRequests();

		Collection<ExternalProject> projects2 = client.getAllProjects(ConsoleImportLogger.INSTANCE);

		Assert.assertNotSame(projects, projects2);
		Assert.assertEquals(ImmutableList.copyOf(projects), ImmutableList.copyOf(projects2));
		Assert.assertEquals("No additional web requests after the first one", requestCount, mockServer.statistics.getRequests());
	}

	@Test
	public void testRetrievingMemberships() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<ExternalUser> users = client.getMembers("232313", ConsoleImportLogger.INSTANCE);
		Collection<String> justNames = Collections2.transform(users, new Function<ExternalUser, String>() {
			@Override
			public String apply(@Nullable ExternalUser input) {
				return input.getName();
			}
		});
		Assert.assertThat(justNames, IterableMatcher.hasOnlyElements("wseliga", "wojciech seliga", "pawel niewiadomski", "test member"));

		final ImmutableMap<String, String> roles = ImmutableMap.of(
				"wseliga", "Owner",
				"wojciech seliga", "Member",
				"pawel niewiadomski", "Member",
				"test member", "Viewer");

		for (ExternalUser user : users) {
			final String role = Iterables.getOnlyElement(user.getProjectRoles().get("232313"));
			Assert.assertEquals(roles.get(user.getName()), role);
		}

	}

	@Test
	public void testCacheRetrievedMemberships() throws Exception {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<ExternalUser> users = client.getMembers("232313", ConsoleImportLogger.INSTANCE);
		int requestCount = mockServer.statistics.getRequests();
		Collection<ExternalUser> users2 = client.getMembers("232313", ConsoleImportLogger.INSTANCE);

		Assert.assertNotSame(users, users2);
		Assert.assertEquals(ImmutableSet.copyOf(users), ImmutableSet.copyOf(users2));

		Assert.assertEquals("No additional web requests after the first one", requestCount, mockServer.statistics.getRequests());
	}

	/**
	 * Regression test: {@link CachingPivotalClient} returned stories that were modifiable. Later {@link PivotalDataBean}
	 * modified them, next call to this method returned different (modified) values that were incorrent.
	 *
	 * Found when implementing https://studio.atlassian.com/browse/JIM-362
	 *
	 * @throws PivotalRemoteException
	 */
	@Test
	public void testStoriesAreImmutable() throws PivotalRemoteException {
		PivotalClient client = new CachingPivotalClient(mockServer.getBaseUri(), UserNameMapper.NO_MAPPING);
		Collection<ExternalIssue> stories = client.getStories("123456", ConsoleImportLogger.INSTANCE);
		Assert.assertEquals(10, stories.size());
		Assert.assertEquals("accepted", stories.iterator().next().getStatus());
		stories.iterator().next().setStatus("not accepted");

		stories = client.getStories("123456", ConsoleImportLogger.INSTANCE);
		Assert.assertEquals(10, stories.size());
		Assert.assertEquals("accepted", stories.iterator().next().getStatus());
	}
}
