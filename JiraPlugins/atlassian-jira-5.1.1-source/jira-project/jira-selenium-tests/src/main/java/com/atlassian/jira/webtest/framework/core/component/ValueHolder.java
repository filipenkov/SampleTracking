package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * A component that holds a single string value in it, e.g. HTML input.
 *
 * @since v4.3
 */
public interface ValueHolder
{
    /**
     * Get this component's current value
     *
     * @return query for the current value of this component 
     */
    TimedQuery<String> value();
}
