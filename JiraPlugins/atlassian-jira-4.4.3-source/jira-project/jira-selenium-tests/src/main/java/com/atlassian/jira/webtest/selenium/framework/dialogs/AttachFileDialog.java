package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Opens and maintains the AttachFileDialog.
 *
 * @since v4.2
 */
public class AttachFileDialog extends AbstractIssueDialog<AttachFileDialog>
{

    public AttachFileDialog(SeleniumContext ctx)
    {
        super(LegacyIssueOperation.ATTACH_FILE, AttachFileDialog.class, ActionType.NEW_PAGE, ctx);
    }

    // TODO ;)
}
