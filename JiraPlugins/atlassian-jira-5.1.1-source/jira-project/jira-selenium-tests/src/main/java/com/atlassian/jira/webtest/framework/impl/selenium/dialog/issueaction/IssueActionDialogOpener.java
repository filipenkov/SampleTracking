package com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction;

import com.atlassian.jira.webtest.framework.model.IssueOperation;

/**
 * You guessed it - it's for opening dialogs! 
 *
 * @since v4.3
 */
public interface IssueActionDialogOpener
{
    /**
     * You guessed it - it's for opening dialogs!
     *
     * @param operation issue operation of the dialog
     */
    void open(IssueOperation operation);
}
