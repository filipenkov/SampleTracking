package com.atlassian.jira.extra.icalfeed.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProvider;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultEntityAsServiceTestCase extends TestCase
{

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private SearchService searchService;

    @Mock
    private DateProvider dateProvider;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private PermissionManager permissionManager;

    private DefaultEntityAsEventService defaultEntityAsEventService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        defaultEntityAsEventService = new DefaultEntityAsEventService(jiraAuthenticationContext, pluginAccessor, searchService, projectManager, permissionManager)
        {
            @Override
            DateProvider getDateProvider(String dateFieldName)
            {
                return dateProvider;
            }
        };
    }

    @Override
    protected void tearDown() throws Exception
    {
        jiraAuthenticationContext = null;
        pluginAccessor = null;
        searchService = null;
        projectManager = null;
        permissionManager = null;
        dateProvider = null;
        super.tearDown();
    }

    public void testVersionWithoutReleaseDatesNotReturnedInResults() throws SearchException, ParseException
    {
        Issue anIssue = mock(Issue.class);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        when(anIssue.getCreated()).thenReturn(currentTime);
        when(anIssue.getUpdated()).thenReturn(currentTime);

        IssueType issueType = mock(IssueType.class);
        when(issueType.getNameTranslation(Matchers.<I18nHelper>anyObject())).thenReturn("");
        when(anIssue.getIssueTypeObject()).thenReturn(issueType);

        Status issueStatus = mock(Status.class);
        when(issueStatus.getNameTranslation(Matchers.<I18nHelper>anyObject())).thenReturn("");
        when(anIssue.getStatusObject()).thenReturn(issueStatus);
        when(dateProvider.getStart(same(anIssue), anyString())).thenReturn(new DateTime());
        when(dateProvider.getEnd(same(anIssue), anyString())).thenReturn(new DateTime().plusMinutes(30));

        when(searchService.search(Matchers.<User>anyObject(), Matchers.<Query>anyObject(), Matchers.<PagerFilter>anyObject())).thenReturn(
                new SearchResults(Arrays.asList(anIssue), 1, PagerFilter.getUnlimitedFilter())
        );

        QueryContext.ProjectIssueTypeContexts projectIssueTypeContexts = mock(QueryContext.ProjectIssueTypeContexts.class);
        when(projectIssueTypeContexts.getProjectIdInList()).thenReturn(Arrays.asList(10000L));

        QueryContext simpleQueryContext = mock(QueryContext.class);
        when(simpleQueryContext.getProjectIssueTypeContexts()).thenReturn(Arrays.asList(projectIssueTypeContexts));

        when(searchService.getSimpleQueryContext(Matchers.<User>anyObject(), Matchers.<Query>anyObject())).thenReturn(simpleQueryContext);

        Project aProject = mock(Project.class);
        when(projectManager.getProjectObj(anyLong())).thenReturn(aProject);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(aProject), Matchers.<User>anyObject())).thenReturn(true);

        Version versionWithoutReleaseDate = mock(Version.class);
        when(aProject.getVersions()).thenReturn(Arrays.asList(versionWithoutReleaseDate));

        EntityAsEventService.Result result = defaultEntityAsEventService.search(null, new HashSet<String>(Arrays.asList(DocumentConstants.ISSUE_DUEDATE)), true, null);

        assertNotNull(result);
        assertNotNull(result.issues);
        assertFalse(result.issues.isEmpty());

        assertNotNull(result.fixedVersions);
        assertTrue(result.fixedVersions.isEmpty());

        verify(versionWithoutReleaseDate, atLeastOnce()).getReleaseDate();
    }

    public void testVersionReturnedInResultsEvenIfNoIssueIsAssociatedWithIt() throws SearchException, ParseException
    {
        Issue anIssue = mock(Issue.class);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        when(anIssue.getCreated()).thenReturn(currentTime);
        when(anIssue.getUpdated()).thenReturn(currentTime);

        IssueType issueType = mock(IssueType.class);
        when(issueType.getNameTranslation(Matchers.<I18nHelper>anyObject())).thenReturn("");
        when(anIssue.getIssueTypeObject()).thenReturn(issueType);

        Status issueStatus = mock(Status.class);
        when(issueStatus.getNameTranslation(Matchers.<I18nHelper>anyObject())).thenReturn("");
        when(anIssue.getStatusObject()).thenReturn(issueStatus);

        when(dateProvider.getStart(same(anIssue), anyString())).thenReturn(new DateTime());
        when(dateProvider.getEnd(same(anIssue), anyString())).thenReturn(new DateTime().plusMinutes(30));

        when(searchService.search(Matchers.<User>anyObject(), Matchers.<Query>anyObject(), Matchers.<PagerFilter>anyObject())).thenReturn(
                new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter())
        );

        QueryContext.ProjectIssueTypeContexts projectIssueTypeContexts = mock(QueryContext.ProjectIssueTypeContexts.class);
        when(projectIssueTypeContexts.getProjectIdInList()).thenReturn(Arrays.asList(10000L));

        QueryContext simpleQueryContext = mock(QueryContext.class);
        when(simpleQueryContext.getProjectIssueTypeContexts()).thenReturn(Arrays.asList(projectIssueTypeContexts));
        
        when(searchService.getSimpleQueryContext(Matchers.<User>anyObject(), Matchers.<Query>anyObject())).thenReturn(simpleQueryContext);

        Project aProject = mock(Project.class);
        when(projectManager.getProjectObj(anyLong())).thenReturn(aProject);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(aProject), Matchers.<User>anyObject())).thenReturn(true);

        Version versionWithoutReleaseDate = mock(Version.class);
        Version version = mock(Version.class);
        when(version.getReleaseDate()).thenReturn(new Date());
        when(aProject.getVersions()).thenReturn(Arrays.asList(versionWithoutReleaseDate, version));

        EntityAsEventService.Result result = defaultEntityAsEventService.search(null, new HashSet<String>(Arrays.asList(DocumentConstants.ISSUE_DUEDATE)), true, null);

        assertNotNull(result);
        assertNotNull(result.issues);
        assertTrue(result.issues.isEmpty());

        assertNotNull(result.fixedVersions);
        assertFalse(result.fixedVersions.isEmpty());
    }


}
