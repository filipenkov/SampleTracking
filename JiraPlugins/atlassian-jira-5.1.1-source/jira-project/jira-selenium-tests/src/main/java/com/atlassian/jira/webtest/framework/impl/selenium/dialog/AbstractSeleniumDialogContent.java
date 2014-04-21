package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;
import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.dialog.DialogContent;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumDialogContent<C extends DialogContent<C, D>, D extends Dialog>
        extends AbstractLocatorBasedPageObject implements DialogContent<C, D>
{
    private final D dialog;

    public AbstractSeleniumDialogContent(SeleniumContext ctx, D dialog)
    {
        super(ctx);

        if (dialog == null) { throw new NullPointerException("dialog"); }
        this.dialog = dialog;
    }

    /**
     * Returns the dialog that contains this content.
     *
     * @return a Dialog
     */
    @Override
    public D dialog()
    {
        return dialog;
    }
}
