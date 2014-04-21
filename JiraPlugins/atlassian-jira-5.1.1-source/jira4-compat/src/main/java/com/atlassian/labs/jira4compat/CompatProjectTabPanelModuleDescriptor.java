package com.atlassian.labs.jira4compat;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.labs.jira4compat.api.CompatProjectTabPanel;
import com.atlassian.labs.jira4compat.spi.CompatProjectTabPanelFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class CompatProjectTabPanelModuleDescriptor extends AbstractCompatModuleDescriptor<CompatProjectTabPanel>
{
    private final ModuleFactory convertingModuleFactory;
    private final CompatProjectTabPanelFactory factory;

    public CompatProjectTabPanelModuleDescriptor(final ModuleFactory moduleFactory, BundleContext bundleContext,
                                                 final CompatProjectTabPanelFactory factory)
    {
        super(moduleFactory, bundleContext);
        this.factory = factory;
        this.convertingModuleFactory = new ModuleFactory()
        {
            public <T> T createModule(String s, ModuleDescriptor<T> tModuleDescriptor) throws PluginParseException
            {
                CompatProjectTabPanel panel = (CompatProjectTabPanel) moduleFactory.createModule(tModuleDescriptor.getParams().get("super-secret-class"), tModuleDescriptor);
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
        originalElement.addAttribute("class", ProjectTabPanel.class.getCanonicalName());

        return factory.createProjectTabPanelModuleDescriptor(ComponentAccessor.getJiraAuthenticationContext(), convertingModuleFactory);
    }

}
