package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Collapsible;
import com.atlassian.jira.webtest.framework.core.Localizable;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface PluginComponent<T extends PluginComponent<T>> extends Collapsible<T>, Localizable
{
    public String getPluginKey();
}
