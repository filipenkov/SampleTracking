package com.atlassian.jira.webtest.framework.impl.selenium.page.issue;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumInput;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.SeleniumIssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.page.issue.ConvertToSubTask2;
import com.atlassian.jira.webtest.framework.page.issue.ConvertToSubTaskSelectTypes;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issue.ConvertToSubTaskSelectTypes}.
 *
 * @since v4.3
 */
public class SeleniumConvertToSubtaskSelectTypes<P extends ParentPage> extends AbstractSeleniumConvertToSubtask<P, ConvertToSubTask2>
        implements ConvertToSubTaskSelectTypes<P>
{

    private final SeleniumLocator parentIssueContainer;
    private final SeleniumLocator parentIssueInputLocator;
    private final SeleniumLocator openIssuePopupLocator;

    private final IssuePickerPopup parentIssuePicker;
    private final SeleniumInput parentIssueInput;

    public SeleniumConvertToSubtaskSelectTypes(SeleniumContext ctx, P flowParent)
    {
        super(ctx, flowParent, 1);
        this.parentIssueContainer = id("parentIssueKey_container");
        this.parentIssueInputLocator = id("parentIssueKey");
        this.openIssuePopupLocator = parentIssueContainer.combine(css("a.popup-trigger"));
        this.parentIssuePicker = new SeleniumIssuePickerPopup(this, openIssuePopupLocator, context);
        this.parentIssueInput = new SeleniumInput(parentIssueInputLocator, context);

    }

    @Override
    protected Class<ConvertToSubTask2> nextStepType()
    {
        return ConvertToSubTask2.class;
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
}
