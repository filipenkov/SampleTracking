package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests specific clauses ability to be used when their system properties have been disabled.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestClausesDependentOnProperties extends FuncTestCase
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreBlankInstance();
        }
    }

    public void testUserClausesSearchOnFullName() throws Exception
    {
        administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        assertSearchResults("assignee = '" + FRED_FULLNAME + "'");
        assertSearchResults("assignee = fred");
        assertSearchResults("reporter = '" + FRED_FULLNAME + "'");
        assertSearchResults("reporter = fred");
        assertSearchResults("UserCF = '" + FRED_FULLNAME + "'");
        assertSearchResults("UserCF = fred");
    }

    public void testTimeTrackingClausesInvalid() throws Exception
    {
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        assertSearchResults("originalEstimate = 999");
        assertSearchResults("timeOriginalEstimate = 999");
        assertSearchResults("remainingEstimate = 999");
        assertSearchResults("timeEstimate = 999");
        assertSearchResults("timeSpent = 999");
        assertSearchResults("workRatio = 999");

        administration.timeTracking().disable();

        assertSearchError("originalEstimate = 999", "Field 'originalEstimate' does not exist or you do not have permission to view it.");
        assertSearchError("timeOriginalEstimate = 999", "Field 'timeOriginalEstimate' does not exist or you do not have permission to view it.");
        assertSearchError("remainingEstimate = 999", "Field 'remainingEstimate' does not exist or you do not have permission to view it.");
        assertSearchError("timeEstimate = 999", "Field 'timeEstimate' does not exist or you do not have permission to view it.");
        assertSearchError("timeSpent = 999", "Field 'timeSpent' does not exist or you do not have permission to view it.");
        assertSearchError("workRatio = 999", "Field 'workRatio' does not exist or you do not have permission to view it.");
    }

    public void testVotingClausesInvalid() throws Exception
    {
        administration.generalConfiguration().enableVoting();

        assertSearchResults("votes = 123");

        administration.generalConfiguration().disableVoting();
        
        assertSearchError("votes = 123", "Field 'votes' does not exist or you do not have permission to view it.");
    }

    public void testSubTaskClausesInvalid() throws Exception
    {
        administration.subtasks().enable();

        assertSearchError("parent = 'HSP-1'", "An issue with key 'HSP-1' does not exist for field 'parent'.");

        administration.subtasks().disable();

        assertSearchError("parent = 'HSP-1'", "Field 'parent' does not exist or you do not have permission to view it.");
    }

    /**
     * Executes a JQL query search and asserts the expected issue keys from the result set.
     *
     * @param jqlQuery the query to execute
     * will be asserted against the number of results
     */
    private void assertSearchResults(final String jqlQuery)
    {
        final List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), 0));

        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
    }

    private void assertSearchError(final String jqlQuery, final String errorMsg)
    {
        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), errorMsg);
    }
}
