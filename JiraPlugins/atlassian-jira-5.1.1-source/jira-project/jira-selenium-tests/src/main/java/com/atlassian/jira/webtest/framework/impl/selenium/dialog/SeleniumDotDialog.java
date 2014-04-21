package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.webtest.ui.keys.Sequences.keys;

/**
 * {@link com.atlassian.jira.webtest.framework.dialog.DotDialog} in the Selenium World&trade;. 
 *
 * @since v4.3
 */
public abstract class SeleniumDotDialog extends AbstractSeleniumAuiDialog<DotDialog> implements DotDialog
{
    private static final String DIALOG_ID = "issue-actions-dialog";
    private static final String QUERYABLE_CONTAINER = "issueactions-queryable-container";

    private final IssueData issueData;

    private final SeleniumLocator queryableContainerLocator;

    private final SeleniumDDInput input;
    private final SeleniumDDDropDown dropDown;
    private final CloseModeImpl closeMode;

    protected SeleniumDotDialog(SeleniumContext context, IssueData issueData)
    {
        super(context, DIALOG_ID);
        this.issueData = issueData;
        this.queryableContainerLocator = detector().combine(id(QUERYABLE_CONTAINER));
        this.input = new SeleniumDDInput(this, context);
        this.dropDown = new SeleniumDDDropDown(this, context);
        this.closeMode = new CloseModeImpl();
    }

    protected SeleniumDotDialog(SeleniumContext context)
    {
        this(context, null);
    }

    /**
     * Parent page of this dot dialog.
     *
     * @return parent page
     */
    protected abstract IssueActionsParent parentPage();


    /* -------------------------------------------------- LOCATORS -------------------------------------------------- */

    SeleniumLocator queryableContainerLocator()
    {
        return queryableContainerLocator;
    }

    /* --------------------------------------------------- QUERIES -------------------------------------------------- */

    @Override
    public IssueData issueData()
    {
        return issueData;
    }

    @Override
    public TimedCondition isOpenableInContext()
    {
        return parentPage().isAt();
    }

    /* ------------------------------------------------- COMPONENT -------------------------------------------------- */

    @Override
    public DDInput input()
    {
        return input;
    }

    @Override
    public DDDropDown dropDown()
    {
        return dropDown;
    }

    /* --------------------------------------------------- ACTIONS -------------------------------------------------- */

    @Override
    public DotDialog open()
    {
        if (and(isClosed(), isOpenable()).byDefaultTimeout())
        {
            return parentPage().openDotDialog();
        }
        else
        {
            throw new IllegalStateException("Already open");
        }
    }

    @Override
    public CloseMode close()
    {
        return closeMode;
    }

    private class CloseModeImpl implements CloseMode
    {

        @Override
        public void byEnter()
        {
            if (and(isOpen(),dropDown.isOpen()).byDefaultTimeout())
            {
                dropDown.close().byEnter();
            }
            else
            {
                throw new IllegalStateException("Cannot close by enter - either this dialog, or the suggestions dropdown is closed");
            }
        }

        @Override
        public void byEscape()
        {
            if (isOpen().byDefaultTimeout())
            {
                if (dropDown.isOpen().now())
                {
                    dropDown.close().byEscape();
                }
                locator().element().type(keys(SpecialKeys.ESC));
            }
            else
            {
                throw new IllegalStateException("Not open");
            }
        }

        @Override
        public void byClickIn(AjsDropdown.Item position)
        {
            position.locator().element().click();
        }
    }

}
