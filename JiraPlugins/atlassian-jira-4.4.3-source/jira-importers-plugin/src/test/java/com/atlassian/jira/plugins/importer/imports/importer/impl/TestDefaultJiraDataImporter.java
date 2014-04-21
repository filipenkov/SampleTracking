/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.plugins.importer.external.ExternalException;
import com.atlassian.jira.plugins.importer.external.ExternalUserUtils;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.ExternalUtilsBuilder;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.UnknownUsersException;
import com.atlassian.jira.plugins.importer.managers.CreateConstantsManager;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.lucene.search.Searcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TestDefaultJiraDataImporter {

	@Mock
	private ExternalUtils utils;
	@Mock
	private WorklogManager worklogManager;
	@Mock
	private FieldManager fieldManager;
	@Mock
	private WatcherManager watcherManager;
	@Mock
	private VoteManager voteManager;
	@Mock
	private IssueIndexManager indexManager;
	@Mock
	private CreateConstantsManager createConstantsManager;
	@Mock
	private SubTaskManager subTaskManager;
	@Mock
	private VersionManager versionManager;
	@Mock
	private UserManager userManager;
	@Mock
	private I18nHelper i18nHelper;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private JiraAuthenticationContext authenticationContext;
	@Mock
	private UserUtil userUtil;
	@Mock
	private GlobalPermissionManager globalPermissionManager;
	@Mock
	private ExternalUserUtils externalUserUtils;
	@Mock
	private ApplicationProperties applicationProperties;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private GroupManager mockGroupManager;
	@Mock
	private ProjectManager projectManager;
	@Mock
	private ImportLogger log;
	@Mock
	private JiraContextTreeManager jiraContextTreeManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private CreateProjectManager createProjectManager;
	@Mock
	private CrowdService crowdService;
	@Mock
	private OptionsManager optionsManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private SearchProviderFactory searchProviderFactory;
	@Mock
	private CustomFieldManager customFieldManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private JiraLicenseService jiraLicenseService;

	@Before
    public void createMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-17941
	 */
	@Test
	public void testInvalidBugIssueLink() throws SQLException {
		Map<String, String> keysLookup = new HashMap<String, String>();

		final String s = "luck Bug 7777777777777";
		final String out = DefaultJiraDataImporter.rewriteStringWithIssueKeys(BugzillaDataBean.ISSUE_KEY_REGEX,
				keysLookup, s);

		Assert.assertEquals(s, out);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-17941
	 * <p/>
	 * Verify if there are valid Bugzilla Bug links and there are issues matching text will be transformed
	 */
	@Test
	public void testRewriteBugLinkInText() {
		Map<String, String> keysLookup = new HashMap<String, String>();
		keysLookup.put("1", "PLE-34");
		keysLookup.put("2", "PLE-55");

		String text = DefaultJiraDataImporter.rewriteStringWithIssueKeys(BugzillaDataBean.ISSUE_KEY_REGEX, keysLookup,
				"Here's a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere's a good Bug 1. And there's a Bug 2 too.");
		Assert.assertEquals(
				"Here's a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere's a good PLE-34. And there's a PLE-55 too.",
				text);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-17941
	 * <p/>
	 * Verify if there are valid Bugzilla Bug links and there are no issues matching text will stay the same.
	 */
	@Test
	public void testRewriteBugLinkInTextNoMatchingIssues() {
		Map<String, String> keysLookup = new HashMap<String, String>();

		String rewrite = "Here's a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere's a good Bug 1.\nThere's a Bug 2 too.";
		String text = DefaultJiraDataImporter.rewriteStringWithIssueKeys(BugzillaDataBean.ISSUE_KEY_REGEX, keysLookup,
				rewrite);
		assertEquals(rewrite, text);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-17941
	 * <p/>
	 * Verify if there are not valid Bugzilla Bug links text stays the same
	 */
	@Test
	public void testRewriteBugLinkInTextNoBugzillaIds() {
		Map<String, String> keysLookup = new HashMap<String, String>();
		String rewrite = "There are no Bugzilla issue \n ids in this message.";
		String text = DefaultJiraDataImporter
				.rewriteStringWithIssueKeys(BugzillaDataBean.ISSUE_KEY_REGEX, keysLookup, rewrite);
		assertEquals(rewrite, text);
	}

	/**
	 * Test case for https://studio.plugins.atlassian.com/browse/JIM-132
	 */
	@Test
	public void testImportUnusedUsers()
			throws PermissionException, AddException, RemoveException, CreateException {

        JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = mock(I18nHelper.class);

		DefaultJiraDataImporter importer = new DefaultJiraDataImporter(new ExternalUtilsBuilder()
                .setAuthenticationContext(jiraAuthenticationContext)
				.createExternalUtils(), worklogManager, fieldManager, watcherManager, voteManager, indexManager,
                createConstantsManager, subTaskManager, versionManager, externalUserUtils, jiraContextTreeManager,
				createProjectManager, crowdService, optionsManager, searchProviderFactory, userUtil, jiraLicenseService);

		Group group = new ImmutableGroup("unused");

		final User user1 = new ImmutableUser(1, "a", null, null, true);
		final User user2 = new ImmutableUser(1, "b", null, null, true);
		final User user3 = new ImmutableUser(1, "c", null, null, true);

		final ExternalUser unused1 = new ExternalUser("b", "A A"),
				unused2 = new ExternalUser("a", "A A"), used = new ExternalUser("c", "A A");

		final Set<ExternalUser> allUsers = Sets.newHashSet(unused1, unused2, used);
		final Set<ExternalUser> requiredUsers = Sets.newHashSet(used);

		final Set<ExternalProject> projects = Sets.newHashSet(new ExternalProject("Test", "TST"));

		ImportDataBean mockBean = mock(ImportDataBean.class);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
		when(mockBean.getAllUsers(Matchers.<ImportLogger>anyObject())).thenReturn(allUsers);
		when(mockBean.getSelectedProjects(Matchers.<ImportLogger>anyObject())).thenReturn(projects);
		when(mockBean.getRequiredUsers(eq(projects), Matchers.<ImportLogger>anyObject())).thenReturn(requiredUsers);
		when(mockBean.getUnusedUsersGroup()).thenReturn("unused", "unused");
		when(externalUserUtils.createUser(eq(unused1), any(ImportLogger.class))).thenReturn(user1);
		when(externalUserUtils.createUser(eq(unused2), any(ImportLogger.class))).thenReturn(user2);
		when(crowdService.getUser("c")).thenReturn(user3);
		when(mockGroupManager.groupExists("unused")).thenReturn(true, true);
		when(mockGroupManager.getGroupObject("unused")).thenReturn(group, group);
		when(userUtil.getUserObject("a")).thenReturn(user1);
		when(userUtil.getUserObject("b")).thenReturn(user2);
		when(crowdService.search(Matchers.<Query<Object>>any())).thenReturn(Lists.<Object>newArrayList());

		importer.initializeLog();
		importer.setDataBean(mockBean);
		importer.importUsers();

		verify(externalUserUtils).createUser(eq(unused1), any(ImportLogger.class));
		verify(externalUserUtils).createUser(eq(unused2), any(ImportLogger.class));
		verify(externalUserUtils).deactivateUser(eq(user1));
		verify(externalUserUtils).deactivateUser(eq(user2));
		verify(externalUserUtils).deactivateUser(eq(user2));


		verifyZeroInteractions(worklogManager);
		verifyZeroInteractions(fieldManager);
		verifyZeroInteractions(watcherManager);
		verifyZeroInteractions(voteManager);
		verifyZeroInteractions(indexManager);
        verifyZeroInteractions(createConstantsManager);
		verifyZeroInteractions(jiraContextTreeManager);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-145
	 * <p/>
	 * Ignore links for issues that were not imported. There should be no references to ExternalUtils.createIssueLink
	 */
	@Test
	public void testMissingLinks() {
        JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        I18nHelper i18nHelper = mock(I18nHelper.class);

        when(utils.getAuthenticationContext()).thenReturn(jiraAuthenticationContext);
		when(utils.getCustomFieldManager()).thenReturn(customFieldManager);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

		DefaultJiraDataImporter importer = new DefaultJiraDataImporter(utils, worklogManager, fieldManager,
				watcherManager, voteManager, indexManager, createConstantsManager, subTaskManager, versionManager,
				externalUserUtils, jiraContextTreeManager, createProjectManager, crowdService, optionsManager,
				searchProviderFactory, userUtil, jiraLicenseService);

		BugzillaDataBean dataBean = mock(BugzillaDataBean.class);
		when(dataBean.getLinks(log)).thenReturn(
				Lists.newArrayList(new ExternalLink("Duplicate", "1", "2"),
						new ExternalLink("Duplicate", "3", "5")));

		importer.initializeLog();
		importer.preImport();
		importer.setDataBean(dataBean);

		importer.importIssueLinks(Maps.<String, String>newHashMap(), true, true);

		verifyZeroInteractions(worklogManager);
		verifyZeroInteractions(fieldManager);
		verifyZeroInteractions(watcherManager);
		verifyZeroInteractions(voteManager);
		verifyZeroInteractions(indexManager);
        verifyZeroInteractions(createConstantsManager);
		verifyZeroInteractions(externalUserUtils);
		verifyZeroInteractions(jiraContextTreeManager);
	}

    /**
     * Test case for https://studio.plugins.atlassian.com/browse/JIM-291
     */
    @Test
    public void testImportSkipUsersWhenExternalUserManagementIsOn()
			throws PermissionException, AddException, RemoveException, CreateException, ExternalException {

		ExternalUtils externalUtils = new ExternalUtilsBuilder()
				.setAuthenticationContext(authenticationContext)
				.setApplicationProperties(applicationProperties)
				.setProjectManager(projectManager)
				.setCustomFieldManager(customFieldManager)
				.createExternalUtils();
		ExternalUtils mockedUtils = spy(externalUtils);
		DefaultJiraDataImporter importer = new DefaultJiraDataImporter(mockedUtils, worklogManager, fieldManager,
				watcherManager, voteManager, indexManager,
                createConstantsManager, subTaskManager, versionManager, externalUserUtils, jiraContextTreeManager,
				createProjectManager, crowdService, optionsManager, searchProviderFactory, userUtil, jiraLicenseService);

        Set<ExternalUser> allUsers = Sets.newHashSet();
        allUsers.add(new ExternalUser("c", "A A"));
        allUsers.add(new ExternalUser("d", "A A"));

        Set<ExternalUser> requiredUsers = Sets.newHashSet();
        requiredUsers.add(new ExternalUser("c", "A A"));

        Set<ExternalProject> projects = Sets.newHashSet(new ExternalProject("Test", "TST", "pniewiadomski"));

		User pniewiadomski = mock(User.class);
		when(pniewiadomski.getName()).thenReturn("pniewiadomski");

        ImportDataBean mockBean = mock(ImportDataBean.class);
        when(authenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).thenReturn(true);
        when(mockBean.getAllUsers(Matchers.<ImportLogger>anyObject())).thenReturn(allUsers);
        when(mockBean.getSelectedProjects(Matchers.<ImportLogger>anyObject())).thenReturn(projects);
        when(mockBean.getRequiredUsers(eq(projects), Matchers.<ImportLogger>anyObject())).thenReturn(requiredUsers);
		when(crowdService.getUser("pniewiadomski")).thenReturn(pniewiadomski);
        when(userManager.getUserObject("c")).thenThrow(new AssertionError("This user should not be imported by this method"));
        when(userManager.getUserObject("d")).thenThrow(new AssertionError("This user should not be imported by this method"));

		Searcher searcher = mock(Searcher.class);
		when(searchProviderFactory.getSearcher(anyString())).thenReturn(searcher);

		importer.initializeLog();
		importer.log = log;
        importer.setDataBean(mockBean);
       	importer.doImport();

		// verify that the exception was thrown
		verify(log).fail(any(UnknownUsersException.class), anyString());

		verifyZeroInteractions(createProjectManager);
        verifyZeroInteractions(worklogManager);
        verifyZeroInteractions(fieldManager);
        verifyZeroInteractions(watcherManager);
        verifyZeroInteractions(voteManager);
        verifyZeroInteractions(indexManager);
        verifyZeroInteractions(createConstantsManager);
		verifyZeroInteractions(jiraContextTreeManager);
		verify(applicationProperties, times(1)).getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

}
