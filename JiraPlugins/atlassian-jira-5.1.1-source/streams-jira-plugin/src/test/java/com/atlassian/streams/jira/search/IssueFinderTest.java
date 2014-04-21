package com.atlassian.streams.jira.search;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.api.StreamsFilterType.Operator.IS;
import static com.atlassian.streams.api.StreamsFilterType.Operator.NOT;
import static com.atlassian.streams.jira.JiraFilterOptionProvider.ISSUE_TYPE;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ISSUE_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueFinderTest
{
    private final static String ISSUE_TYPE1 = "1";
    private final static String ISSUE_TYPE2 = "2";
    private final static String PROJECT_STRM = "STRM";
    private final static String PROJECT_UPM = "UPM";

    @Mock UserHistory userHistory;
    @Mock IssueSearch issueSearch;
    @Mock PermissionManager permissionManager;
    @Mock JiraAuthenticationContext authenticationContext;

    @Mock ActivityRequest request;

    @Mock Issue strm1;
    @Mock Issue strm2;
    @Mock Issue strm3;
    @Mock Issue upm1;
    @Mock Issue upm2;

    @Mock IssueType issueType1;
    @Mock IssueType issueType2;

    @Mock Project projectStrm;
    @Mock Project projectUpm;

    IssueFinder issueFinder;

    @Before
    public void setup()
    {
        issueFinder = new IssueFinder(userHistory, issueSearch, permissionManager, authenticationContext);

        when(issueType1.getId()).thenReturn(ISSUE_TYPE1);
        when(issueType2.getId()).thenReturn(ISSUE_TYPE2);

        when(projectStrm.getKey()).thenReturn(PROJECT_STRM);
        when(projectUpm.getKey()).thenReturn(PROJECT_UPM);

        when(strm1.getKey()).thenReturn("STRM-1");
        when(strm1.toString()).thenReturn("STRM-1");
        when(strm1.getProjectObject()).thenReturn(projectStrm);
        when(strm1.getIssueTypeObject()).thenReturn(issueType1);
        when(strm2.getKey()).thenReturn("STRM-2");
        when(strm2.toString()).thenReturn("STRM-2");
        when(strm2.getProjectObject()).thenReturn(projectStrm);
        when(strm2.getIssueTypeObject()).thenReturn(issueType2);
        when(strm3.getKey()).thenReturn("STRM-3");
        when(strm3.toString()).thenReturn("STRM-3");
        when(strm3.getProjectObject()).thenReturn(projectStrm);
        when(strm3.getIssueTypeObject()).thenReturn(issueType1);
        when(upm1.getKey()).thenReturn("UPM-1");
        when(upm1.toString()).thenReturn("UPM-1");
        when(upm1.getProjectObject()).thenReturn(projectUpm);
        when(upm1.getIssueTypeObject()).thenReturn(issueType1);
        when(upm2.getKey()).thenReturn("UPM-2");
        when(upm2.toString()).thenReturn("UPM-2");
        when(upm2.getProjectObject()).thenReturn(projectUpm);
        when(upm2.getIssueTypeObject()).thenReturn(issueType1);

        when(request.getStandardFilters()).thenReturn(ImmutableMultimap.<String, Pair<Operator, Iterable<String>>>of());
        when(request.getProviderFilters()).thenReturn(ImmutableMultimap.<String, Pair<Operator, Iterable<String>>>of());
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), any(Issue.class), any(User.class))).thenReturn(true);
    }

    @Test
    public void assertThatOnlyItemKeysInIsOperatorShowUpInResults()
    {
        Pair<Operator, Iterable<String>> filter = mkFilter(IS, ImmutableList.of(strm1.getKey(), strm3.getKey(), upm1.getKey()));
        when(request.getStandardFilters()).thenReturn(ImmutableMultimap.of(ISSUE_KEY.getKey(), filter));
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1, strm2, strm3));

        Set<Issue> filteredIssues = ImmutableSet.of(strm1, strm3);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatItemKeysInNotOperatorDoNotShowUpInResults()
    {
        Pair<Operator, Iterable<String>> filter = mkFilter(NOT, ImmutableList.of(strm1.getKey(), strm3.getKey(), upm1.getKey()));
        when(request.getStandardFilters()).thenReturn(ImmutableMultimap.of(ISSUE_KEY.getKey(), filter));
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1, strm2, strm3));

        Set<Issue> filteredIssues = ImmutableSet.of(strm2);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatOnlyIssuesUserHasBrowsePermissionForShowUpInResults()
    {
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1, strm2, strm3));
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(strm2), any(User.class))).thenReturn(false);

        Set<Issue> filteredIssues = ImmutableSet.of(strm1, strm3);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatUserHistoryGetsFilteredByIsIssueTypeFilter()
    {
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1));
        when(userHistory.find(request)).thenReturn(ImmutableSet.of(strm2));

        Pair<Operator, Iterable<String>> filter = mkFilter(IS, ImmutableList.of(ISSUE_TYPE2));
        when(request.getProviderFilters()).thenReturn(ImmutableMultimap.of(ISSUE_TYPE, filter));

        Set<Issue> filteredIssues = ImmutableSet.of(strm2);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatUserHistoryGetsFilteredByNotIssueTypeFilter()
    {
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1));
        when(userHistory.find(request)).thenReturn(ImmutableSet.of(strm2));

        Pair<Operator, Iterable<String>> filter = mkFilter(NOT, ImmutableList.of(ISSUE_TYPE2));
        when(request.getProviderFilters()).thenReturn(ImmutableMultimap.of(ISSUE_TYPE, filter));

        Set<Issue> filteredIssues = ImmutableSet.of(strm1);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatUserHistoryGetsFilteredByIsProjectKeyFilter()
    {
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1));
        when(userHistory.find(request)).thenReturn(ImmutableSet.of(strm2, upm1));

        Pair<Operator, Iterable<String>> filter = mkFilter(IS, ImmutableList.of(PROJECT_UPM));
        when(request.getStandardFilters()).thenReturn(ImmutableMultimap.of(PROJECT_KEY, filter));

        Set<Issue> filteredIssues = ImmutableSet.of(upm1);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    @Test
    public void assertThatUserHistoryGetsFilteredByNotProjectKeyFilter()
    {
        when(issueSearch.search(request)).thenReturn(ImmutableSet.of(strm1));
        when(userHistory.find(request)).thenReturn(ImmutableSet.of(strm2, upm1));

        Pair<Operator, Iterable<String>> filter = mkFilter(NOT, ImmutableList.of(PROJECT_UPM));
        when(request.getStandardFilters()).thenReturn(ImmutableMultimap.of(PROJECT_KEY, filter));

        Set<Issue> filteredIssues = ImmutableSet.of(strm1, strm2);
        assertThat(issueFinder.find(request), is(equalTo(filteredIssues)));
    }

    private static Pair<Operator, Iterable<String>> mkFilter(Operator op, Iterable<String> values)
    {
        return Pair.<Operator, Iterable<String>>pair(op, values);
    }
}
