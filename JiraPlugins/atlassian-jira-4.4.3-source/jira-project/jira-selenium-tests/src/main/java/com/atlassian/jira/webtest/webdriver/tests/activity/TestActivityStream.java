package com.atlassian.jira.webtest.webdriver.tests.activity;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Web test for the activity stream gadget.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.GADGETS, Category.ACTIVITY_STREAMS })
@Restore("xml/TestActivityStream.xml")
public class TestActivityStream extends BaseJiraWebTest
{
    @Inject private DashboardPage dashboard;

    // TODO something is missing here! implementation? ;)

    @Test
    public void shouldShowDeletedStatus()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
    }
}