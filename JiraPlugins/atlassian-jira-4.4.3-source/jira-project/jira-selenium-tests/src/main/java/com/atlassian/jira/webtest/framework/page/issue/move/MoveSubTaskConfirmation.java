package com.atlassian.jira.webtest.framework.page.issue.move;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.FlowLastPage;

/**
 * Fourth page in the Move Sub-task flow (started when 'Move' operation is invoked on an issue). It is a confirmation page
 * that in case of parent change contains an issue picker to select the parent issue.
 *
 *
 * @since v4.3
 */
public interface MoveSubTaskConfirmation<P extends ParentPage> extends FlowLastPage<P, P>, IssueAware
{

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    /**
     * Issue picker popup of this page.
     *
     * @return issue picker popup instance
     */
    IssuePickerPopup parentIssuePicker();

    /**
     * Input for editing the parent issue value. This may be edited manually, or via the issue picker.
     *
     * @return parent issue input
     * @see #parentIssuePicker()
     */
    Input parentIssueInput();


    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    /**
     * Open and get the issue picker popup of this page.
     *
     * @return opened issue picker popup instance
     */
    IssuePickerPopup openParentIssuePicker();
}