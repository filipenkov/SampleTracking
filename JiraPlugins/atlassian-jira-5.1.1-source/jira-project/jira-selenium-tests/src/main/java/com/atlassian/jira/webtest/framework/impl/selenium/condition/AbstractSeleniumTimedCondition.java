package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.AbstractTimedCondition;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumClient;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Abstract implementation of the {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition}
 * in the Selenium world.
 *
 * @since v4.2
 */
public abstract class AbstractSeleniumTimedCondition extends AbstractTimedCondition implements TimedCondition
{
    /**
     * A global default timeout for conditions is {@link Timeouts#UI_ACTION}, as most of the conditions
     * is run to evaluate results of such.
     *
     */
    public static final Timeouts DEFAULT_TIMEOUT = Timeouts.UI_ACTION;

    protected final SeleniumContext context;
    protected final SeleniumClient client;

    /**
     * Creates a new <tt>AbstractSeleniumTimedCondition</tt> with default timeout set to
     * {@link #DEFAULT_TIMEOUT}. 
     *
     * @param context current test context
     */
    protected AbstractSeleniumTimedCondition(SeleniumContext context)
    {
        super(notNull("context", context).timeoutFor(DEFAULT_TIMEOUT), context.timeoutFor(Timeouts.EVALUATION_INTERVAL));
        this.context = context;
        this.client = notNull("context.client", context.client());
    }

    /**
     * Creates a new <tt>AbstractSeleniumTimedCondition</tt> with <tt>defaultTimeout</tt> customized for given condition
     * instance.
     *
     * @param context current test context
     * @param defaultTimeout default timeout for this condition
     */
    protected AbstractSeleniumTimedCondition(SeleniumContext context, long defaultTimeout)
    {
        super(defaultTimeout, notNull("context", context).timeoutFor(Timeouts.EVALUATION_INTERVAL));
        this.context = context;
        this.client = notNull("context.client", context.client());
    }

    /**
     * Creates a new <tt>AbstractSeleniumTimedCondition</tt> with <tt>defaultTimeout</tt> customized for given condition
     * instance.
     *
     * @param context current test context
     * @param defaultTimeout default timeout for this condition
     */
    protected AbstractSeleniumTimedCondition(SeleniumContext context, Timeouts defaultTimeout)
    {
        this(context, notNull("context", context).timeoutFor(defaultTimeout));
    }

    protected AbstractSeleniumTimedCondition(AbstractSeleniumTimedConditionBuilder<?,? extends AbstractSeleniumTimedCondition> builder)
    {
        this(builder.context(), builder.defaultTimeout());
    }
    
}
