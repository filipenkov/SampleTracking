package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Abstract Selenium {@link com.atlassian.jira.webtest.framework.dialog.Dialog} implementation, based on assumption that
 * there is a single unique page element that unambiguously distinguishes an open dialog. 
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumDialog<T extends Dialog<T>> extends AbstractLocatorBasedPageObject implements Dialog<T>
{
    /**
     * Default constructor.
     *
     * @param context current Selenium test context
     */
    protected AbstractSeleniumDialog(SeleniumContext context)
    {
        super(context);
    }

    /**
     * Unique locator of an open dialog.
     *
     * @return open dialog locator
     */
    protected abstract SeleniumLocator openDialogLocator();

    /**
     * {@inheritDoc}
     *
     * As a 'ready' dialog we assume open dialog with loaded contents.
     *
     * @return {@inheritDoc}
     */
    @Override
    protected final SeleniumLocator detector()
    {
        return openDialogLocator();
    }

    @Override
    public Locator locator()
    {
        return openDialogLocator();
    }

    @Override
    public TimedCondition isOpen()
    {
        return openDialogLocator().element().isPresent();
    }

    @Override
    public TimedCondition isClosed()
    {
        return Conditions.not(isOpen());
    }

    @Override
    public final TimedCondition isOpenable()
    {
        return and(isClosed(), isOpenableInContext());
    }

    /**
     * Timed condition checking if this dialog is openable in the current test context
     *
     * @return timed condition 'is openable in the current context?'
     */
    protected abstract TimedCondition isOpenableInContext();
}
