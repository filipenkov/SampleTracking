package com.atlassian.jira.extra.icalfeed.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProvider;
import com.atlassian.jira.extra.icalfeed.util.QueryUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultEntityAsServiceTestCase extends TestCase
{

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private FieldManager fieldManager;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private SearchService searchService;

    @Mock
    private DateProvider dateProvider;

    @Mock
    private QueryUtil queryUtil;

    @Mock
    private Field field;

    private DefaultEntityAsEventService defaultEntityAsEventService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        when(fieldManager.getField(anyString())).thenReturn(field);
        defaultEntityAsEventService = new DefaultEntityAsEventService(jiraAuthenticationContext, fieldManager, null, null, pluginAccessor, searchService, queryUtil)
        {
            @Override
            Collection<DateProvider> getDateProvider(Field field)
            {
                return Arrays.asList(dateProvider);
            }
        };
    }

    @Override
    protected void tearDown() throws Exception
    {
        jiraAuthenticationContext = null;
        fieldManager = null;
        pluginAccessor = null;
        searchService = null;
        queryUtil = null;
        dateProvider = null;
        field = null;
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

        DateTime startDate = new DateTime();
        when(dateProvider.getStart(same(anIssue), Matchers.<Field>anyObject())).thenReturn(startDate);
        when(dateProvider.getEnd(same(anIssue), Matchers.<Field>anyObject(), same(startDate))).thenReturn(startDate.plusMinutes(30));

        when(searchService.search(Matchers.<User>anyObject(), Matchers.<Query>anyObject(), Matchers.<PagerFilter>anyObject())).thenReturn(
                new SearchResults(Arrays.asList(anIssue), 1, PagerFilter.getUnlimitedFilter())
        );

        Project aProject = mock(Project.class);
        Version versionWithoutReleaseDate = mock(Version.class);
        when(aProject.getVersions()).thenReturn(Arrays.asList(versionWithoutReleaseDate));

        when(queryUtil.getBrowseableProjectsFromQuery(Matchers.<User>anyObject(), Matchers.<Query>anyObject())).thenReturn(
                new HashSet<Project>(Arrays.asList(aProject))
        );

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

        DateTime startDate = new DateTime();
        when(dateProvider.getStart(same(anIssue), Matchers.<Field>anyObject())).thenReturn(startDate);
        when(dateProvider.getEnd(same(anIssue), Matchers.<Field>anyObject(), same(startDate))).thenReturn(startDate.plusMinutes(30));

        when(searchService.search(Matchers.<User>anyObject(), Matchers.<Query>anyObject(), Matchers.<PagerFilter>anyObject())).thenReturn(
                new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter())
        );

        Project aProject = mock(Project.class);
        Version versionWithoutReleaseDate = mock(Version.class);
        Version version = mock(Version.class);
        when(version.getReleaseDate()).thenReturn(new Date());
        when(aProject.getVersions()).thenReturn(Arrays.asList(versionWithoutReleaseDate, version));

        when(queryUtil.getBrowseableProjectsFromQuery(Matchers.<User>anyObject(), Matchers.<Query>anyObject())).thenReturn(
                new HashSet<Project>(Arrays.asList(aProject))
        );

        EntityAsEventService.Result result = defaultEntityAsEventService.search(null, new HashSet<String>(Arrays.asList(DocumentConstants.ISSUE_DUEDATE)), true, null);

        assertNotNull(result);
        assertNotNull(result.issues);
        assertTrue(result.issues.isEmpty());

        assertNotNull(result.fixedVersions);
        assertFalse(result.fixedVersions.isEmpty());
    }


}
