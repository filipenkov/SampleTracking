package com.atlassian.labs.jira4compat;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.labs.jira4compat.api.CompatIssueTabPanel;
import com.atlassian.labs.jira4compat.impl.Jira4CompatIssueTabPanelFactory;
import com.atlassian.labs.jira4compat.spi.CompatIssueTabPanelFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class CompatIssueTabPanelModuleDescriptor extends AbstractCompatModuleDescriptor<CompatIssueTabPanel>
{
    private final ModuleFactory convertingModuleFactory;
    private final CompatIssueTabPanelFactory factory;

    public CompatIssueTabPanelModuleDescriptor(final ModuleFactory moduleFactory, BundleContext bundleContext,
                                               final CompatIssueTabPanelFactory factory)
    {
        super(moduleFactory, bundleContext);
        this.factory = factory;
        this.convertingModuleFactory = new ModuleFactory()
        {
            public <T> T createModule(String s, ModuleDescriptor<T> tModuleDescriptor) throws PluginParseException
            {
                CompatIssueTabPanel panel = (CompatIssueTabPanel) moduleFactory.createModule(tModuleDescriptor.getParams().get("super-secret-class"), tModuleDescriptor);
                return (T) factory.convert(panel);
            }
        };
    }

    @Override
    protected ModuleDescriptor createModuleDescriptor(Element originalElement)
    {
        // Store the class into a known location
        originalElement.addElement("param").addAttribute("name", "super-secret-class").addAttribute("value", originalElement.attribute("class").getValue());
        // And modify the class attribute to look where they expect
        originalElement.addAttribute("class", IssueTabPanel.class.getCanonicalName());

        return factory.createIssueTabPanelModuleDescriptor(ComponentAccessor.getJiraAuthenticationContext(), convertingModuleFactory);
    }
}
