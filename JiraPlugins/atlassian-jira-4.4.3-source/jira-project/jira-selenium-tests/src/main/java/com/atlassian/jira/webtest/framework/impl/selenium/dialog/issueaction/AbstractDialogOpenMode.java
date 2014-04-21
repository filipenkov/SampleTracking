package com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;
import com.atlassian.webtest.ui.keys.Sequences;
import com.atlassian.webtest.ui.keys.TypeMode;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;


/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.IssueActionsParent.DialogOpenMode}.
 *
 * @since v4.3
 */
public abstract class AbstractDialogOpenMode<D extends IssueActionDialog<D>> extends AbstractSeleniumPageObject
        implements IssueActionsParent.DialogOpenMode<D>
{

    protected final D dialog;
    protected final IssueOperation action;
    private final DotDialog dotDialog;

    protected AbstractDialogOpenMode(D dialog, DotDialog dotDialog, SeleniumContext context)
    {
        super(context);
        this.dialog = notNull("dialog", dialog);
        this.action = notNull("action", dialog.action());
        this.dotDialog = notNull("dotDialog", dotDialog);
    }

    @Override
    public D byShortcut()
    {
        if (!action.hasShortcut())
        {
            throw new IllegalStateException(asString("No shortcut for dialog <",dialog,"> issue operation <",
                    action,">"));
        }
        body().element().type(action.shortcut());
        return dialog;
    }

    @Override
    public D byDotDialog()
    {
        dotDialog.open();
        assertThat(dotDialog.isOpen(), byDefaultTimeout());
        dotDialog.input().type(Sequences.charsBuilder(action.uiName()).typeMode(TypeMode.INSERT_WITH_EVENT).build())
                .parent().close().byEnter();
        return dialog;
    }

    @Override
    public TimedCondition isReady()
    {
        throw new UnsupportedOperationException("Yes the design is bad");
    }
}
