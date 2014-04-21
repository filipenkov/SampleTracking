package com.atlassian.jira.webtest.selenium.renderers;

import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * @since v4.3
 */
public abstract class AbstractPluggableRendererComponents extends JiraSeleniumTest
{
    protected static final String PREVIEW_DIV_LOCATOR = "xpath=//div[@id='comment-wiki-edit']/div[@class='content-inner']";
    protected static final int PREVIEW_WAIT = 10000;
    protected static final String DELETE_COMMENT_RENDERED_CONTENT_LOCATOR = "xpath=//form[@name='jiraform']/table/tbody/tr[7]/td[@class='fieldValueArea']";

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestPluggableRendererComponents.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");
    }
}
