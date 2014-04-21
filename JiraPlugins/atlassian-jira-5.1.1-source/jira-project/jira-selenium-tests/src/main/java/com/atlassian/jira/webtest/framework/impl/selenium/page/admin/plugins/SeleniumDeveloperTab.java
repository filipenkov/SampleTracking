package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.admin.plugins.DeveloperTab;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumDeveloperTab extends AbstractSeleniumPluginsTab<DeveloperTab> implements DeveloperTab
{
    protected SeleniumDeveloperTab(SeleniumContext context, Plugins plugins)
    {
        super(context, plugins, "upm-panel-dev", "upm-tab-dev");
    }
}