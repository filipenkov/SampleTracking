package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlVoterAndWatcherFields extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlVoterAndWatcherFields.xml");
    }

    public void testVoterField()
    {
        navigation.login(BOB_USERNAME);

        assertSearchWithResults("voter = currentUser()",  "HSP-4", "HSP-3");
        assertSearchWithResults("voter = bob", "HSP-4", "HSP-3");
        // check that it ignores case
        assertSearchWithResults("voter = BoB", "HSP-4", "HSP-3");

        assertSearchWithResults("voter = fred");
        assertSearchWithResults("voter = FreD");

        navigation.login(FRED_USERNAME);

        // sees own issue despite not being able to see other votes
        assertSearchWithResults("voter = currentUser()", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = fred", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = FreD", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = bob", "HSP-4");
        // check that it ignores case
        assertSearchWithResults("voter = BoB", "HSP-4");

        // reassign HSP-2 to fred
        navigation.login(ADMIN_USERNAME);
        assertSearchWithResults("voter = fred", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = FreD", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = bob", "HSP-4", "HSP-3");
        assertSearchWithResults("voter = BoB", "HSP-4", "HSP-3");

        assertSearchWithWarning("voter = dingbat", "The value 'dingbat' does not exist for the field 'voter'.");
    }

    public void testWatcherField()
    {
        navigation.login(BOB_USERNAME);

        assertSearchWithResults("watcher = currentUser()",  "HSP-3");
        assertSearchWithResults("watcher = bob", "HSP-3");
        // check that it ignores case
        assertSearchWithResults("watcher = BoB", "HSP-3");

        assertSearchWithResults("watcher = fred");
        assertSearchWithResults("watcher = FreD");

        navigation.login(FRED_USERNAME);

        // sees own issue despite not being able to see other votes
        assertSearchWithResults("watcher = currentUser()", "HSP-4", "HSP-3");
        assertSearchWithResults("watcher = fred", "HSP-4", "HSP-3");
        assertSearchWithResults("watcher = FreD", "HSP-4", "HSP-3");
        assertSearchWithResults("watcher = bob");
        // check that it ignores case
        assertSearchWithResults("watcher = BoB");

        // reassign HSP-2 to fred
        navigation.login(ADMIN_USERNAME);
        assertSearchWithResults("watcher = fred", "HSP-4", "HSP-3");
        assertSearchWithResults("watcher = FreD", "HSP-4", "HSP-3");
        assertSearchWithResults("watcher = bob", "HSP-3");
        assertSearchWithResults("watcher = BoB", "HSP-3");

        assertSearchWithWarning("watcher = dingbat", "The value 'dingbat' does not exist for the field 'watcher'.");
    }
}