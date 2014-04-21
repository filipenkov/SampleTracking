package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.model.IssueOperation;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Issue menu on the View Issue page.
 *
 * @since v4.4
 */
public class IssueMenu
{
    private static final String MORE_ACTIONS_LOCATOR = "opsbar-operations_more";
    private static final String MORE_WORKFLOWS_LOCATOR = "opsbar-transitions_more";


    @ElementBy(id = MORE_ACTIONS_LOCATOR)
    private PageElement moreActionsTrigger;

    @ElementBy(id = MORE_WORKFLOWS_LOCATOR)
    private PageElement moreWorkflowsTrigger;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder locator;

    @Inject
    private AtlassianWebDriver driver;

    private final ViewIssuePage viewIssuePage;


    public IssueMenu(ViewIssuePage viewIssuePage)
    {
        this.viewIssuePage = checkNotNull(viewIssuePage);
    }

    /**
     * Invoke given <tt>issueOperation</tt>.
     *
     * @param issueOperation
     * @return
     */
    public ViewIssuePage invoke(IssueOperation issueOperation)
    {
        PageElement topLevelIssueOperation = locator.find(By.cssSelector("li #" + issueOperation.id()));

        if (topLevelIssueOperation.isVisible())
        {
            topLevelIssueOperation.click();
        }
        else
        {
            invokeFromMores(issueOperation);
        }
        return viewIssuePage;
    }

    private void invokeFromMores(IssueOperation issueOp)
    {
        if (isInMoreActions(issueOp))
        {
            openMoreActions();
        }
        else
        {
            openMoreWorkflows();
        }
        final PageElement toClick = locator.find(By.id(issueOp.id()));
        toClick.click();
    }

    private boolean isInMoreActions(IssueOperation issueOp)
    {
        return driver.elementExists(By.cssSelector("#" + MORE_ACTIONS_LOCATOR + " + div #" + issueOp.id()));
    }

    private void openMoreActions()
    {
        moreActionsTrigger.click();
        waitUntilTrue(locator.find(By.cssSelector("#" + MORE_ACTIONS_LOCATOR + ".active")).timed().isPresent());
    }

    private void openMoreWorkflows()
    {
        moreWorkflowsTrigger.click();
        waitUntilTrue(locator.find(By.cssSelector("#" + MORE_WORKFLOWS_LOCATOR + ".active")).timed().isPresent());
    }
}
