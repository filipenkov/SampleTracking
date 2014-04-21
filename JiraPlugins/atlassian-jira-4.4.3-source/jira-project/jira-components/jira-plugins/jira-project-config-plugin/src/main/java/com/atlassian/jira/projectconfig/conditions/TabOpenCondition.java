package com.atlassian.jira.projectconfig.conditions;

import com.atlassian.jira.projectconfig.tab.WebPanelTab;
import com.atlassian.jira.web.sitemesh.AdminDecoratorHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Will cause web fragments to display given that a particular tab is open. The tab ID/name is expected to be stored in
 * the context.
 *
 * @since v4.4
 */
public class TabOpenCondition implements Condition
{
    private static final String TAB_LINK_ID_KEY = "tabLinkId";
    private static final String TAB_NAME_KEY = "tabName";

    private String tabId;
    private String tabName;

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        tabId = params.get(TAB_LINK_ID_KEY);
        tabName = params.get(TAB_NAME_KEY);
        checkState(tabId != null || tabName != null, "Either tabLinkId, or tabName init paremeter must be set");
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return (tabId != null && tabId.equals(context.get(AdminDecoratorHelper.ACTIVE_TAB_LINK_KEY)))
                || (tabName != null && tabName.equals(context.get(WebPanelTab.CURRENT_TAB_NAME)));
    }
}
