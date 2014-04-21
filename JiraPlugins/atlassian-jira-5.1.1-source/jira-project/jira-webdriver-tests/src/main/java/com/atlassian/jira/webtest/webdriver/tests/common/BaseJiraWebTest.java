package com.atlassian.jira.webtest.webdriver.tests.common;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.config.junit4.rule.DirtyWarningTerminatorRule;
import com.atlassian.jira.pageobjects.framework.util.DirtyWarningTerminator;
import com.atlassian.jira.webtest.webdriver.setup.SingleJiraWebTestRunner;
import com.atlassian.pageobjects.PageBinder;
import com.google.inject.Inject;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * <p>
 * Lightweight base class mainly containing annotations common for all tests.
 *
 * <p>
 * DO NOT put any utility methods here. Use page objects framework for that. In fact, do not put anything here
 * without permission :P
 *
 * @since v4.4
 */
@RunWith(SingleJiraWebTestRunner.class)
public abstract class BaseJiraWebTest
{
    @Inject protected static JiraTestedProduct jira;
    @Inject protected static PageBinder pageBinder;
    @Inject protected static Backdoor backdoor;
    @Inject protected static DirtyWarningTerminator dirtyWarningTerminator;


    @Rule public DirtyWarningTerminatorRule dirtyWarningRule = new DirtyWarningTerminatorRule(dirtyWarningTerminator);

}
