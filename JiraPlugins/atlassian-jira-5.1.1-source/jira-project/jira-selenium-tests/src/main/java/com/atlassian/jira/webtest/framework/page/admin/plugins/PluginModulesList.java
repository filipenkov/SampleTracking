package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface PluginModulesList<T extends PluginModuleComponent<T>> extends PageObject, Localizable
{
    public TimedCondition hasPluginModuleComponent(String moduleKey);
    public TimedQuery<T> findPluginModuleComponent(String moduleKey);
}