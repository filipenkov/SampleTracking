package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestUserNameSearching extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestUserNameSearching.xml");
    }

    public void testSystemFieldSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("assignee", "assignee");
        _testSearchByFullNameDoesntFit("assignee");
        _testSearchByEmailDoesntFit("assignee");
        _testSearchByEmailReturnsResultsByEmail("assignee");
    }

    public void testUserPickerSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("userpicker", "customfield_10000");
        _testSearchByFullNameDoesntFit("userpicker");
        _testSearchByEmailDoesntFit("userpicker");
        _testSearchByEmailReturnsResultsByEmail("userpicker");
    }

    public void testUserGroupPickerSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("usergrouppicker", "customfield_10001");
        _testSearchByFullNameDoesntFit("usergrouppicker");
        _testSearchByEmailDoesntFit("usergrouppicker");
        _testSearchByEmailReturnsResultsByEmail("usergrouppicker");
    }

    public void _testSearchByUserNameFits(String field, String formName) throws Exception
    {
        assertFitsFilterForm(field + " = admin", createFilterFormParam(formName, ADMIN_USERNAME));
        assertions.getIssueNavigatorAssertions().assertSearchResults(CollectionBuilder.<SearchResultsCondition>newBuilder(
                new NumberOfIssuesCondition(text, 1),
                new ContainsIssueKeysCondition(text, "HSP-1")
        ).asList());
    }

    public void _testSearchByFullNameDoesntFit(String field) throws Exception
    {
        assertTooComplex(field + " = " + ADMIN_FULLNAME);
        assertions.getIssueNavigatorAssertions().assertSearchResults(CollectionBuilder.<SearchResultsCondition>newBuilder(
                new NumberOfIssuesCondition(text, 1),
                new ContainsIssueKeysCondition(text, "HSP-1")
        ).asList());
    }

    public void _testSearchByEmailDoesntFit(String field) throws Exception
    {
        assertTooComplex(field + " = 'admin@example.com'");
        assertions.getIssueNavigatorAssertions().assertSearchResults(CollectionBuilder.<SearchResultsCondition>newBuilder(
                new NumberOfIssuesCondition(text, 1),
                new ContainsIssueKeysCondition(text, "HSP-1")
        ).asList());
    }

    public void _testSearchByEmailReturnsResultsByEmail(String field) throws Exception
    {
        navigation.issueNavigator().createSearch(field + " = 'email@example.com'");
        assertions.getIssueNavigatorAssertions().assertSearchResults(CollectionBuilder.<SearchResultsCondition>newBuilder(
                new NumberOfIssuesCondition(text, 1),
                new ContainsIssueKeysCondition(text, "HSP-3")
        ).asList());
    }
}
