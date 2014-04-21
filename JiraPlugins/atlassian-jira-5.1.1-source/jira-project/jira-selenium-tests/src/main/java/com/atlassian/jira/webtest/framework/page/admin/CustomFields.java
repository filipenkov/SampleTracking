package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface CustomFields extends AdminPage
{
    TimedCondition canAddCustomFields();

    // TODO move to 'Add custom field' Flow
    CustomFields openAddCustomFields();

    TimedCondition cascadingSelectAvailable();
}
