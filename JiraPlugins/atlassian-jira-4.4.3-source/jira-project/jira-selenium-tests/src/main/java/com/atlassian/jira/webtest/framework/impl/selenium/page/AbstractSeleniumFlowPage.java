package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.FlowPage;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * <p>
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.FlowPage}. It represents a
 * flow page that navigates to the next step and cancels the flow through clickable links/buttons.
 *
 * <p>
 * The {@link com.atlassian.jira.webtest.framework.page.FlowPage#cancel()} and
 * {@link com.atlassian.jira.webtest.framework.page.FlowPage#next()} methods are defined as template methods using
 * hooks returning locators for the 'next' and 'cancel' clickable items.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumFlowPage<P extends ParentPage, N extends Page> extends AbstractSeleniumPage
        implements FlowPage<P,N>
{

    protected final P flowParent;
    protected final int stepNumber;

    protected AbstractSeleniumFlowPage(SeleniumContext ctx, P flowParent, int stepNo)
    {
        super(ctx);
        this.flowParent = notNull("flowParent", flowParent);
        this.stepNumber = greaterThan("stepNo", stepNo, 0);
    }


    @Override
    public final P cancel()
    {
        if (!isAt().now())
        {
            throw new IllegalStateException("Not on the page");
        }
        cancelLocator().element().click();
        waitFor().pageLoad();
        return flowParent;
    }

    @Override
    public final N submit()
    {
        if (!isAt().now())
        {
            throw new IllegalStateException("Not on the page");
        }
        nextLocator().element().click();
        waitFor().pageLoad();
        return submitTarget();
    }

    private N submitTarget()
    {
        return isParentNext() ? parentAsNext() : flowParent.getChild(nextStepType());
    }

    @SuppressWarnings ({ "unchecked" })
    private N parentAsNext()
    {
        return (N) flowParent;
    }

    private boolean isParentNext()
    {
        return flowParent.getClass().equals(nextStepType());
    }


    @Override
    public final N next()
    {
        return submit();
    }

    @Override
    public final int stepNumber()
    {
        return stepNumber;
    }

    /**
     * Locator of a clickable 'cancel' object on the page.
     *
     * @return cancel object locator
     */
    protected abstract Locator cancelLocator();

    /**
     * Locator of a clickable 'next' object on the page.
     *
     * @return next object locator
     */
    protected abstract Locator nextLocator();

    /**
     * Next page type
     *
     * @return next page type
     */
    protected abstract Class<N> nextStepType();
}
