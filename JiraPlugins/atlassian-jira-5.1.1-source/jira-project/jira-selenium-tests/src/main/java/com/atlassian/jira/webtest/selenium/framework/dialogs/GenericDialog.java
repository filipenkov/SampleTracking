package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Represents a generic dialog that can be used for common assertions such as
 * if the dialog is open etc.
 *
 * @since v4.2
 */
public class GenericDialog extends AbstractIssueDialog<GenericDialog>
{
    private static LegacyIssueOperation FAKE = new LegacyIssueOperation("#notexisting", "Fake");

    public GenericDialog(LegacyIssueOperation op, ActionType at, SeleniumContext ctx)
    {
        super(op, GenericDialog.class, at, ctx);
    }

    public GenericDialog(SeleniumContext ctx, ActionType at)
    {
        this(FAKE, at, ctx);
    }

    public GenericDialog(SeleniumContext ctx)
    {
        this(FAKE, ActionType.JAVASCRIPT, ctx);
    }
}