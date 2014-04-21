package com.atlassian.jira.webtest.webdriver.tests.common;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.webtest.webdriver.setup.SingleJiraWebTestRunner;
import com.atlassian.pageobjects.PageBinder;
import com.google.inject.Inject;
import org.junit.runner.RunWith;

/**
 * <p>
 * Lightweight base class mainly containing annotations common for all tests.
 *
 * <p>
 * DO NOT put any utility methods here. Use page objects framework for that.
 *
 * @since v4.4
 */
@RunWith(SingleJiraWebTestRunner.class)
public abstract class BaseJiraWebTest
{
    @Inject protected JiraTestedProduct jira;
    @Inject protected PageBinder pageBinder;

}
