package com.atlassian.jira.webtest.framework.impl.selenium.page.issue.move;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumInput;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.SeleniumIssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskConfirmation;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskConfirmation}.
 *
 * @since v4.3
 */
public class SeleniumMoveSubtaskConfirmation<P extends ParentPage> extends AbstractSeleniumMoveSubTask<P, P>
        implements MoveSubTaskConfirmation<P>
{

    private final SeleniumLocator parentIssueContainer;
    private final SeleniumLocator parentIssueInputLocator;
    private final SeleniumLocator openIssuePopupLocator;

    private final IssuePickerPopup parentIssuePicker;
    private final SeleniumInput parentIssueInput;

    public SeleniumMoveSubtaskConfirmation(SeleniumContext ctx, P flowParent)
    {
        super(ctx, flowParent, 4);
        this.parentIssueContainer = id("parentIssue_container");
        this.parentIssueInputLocator = id("parentIssue");
        this.openIssuePopupLocator = parentIssueContainer.combine(css("a.popup-trigger"));
        this.parentIssuePicker = new SeleniumIssuePickerPopup(this, openIssuePopupLocator, context);
        this.parentIssueInput = new SeleniumInput(parentIssueInputLocator, context);

    }

    @Override
    @SuppressWarnings ({ "unchecked" })
    protected Class<P> nextStepType()
    {
        return (Class) flowParent.getClass();
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    @Override
    public IssuePickerPopup parentIssuePicker()
    {
        return parentIssuePicker;
    }

    @Override
    public Input parentIssueInput()
    {
        return parentIssueInput;
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public IssuePickerPopup openParentIssuePicker()
    {
        return parentIssuePicker.open();
    }

    @Override
    public P finish()
    {
        return submit();
    }
}
