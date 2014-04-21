package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;

/**
 * Represents the "menu" in the stalker bar.
 *
 * @since v4.2
 */
public class IssueMenu extends AbstractSeleniumPageObject implements PageObject
{
    private static final String MORE_ACTIONS_LOCATOR = Locators.ID.create("opsbar-operations_more");
    private static final String MORE_WORKFLOWS_LOCATOR = Locators.ID.create("opsbar-transitions_more");

    private final long defaultTimeout;

    public IssueMenu(SeleniumContext ctx, final long defaultTimeout)
    {
        super(ctx);
        this.defaultTimeout = defaultTimeout;
    }

    public IssueMenu(SeleniumContext ctx)
    {
        this(ctx, 2000);
    }

    public void assertReady(final long timeout)
    {
        assertThat.elementPresentByTimeout(Locators.CSS.create(".command-bar"));
    }

    public void clickOperation(LegacyIssueOperation operation)
    {
        clickWithMore(operation.getViewIssueMenuLocator(), MORE_ACTIONS_LOCATOR);
    }

    public void clickWorkflow(WorkflowTransition workflow)
    {
        clickWithMore(workflow.viewIssueLinkLocator(), MORE_WORKFLOWS_LOCATOR);
    }

    private void clickWithMore(final String linkLocator, final String moreLocator)
    {
        if (!client.isElementPresent(linkLocator))
        {
            client.click(moreLocator);
            assertThat.elementPresentByTimeout(linkLocator, defaultTimeout);
        }
        client.click(linkLocator);
    }
}
