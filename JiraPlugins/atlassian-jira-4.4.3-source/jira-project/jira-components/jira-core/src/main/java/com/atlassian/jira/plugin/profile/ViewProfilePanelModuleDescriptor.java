package com.atlassian.jira.plugin.profile;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * Defines a plugin point for rendering content on the JIRA view profile page.
 *
 * @since v3.12
 */
public class ViewProfilePanelModuleDescriptor extends JiraResourcedModuleDescriptor<ViewProfilePanel> implements OrderableModuleDescriptor
{
    public static final String TAB_KEY = "tabKey";
    public static final String DEFAULT_TAB_KEY = "admin.common.words.default";

    private int order;
    private String tabKey;

    public ViewProfilePanelModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        order = ModuleDescriptorXMLUtils.getOrder(element);
        String tabKey = element.attributeValue(TAB_KEY);
        this.tabKey = (tabKey == null) ? DEFAULT_TAB_KEY : tabKey;
    }

    public int getOrder()
    {
        return order;
    }

    /**
     * Returns the "tab" key with which this panel is associated. This should be the
     * i18n property key that can be translated into the displayable tab name.
     *
     * @return the i18n key that uniquely identifies he tab and can be used to get the display name.
     */
    public String getTabKey()
    {
        return tabKey;
    }
}
