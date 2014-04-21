package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * An &lt;input type=checkbox/&gt;.
 *
 * @since v4.3
 */
public interface Checkbox extends PageObject, Localizable
{
    /**
     * Returns a boolean indicating whether this checkbox is checked.
     *
     * @return a boolean indicating whether this checkbox is checked
     */
    TimedCondition checked();

    /**
     * Sets the checked status of this checkbox.
     *
     * @return this
     */
    Checkbox toggle();
}
