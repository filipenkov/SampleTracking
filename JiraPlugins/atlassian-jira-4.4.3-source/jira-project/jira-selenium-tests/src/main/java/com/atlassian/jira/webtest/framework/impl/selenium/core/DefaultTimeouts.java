package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.selenium.SeleniumConfiguration;

import java.util.EnumSet;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * Encapsulates JIRA Selenium timeouts.
 *
 * <p>
 * <b>NOTE</b>: do <b>NOT</b> modify those timeouts to account for single/intermittent test failures. Use
 * {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition#by(long)} to adjust custom timeouts
 * to problematic tests. The timeout values contained in this class should be generally considered immutable.
 *
 * @since v4.2
 */
public final class DefaultTimeouts
{
    private final SeleniumConfiguration config;
    // TODO may want to extract those into a properties file
    private final Map<Timeouts, Long> timeoutMappings = MapBuilder.<Timeouts,Long>newBuilder()
            .add(Timeouts.PAGE_LOAD, 20000L)
            .add(Timeouts.DIALOG_LOAD, 5000L)
            .add(Timeouts.COMPONENT_LOAD, 1000L)
            .add(Timeouts.AJAX_ACTION, 3000L)
            .toMap();
    
    public DefaultTimeouts(SeleniumConfiguration config)
    {
        this.config = notNull("config", config);
        in_sanityTest();
    }

    private void in_sanityTest()
    {
        for (Timeouts timeoutType : Timeouts.values())
        {
            if (!inConfiguration(timeoutType) && !inMappings(timeoutType))
            {
                throw new IllegalStateException("Missing timeout value for " + timeoutType);
            }
        }
    }

    private boolean inConfiguration(final Timeouts timeoutType)
    {
        return EnumSet.of(Timeouts.EVALUATION_INTERVAL, Timeouts.SLOW_PAGE_LOAD, Timeouts.UI_ACTION).contains(timeoutType);
    }

    private boolean inMappings(Timeouts timeoutType)
    {
        return timeoutMappings.containsKey(timeoutType);
    }

    public long timeoutFor(Timeouts timeoutType)
    {
        if (timeoutType == Timeouts.EVALUATION_INTERVAL)
        {
            return config.getConditionCheckInterval();
        }
        else if (timeoutType == Timeouts.SLOW_PAGE_LOAD)
        {
            return config.getPageLoadWait();
        }
        else if (timeoutType == Timeouts.UI_ACTION)
        {
            return config.getActionWait();
        }
        else
        {
            Long timeout = timeoutMappings.get(timeoutType);
            return notNull("timeout", timeout);
        }
    }

    /**
     * Default timeout for dialog load.
     *
     * @return default timeout
     * @deprecated use {@link #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)} instead
     *
     * @see #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see SeleniumContext#timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#DIALOG_LOAD
     */
    @Deprecated
    public long dialogLoad()
    {
        return timeoutFor(Timeouts.DIALOG_LOAD);
    }

    /**
     * Default timeout for JS/AJAX-based components.
     *
     * @return default timeout
     * @deprecated use {@link #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)} instead
     *
     * @see #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see SeleniumContext#timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#COMPONENT_LOAD
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#AJAX_ACTION
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#UI_ACTION
     */
    @Deprecated
    public long components()
    {
        return 500;
    }

    /**
     * For AJAX calls
     *
     * @return timeout for AJAX calls
     * @deprecated use {@link #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)} instead
     *
     * @see #timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see SeleniumContext#timeoutFor(com.atlassian.jira.webtest.framework.core.Timeouts)
     * @see Timeouts#AJAX_ACTION
     */
    @Deprecated
    public long ajax()
    {
        return 3000;
    }
}
