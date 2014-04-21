package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Displays a reports dropdown on the browse project page.
 *
 * @since v4.0
 */
public class ReportsMenuFragment implements MenuFragment
{
    private static final String REPORTS_FRAGMENT_DIV_ID = "reports";
    private static final String TEMPLATE_PATH =
            "templates/plugins/jira/projectpanels/fragments/summary/menu/reportsmenu.vm";

    private static final Logger log = Logger.getLogger(ReportsMenuFragment.class);

    private final VelocityTemplatingEngine templatingEngine;
    private final JiraAuthenticationContext authenticationContext;
    private final PluginAccessor pluginAccessor;

    public ReportsMenuFragment(final VelocityTemplatingEngine templatingEngine, final JiraAuthenticationContext authenticationContext,
            final PluginAccessor pluginAccessor)
    {
        this.templatingEngine = templatingEngine;
        this.authenticationContext = authenticationContext;
        this.pluginAccessor = pluginAccessor;
    }

    public String getId()
    {
        return REPORTS_FRAGMENT_DIV_ID;
    }

    public String getHtml(final BrowseContext ctx)
    {
        try
        {
            final Map<String, Object> velocityParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
            velocityParams.put("project", ctx.getProject());
            velocityParams.put("i18n", authenticationContext.getI18nHelper());
            velocityParams.put(REPORTS_FRAGMENT_DIV_ID, getReports());
            velocityParams.put("browseContext", ctx);

            return templatingEngine.render(file(TEMPLATE_PATH)).applying(velocityParams).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template for '" + TEMPLATE_PATH + "'.", e);
        }

        return "";
    }

    private List<ReportModuleDescriptor> getReports()
    {
        final List<ReportModuleDescriptor> reports =
                pluginAccessor.getEnabledModuleDescriptorsByClass(ReportModuleDescriptor.class);

        for (Iterator<ReportModuleDescriptor> iterator = reports.iterator(); iterator.hasNext();)
        {
            final ReportModuleDescriptor descriptor = iterator.next();

            if (!descriptor.getModule().showReport())
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
