package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Displays a reports dropdown on the browse project page.
 *
 * @since v4.0
 */
public class ReportsMenuFragment implements MenuFragment
{
    private static final String TEMPLATE = "reportsmenu.vm";
    private final static String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/menu/";
    private static final String REPORTS_ID = "reports";

    private static final Logger log = Logger.getLogger(ReportsMenuFragment.class);

    private final VelocityManager velocityManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;

    public ReportsMenuFragment(final VelocityManager velocityManager,
                               final JiraAuthenticationContext jiraAuthenticationContext, final ApplicationProperties applicationProperties, PluginAccessor pluginAccessor)
    {
        this.velocityManager = velocityManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.applicationProperties = applicationProperties;
        this.pluginAccessor = pluginAccessor;
    }

    public String getId()
    {
        return REPORTS_ID;
    }

    public String getHtml(final BrowseContext ctx)
    {
        try
        {
            final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(jiraAuthenticationContext);
            velocityParams.put("project", ctx.getProject());
            velocityParams.put("i18n", jiraAuthenticationContext.getI18nHelper());
            velocityParams.put(REPORTS_ID, getReports());
            velocityParams.put("browseContext", ctx);

            return velocityManager.getEncodedBody(TEMPLATE_DIRECTORY_PATH, TEMPLATE, applicationProperties.getEncoding(), velocityParams);
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_DIRECTORY_PATH + TEMPLATE + "'.", e);
        }

        return "";
    }

    private List<ReportModuleDescriptor> getReports()
    {
        final List<ReportModuleDescriptor> reports = new ArrayList<ReportModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(ReportModuleDescriptor.class));

        for (Iterator iterator = reports.iterator(); iterator.hasNext();)
        {
            final ReportModuleDescriptor descriptor = (ReportModuleDescriptor) iterator.next();

            if (!((Report) descriptor.getModule()).showReport())
            {
                iterator.remove();
            }
        }

        return reports;
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        return !getReports().isEmpty();
    }
}
