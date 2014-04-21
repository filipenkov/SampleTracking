/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;

/**
 * An issue tab panel plugin adds extra panel tabs to JIRA's View Issue page.
 */
public class IssueTabPanelModuleDescriptorImpl extends AbstractJiraModuleDescriptor<IssueTabPanel> implements IssueTabPanelModuleDescriptor
{
    private static final Logger log = Logger.getLogger(IssueTabPanelModuleDescriptorImpl.class);

    String label;
    private String labelKey;
    boolean isDefault = false;
    private int order;
    private boolean isSortable = false;
    private boolean supportsAjaxLoad;

    public IssueTabPanelModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
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
        supportsAjaxLoad = getBooleanElement(element, "supports-ajax-load");
    }

    @Override
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

    @Override
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
     * @return A List of {@link com.atlassian.jira.plugin.issuetabpanel.IssueAction}s.
     */
    @Override
    public List<IssueAction> getActions(Issue issue, User remoteUser)
    {
        IssueTabPanel tabPanel = getTabPanel();

        try
        {
            if (tabPanel.showPanel(issue, remoteUser))
                return getTabPanel().getActions(issue, remoteUser);
            else
                return Collections.emptyList();
        }
        catch (AbstractMethodError ex)
        {
            // The showPanel() and getActions() method signatures will change because of Embedded Crowd.
            // If you run an old plugin it will throw a java.lang.AbstractMethodError
            log.error("AbstractMethodError detected for IssueTabPanel '" + this.getName() + "'. This likely means the plugin is not compatible with this version of JIRA.");

            // Show an error message
            IssueAction action = new GenericMessageAction(getI18nBean().getText("viewissue.pluginerror"));
            return Collections.singletonList(action);
        }
        catch (RuntimeException ex)
        {
            log.error("Error occured in IssueTabPanel '" + this.getName() + "'.", ex);

            // Show an error message
            IssueAction action = new GenericMessageAction(getI18nBean().getText("viewissue.pluginerror"));
            return Collections.singletonList(action);
        }
    }

    @Override
    public int getOrder()
    {
        return order;
    }

    @Override
    public boolean isDefault()
    {
        return isDefault;
    }

    @Override
    public boolean isSortable()
    {
        return isSortable;
    }

    @Override
    public boolean isSupportsAjaxLoad()
    {
        return supportsAjaxLoad;
    }
}
