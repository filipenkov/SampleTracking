package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.model.IssueOperation;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
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
    private static final String MORE_ACTIONS_DROPDOWN_LOCATOR = "opsbar-operations_more_drop";
    private static final String MORE_WORKFLOWS_DROPDOWN_LOCATOR = "opsbar-transitions_more_drop";


    @ElementBy(id = MORE_ACTIONS_LOCATOR)
    private PageElement moreActionsTrigger;

    @ElementBy(id = MORE_WORKFLOWS_LOCATOR)
    private PageElement moreWorkflowsTrigger;

    @ElementBy(id = MORE_ACTIONS_DROPDOWN_LOCATOR)
    private PageElement moreActionsDropdown;

    @ElementBy(id = MORE_WORKFLOWS_DROPDOWN_LOCATOR)
    private PageElement moreWorkflowsDropdown;

    @Inject
    private PageBinder pageBinder;

    private final ViewIssuePage viewIssuePage;


    public IssueMenu(ViewIssuePage viewIssuePage)
    {
        this.viewIssuePage = checkNotNull(viewIssuePage);
    }

    /**
     * Check if given <tt>issueOperation</tt> is accessible from this issue menu.
     *
     * @param issueOperation issue operation to check
     * @return <code>true</code>, if given <tt>issueOperation</tt> is accessible
     */
    public boolean isAccessible(IssueOperation issueOperation)
    {
        PageElement toCheck = pageBinder.bind(PageElement.class, By.id(issueOperation.id()));
        return toCheck.isVisible() || checkInMores(toCheck);
    }

    /**
     * Invoke given <tt>issueOperation</tt>. If {@link #isAccessible(com.atlassian.jira.pageobjects.model.IssueOperation)}
     * returns <code>false</code>, this method will throw exception.
     *
     * @param issueOperation
     * @return
     */
    public ViewIssuePage invoke(IssueOperation issueOperation)
    {
        PageElement toClick = pageBinder.bind(PageElement.class, By.id(issueOperation.id()));
        if (!toClick.isVisible())
        {
            invokeFromMores(issueOperation, toClick);
        }
        else
        {
            toClick.click();
        }
        return viewIssuePage;
    }

    private boolean checkInMores(PageElement toClick)
    {
         return isInMoreActions(toClick) || isInMoreWorkflows(toClick);
    }

    private void invokeFromMores(IssueOperation issueOp, PageElement toClick)
    {
        if (isInMoreActions(toClick))
        {
            triggerMoreActions();
        }
        else
        {
            triggerMoreWorkflows();
        }
        waitUntilTrue("Failed to locate element for issue operation: " + issueOp, toClick.timed().isVisible());
        toClick.click();
    }

    private boolean isInMoreActions(PageElement toClick)
    {
        return moreActionsVisible() && presentWithinMoreActions(toClick);
    }

    private boolean isInMoreWorkflows(PageElement toClick)
    {
        return moreWorkflowsVisible() && presentWithinMoreWorkflows(toClick);
    }

    private boolean presentWithinMoreActions(PageElement toClick)
    {
        enforceMoreActionsDropDownPresent();
        return toClick.timed().isPresent().byDefaultTimeout();
    }

    private boolean presentWithinMoreWorkflows(PageElement toClick)
    {
        enforceMoreWorkflowsDropDownPresent();
        return toClick.timed().isPresent().byDefaultTimeout();
    }

    private void enforceMoreActionsDropDownPresent()
    {
        enforceDropdownAppendedToPage(moreActionsTrigger, moreWorkflowsDropdown);
    }

    private void enforceMoreWorkflowsDropDownPresent()
    {
        enforceDropdownAppendedToPage(moreWorkflowsTrigger, moreWorkflowsDropdown);
    }

    private void enforceDropdownAppendedToPage(PageElement trigger, PageElement dropdown)
    {
        if (!dropdown.timed().isPresent().byDefaultTimeout())
        {
            // open
            trigger.click();
            waitUntilTrue(dropdown.timed().isVisible());
            // close
            trigger.click();
            waitUntilTrue(and(dropdown.timed().isPresent(), not(dropdown.timed().isVisible())));
        }
    }

    private void triggerMoreActions()
    {
        moreActionsTrigger.click();
    }

    private void triggerMoreWorkflows()
    {
        moreWorkflowsTrigger.click();
    }

    private boolean moreActionsVisible()
    {
        return moreActionsTrigger.isVisible();
    }

    private boolean moreWorkflowsVisible()
    {
        return moreWorkflowsTrigger.isVisible();
    }
}
