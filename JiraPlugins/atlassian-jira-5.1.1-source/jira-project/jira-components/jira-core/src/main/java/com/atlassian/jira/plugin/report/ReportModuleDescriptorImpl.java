/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.report;

import com.atlassian.jira.plugin.AbstractConfigurableModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * The report plugin allows end users to write pluggable reports for JIRA.
 *
 * @see com.atlassian.jira.plugin.report.Report
 */
//@RequiresRestart
public class ReportModuleDescriptorImpl extends AbstractConfigurableModuleDescriptor<Report> implements ReportModuleDescriptor
{
    // only used if this report is a singleton!
    private Report report;
    private String label = "Unknown";
    private String labelKey;

    public ReportModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    protected boolean isSingletonByDefault()
    {
        return false;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final Element labelEl = element.element("label");
        if (labelEl != null)
        {
            if (labelEl.attribute("key") != null)
                labelKey = labelEl.attributeValue("key");
            else
                label = labelEl.getTextTrim();
        }
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(Report.class);
    }

    public Report getModule()
    {
        if (!isSingleton())
        {
            return makeModule();
        }
        else
        {
            if (report == null)
            {
                report = makeModule();
            }

            return report;
        }
    }

    private Report makeModule()
    {
        final Plugin plugin = getPlugin();
        Report reportModule;
        if (plugin instanceof AutowireCapablePlugin)
        {
            reportModule = ((AutowireCapablePlugin) plugin).autowire(getModuleClass());
        }
        else
        {
            reportModule = JiraUtils.loadComponent(getModuleClass());
        }
        reportModule.init(this);
        return reportModule;
    }

    public String getLabel()
    {
        if (labelKey != null)
            return getI18nBean().getText(labelKey);

        return label;
    }

    public String getLabelKey()
    {
        return labelKey;
    }
}
