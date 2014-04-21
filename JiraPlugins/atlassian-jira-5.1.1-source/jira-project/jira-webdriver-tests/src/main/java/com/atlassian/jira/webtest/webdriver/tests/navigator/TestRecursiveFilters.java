package com.atlassian.jira.webtest.webdriver.tests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUE_NAVIGATOR })
public class TestRecursiveFilters extends BaseJiraWebTest
{
    /**
     * JRA-24366 Test if opening a filter that has a cyclical reference causes stack overflow exception.
     * After the fix it should be possible to open the test and see an error message.
     */
    @Test
    public void testRecursiveFilters() {
        backdoor.project().addProject("Test", "TST", "admin");
        backdoor.searchRequests().createFilter("admin", "filter=Test1", "Test1", "");
        String filter1Id = backdoor.searchRequests().createFilter("admin", "filter=Test1", "Test2", "");
        AdvancedSearch search = jira.gotoLoginPage().loginAsSysAdmin(AdvancedSearch.class, Long.valueOf(filter1Id));
        assertEquals("Field 'filter' with value 'Test1' matches filter 'Test1' and causes a cyclical reference,"
                + " this query can not be executed and should be edited.", search.getJQLError());
    }
}
