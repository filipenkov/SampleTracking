package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static junit.framework.Assert.assertTrue;

/**
 * Abstract implementation of {@link SubmittableDialog} in terms of abstract locators of
 * the cancel and submit button.
 *
 * @since v4.2
 */
public abstract class AbstractSubmittableDialog<T extends AbstractSubmittableDialog<T>> extends AbstractDialog<T>
        implements SubmittableDialog
{
    private final ActionType afterSubmit;

    // TODO change this as the interface changes (openable/closeable)

    protected AbstractSubmittableDialog(Class<T> targetType, ActionType afterSubmit, SeleniumContext ctx)
    {
        super(ctx, targetType);
        
        this.afterSubmit = notNull("afterSubmit", afterSubmit);
    }

    public final T cancel(final CancelType cancelType)
    {
        cancelType.execute(client, cancelTriggerLocator());
        assertTrue("Closing of dialog not successful", isClosed());
        return asTargetType();
    }

    public final T closeByEscape()
    {
        context.ui().pressInBody(Keys.ESCAPE);
        assertTrue("Closing of dialog not successful", isClosed());
        return asTargetType();
    }

    public final SubmittableDialog submit(SubmitType st)
    {
        return submit(st, true);
    }

    public final SubmittableDialog submit(SubmitType st, boolean waitForAction)
    {
        st.execute(client, submitTriggerLocator());
        if (waitForAction)
        {
            afterSubmit.waitForAction(context);            
        }
        return asTargetType();
    }

    public final ActionType afterSubmit()
    {
        return afterSubmit;
    }

}
