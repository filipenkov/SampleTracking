package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.admin.plugins.AuditLogTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.UpgradePlugins;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumAuditLogTab extends AbstractSeleniumPluginsTab<AuditLogTab> implements AuditLogTab
{
    protected SeleniumAuditLogTab(SeleniumContext context, Plugins plugins)
    {
        super(context, plugins, "upm-panel-log", "upm-tab-log");
    }
}