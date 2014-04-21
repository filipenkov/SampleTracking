/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.user.User;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;

/**
 * An issue tab panel plugin adds extra panel tabs to JIRA's View Issue page.
 */
public class IssueTabPanelModuleDescriptor extends JiraResourcedModuleDescriptor<IssueTabPanel> implements OrderableModuleDescriptor
{
    String label;
    private String labelKey;
    boolean isDefault = false;
    private int order;
    private boolean isSortable = false;

    public IssueTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        label = element.element("label").getTextTrim();
        if (element.element("label").attribute("key") != null)
        {
            labelKey = element.element("label").attribute("key").getText();
        }
        isDefault = getBooleanElement(element, "default");
        order = ModuleDescriptorXMLUtils.getOrder(element);
        isSortable = getBooleanElement(element, "sortable");
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(IssueTabPanel.class);
    }

    private static boolean getBooleanElement(Element parentElement, String elementName)
    {
        Element element = parentElement.element(elementName);
        if (element == null)
        {
            return false;
        }

        String isDefaultText = element.getTextTrim();
        return isDefaultText != null && Boolean.valueOf(isDefaultText);
    }

    private IssueTabPanel getTabPanel()
    {
        return getModule();
    }

    public String getLabel()
    {
        if (labelKey != null)
        {
            return getI18nBean().getText(labelKey);
        }
        return label;
    }

    /**
     * @param issue      Issue
     * @param remoteUser User
     * @return A List of {@link IssueAction}s.
     */
    public List getActions(Issue issue, User remoteUser)
    {
        IssueTabPanel tabPanel = getTabPanel();

        if (tabPanel.showPanel(issue, remoteUser))
            return getTabPanel().getActions(issue, remoteUser);
        else
            return Collections.EMPTY_LIST;
    }

    public int getOrder()
    {
        return order;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public boolean isSortable()
    {
        return isSortable;
    }
}
