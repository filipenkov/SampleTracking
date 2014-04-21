package com.atlassian.jira.webtest.framework.page.issue;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.FlowPage;

/**
 * Convert issue to sub-task page one. 
 *
 * @since v4.3
 */
public interface ConvertToSubTaskSelectTypes<P extends ParentPage> extends FlowPage<P, ConvertToSubTask2>, IssueAware
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
