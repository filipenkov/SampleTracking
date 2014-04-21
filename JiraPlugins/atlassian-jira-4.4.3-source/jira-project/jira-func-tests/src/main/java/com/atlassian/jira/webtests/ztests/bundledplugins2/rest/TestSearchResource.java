package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchRequest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchResult;
import com.google.common.collect.Sets;
import org.hamcrest.CoreMatchers;

import java.util.Set;

import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.matcher.ContainsStringThatStartsWith.containsStringThatStartsWith;
import static java.lang.Math.min;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the issue search functionality.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestSearchResource extends RestFuncTest
{
    private static final String HSP = "HSP";
    private static final String MKY = "MKY";
    private static final String HSP_1 = HSP + "-1";

    private SearchClient searchClient;
    private IssueClient issueClient;

    public void testSearchShouldFilterOutIssuesFromNonBrowseableProjects() throws Exception
    {
        // HSP is not viewable by anonymous
        SearchResult mkyResults = searchClient.anonymous().postSearch(new SearchRequest().jql("project = " + HSP));
        assertThat(mkyResults.total, equalTo(0));
        assertThat(mkyResults.issues.size(), equalTo(0));

        // MKY is "public"
        SearchResult hspResults = searchClient.anonymous().postSearch(new SearchRequest().jql("project = " + MKY));
        assertThat(hspResults.total, equalTo(4));
        assertThat(hspResults.issues.size(), equalTo(4));

        // this should be identical to the above
        SearchResult allResults = searchClient.anonymous().postSearch(new SearchRequest().jql(""));
        assertThat(allResults.issues, equalTo(hspResults.issues));
    }

    public void testSearchMaxResultsShouldDefaultTo50() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest());
        assertThat(results.maxResults, equalTo(50));
    }

    public void testSearchMaxResultsIsNotAllowedToExceed1000() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest().maxResults(2000));
        assertThat(results.maxResults, equalTo(1000));
    }

    public void testSearchShouldReturnPagesWithAtMostThreeIssues() throws Exception
    {
        final int issueCount = 8;
        final int pageSize = 3;

        int startAt;
        int pageNum = 0;
        Set<Issue> keysReturnedInPreviousPage = Sets.newHashSet();

        while ((startAt = (pageSize * pageNum++)) < issueCount)
        {
            SearchResult page = searchClient.postSearch(new SearchRequest().jql("order by key").maxResults(pageSize).startAt(startAt));
            assertThat(page.startAt, equalTo(startAt));
            assertThat(page.total, equalTo(issueCount));
            assertThat(page.issues.size(), equalTo(min(pageSize, issueCount - startAt)));
            assertTrue(Sets.intersection(keysReturnedInPreviousPage, Sets.newHashSet(page.issues)).isEmpty());
            keysReturnedInPreviousPage.addAll(page.issues);
        }
    }

    public void testSearchStartAtAndMaxResultsShouldHaveDefaultValues() throws Exception
    {
        SearchResult results = searchClient.postSearch(new SearchRequest());
        assertThat(results.startAt, equalTo(0));
        assertThat(results.maxResults, CoreMatchers.<Integer>notNullValue());
    }

    public void testSearchWithBadJqlShouldReturnStatusCode400() throws Exception
    {
        Response results = searchClient.postSearchResponse(new SearchRequest().jql("zomg!!!11111"));

        assertThat(results.statusCode, equalTo(400));
        assertThat(results.entity.errorMessages, containsStringThatStartsWith("Error in the JQL Query:"));
    }

    public void testSearchWithNoJqlShouldReturnAllIssues() throws Exception
    {
        SearchRequest searchRequest = new SearchRequest().jql(null);

        assertFalse(searchClient.getSearch(searchRequest).issues.isEmpty());
        assertFalse(searchClient.postSearch(searchRequest).issues.isEmpty());
    }

    public void testSearchResultsShouldReturnOnlyTheIssueKeyAndSelf() throws Exception
    {
        SearchResult hsp1Results = searchClient.postSearch(new SearchRequest().jql("key = " + HSP_1));
        assertThat(hsp1Results.total, equalTo(1));
        assertThat(hsp1Results.issues.size(), equalTo(1));

        Issue hsp1 = hsp1Results.issues.get(0);
        assertThat(hsp1.key, equalTo(HSP_1));

        // validate the self link
        Issue selfIssue = issueClient.getFromURL(hsp1.self);
        assertThat(hsp1.key, equalTo(selfIssue.key));

        // workaround until we fix https://studio.atlassian.com/browse/REST-159
        // workaround removed while we have debug logging in place to try and fix this.
        assertThat(hsp1.self, equalTo(selfIssue.self));
//        assertThat(hsp1.self.replace("/latest/", "/2.0.alpha1/"), equalTo(selfIssue.self.replace("/latest/", "/2.0.alpha1/")));
    }

    public void testSearchUsingGetReturnsTheSameAsUsingPost() throws Exception
    {
        SearchRequest aSearch = new SearchRequest();

        SearchResult postResults = searchClient.postSearch(aSearch);
        SearchResult getResults = searchClient.getSearch(aSearch);
        assertEquals(getResults, postResults);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueSearch.xml");
        searchClient = new SearchClient(getEnvironmentData());
        issueClient = new IssueClient(getEnvironmentData());
    }
}
