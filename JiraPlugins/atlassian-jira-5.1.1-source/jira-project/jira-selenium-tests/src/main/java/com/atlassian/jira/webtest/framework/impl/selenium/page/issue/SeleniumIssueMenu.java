package com.atlassian.jira.webtest.framework.impl.selenium.page.issue;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPageSection;
import com.atlassian.jira.webtest.framework.model.DefaultIssueActions;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.issue.IssueMenu;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;

import java.util.EnumSet;
import java.util.Set;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.page.issue.IssueMenu} interface.
 *
 * @since v4.3
 */
public class SeleniumIssueMenu extends AbstractSeleniumPageSection<ViewIssue> implements IssueMenu
{
    private static final String DETECTOR = "div.command-bar";

    private static final String MORE_ACTIONS_LOCATOR = "opsbar-operations_more";
    private static final String MORE_WORKFLOWS_LOCATOR = "opsbar-transitions_more";
    private static final String MORE_ACTIONS_DROPDOWN_LOCATOR = "#opsbar-operations_more_drop";
//    private static final String MORE_WORKFLOWS_DROPDOWN_LOCATOR = "#opsbar-transitions_more_drop";

    private static final Set<? extends IssueOperation> NEW_PAGE_ACTIONS = EnumSet.of(
            DefaultIssueActions.EDIT,
            DefaultIssueActions.CREATE_SUBTASK,
            DefaultIssueActions.CONVERT_TO_SUBTASK,
            DefaultIssueActions.MOVE
    );

    private static boolean isNewPageAction(IssueOperation op)
    {
        return NEW_PAGE_ACTIONS.contains(op);
    }

    private final SeleniumLocator detector;

    private final SeleniumLocator moreActionsLocator;
    private final SeleniumLocator moreWorkflowsLocator;

    private final SeleniumLocator moreActionsDropdownLocator;
//    private final SeleniumLocator moreWorkflowsDropdownLocator;

    protected SeleniumIssueMenu(ViewIssue page, SeleniumContext context)
    {
        super(page, context);
        this.detector = css(DETECTOR);
        this.moreActionsLocator = id(MORE_ACTIONS_LOCATOR);
        this.moreWorkflowsLocator = id(MORE_WORKFLOWS_LOCATOR);
        this.moreActionsDropdownLocator = jQuery(MORE_ACTIONS_DROPDOWN_LOCATOR);
//        this.moreWorkflowsDropdownLocator = jQuery(MORE_WORKFLOWS_DROPDOWN_LOCATOR);
    }

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }

    @Override
    public ViewIssue invoke(IssueOperation issueOperation)
    {
        final SeleniumLocator toClick = id(issueOperation.id());
        if (toClick.element().isNotVisible().byDefaultTimeout())
        {
            invokeFromMores(issueOperation, toClick);
        }
        else
        {
            toClick.element().click();
        }
        if (isNewPageAction(issueOperation))
        {
            waitFor().pageLoad();
        }
        return page();
    }

    private void invokeFromMores(IssueOperation issueOp, SeleniumLocator toClick)
    {
        if (isInMoreActions(toClick))
        {
            triggerMoreActions();
        }
        else
        {
            triggerMoreWorkflows();
        }
        assertThat("Failed to locate element for issue operation: " + issueOp, toClick.element().isVisible(), byDefaultTimeout());
        toClick.element().click();
    }

    private boolean isInMoreActions(SeleniumLocator toClick)
    {
        return moreActionsVisible() && presentWithinMoreActions(toClick);
    }

    private boolean presentWithinMoreActions(SeleniumLocator toClick)
    {
        assertMoreActionsDropDownPresent();
        return moreActionsDropdownLocator.combine(toClick).element().isPresent().byDefaultTimeout();
    }

    private void assertMoreActionsDropDownPresent()
    {
        if (!moreActionsDropdownLocator.element().isPresent().byDefaultTimeout())
        {
            triggerMoreActions();
            assertThat(moreActionsDropdownLocator.element().isVisible(), byDefaultTimeout());
            triggerMoreActions();
            assertThat(and(moreActionsDropdownLocator.element().isPresent(), moreActionsDropdownLocator.element().isNotVisible()),
                    byDefaultTimeout());
        }
    }

    private void triggerMoreActions()
    {
        moreActionsLocator.element().click();
    }

    private void triggerMoreWorkflows()
    {
        moreWorkflowsLocator.element().click();
    }

    private boolean moreActionsVisible()
    {
        return moreActionsLocator.element().isVisible().byDefaultTimeout();
    }

}
