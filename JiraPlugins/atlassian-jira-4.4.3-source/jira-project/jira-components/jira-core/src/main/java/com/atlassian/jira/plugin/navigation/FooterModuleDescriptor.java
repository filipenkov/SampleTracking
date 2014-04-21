package com.atlassian.jira.plugin.navigation;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorXMLUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.profiling.UtilTimerStack;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Module descriptor for footer modules.
 *
 * @since v3.12
 */
public class FooterModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableFooter> implements OrderableModuleDescriptor
{
    private static final String VIEW_TEMPLATE = "view";

    private final JiraLicenseService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;

    private int order;

    public FooterModuleDescriptor(JiraAuthenticationContext authenticationContext, final JiraLicenseService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        order = ModuleDescriptorXMLUtils.getOrder(element);
    }

    public void enabled()
    {
        super.enabled();
    }

    public int getOrder()
    {
        return order;
    }

    /**
     * This method will setup the params related to the license information and render the html for the footer.
     *
     * @param request the servlet request
     * @param startingParams any parameters that you want to have available in the context when rendering the footer.
     * @return html representing the footer.
     */
    public String getFooterHtml(HttpServletRequest request, Map startingParams)
    {
        Map params = createVelocityParams(request, startingParams);
        return getHtml(VIEW_TEMPLATE, params);
    }

    protected Map createVelocityParams(HttpServletRequest request, Map startingParams)
    {
        Map<String, Object> params = (startingParams != null) ? new HashMap(startingParams) : new HashMap();


        String licenseMessageClass = null;
        boolean longFooterMessage = true;

        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
        if (!licenseDetails.isLicenseSet() || licenseDetails.isEvaluation() || !licenseDetails.isCommercial() || licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone())
        {
            params.put("notfull", Boolean.TRUE);

            if (!licenseDetails.isLicenseSet()) // unlicensed
            {
                params.put("unlicensed", Boolean.TRUE);
                licenseMessageClass = "licensemessagered";
            }
            else if (licenseDetails.isEvaluation())
            {
                longFooterMessage = false;
                params.put("evaluation", Boolean.TRUE);
                licenseMessageClass = "licensemessagered";
            }
            else if (licenseDetails.hasLicenseTooOldForBuildConfirmationBeenDone())
            {
                params.put("confirmedWithOldLicense", Boolean.TRUE);
                licenseMessageClass = "licensemessagered";
            }
            else if (licenseDetails.isCommunity())
            {
                longFooterMessage = false;
                params.put("community", Boolean.TRUE);
                licenseMessageClass = "licensemessage";
            }
            else if (licenseDetails.isOpenSource())
            {
                longFooterMessage = false;
                params.put("opensource", Boolean.TRUE);
                licenseMessageClass = "licensemessage";
            }
            else if (licenseDetails.isNonProfit())
            {
                longFooterMessage = false;
                params.put("nonprofit", Boolean.TRUE);
                licenseMessageClass = "licensemessage";
            }
            else if (licenseDetails.isDemonstration())
            {
                params.put("demonstration", Boolean.TRUE);
                licenseMessageClass = "licensemessage";
            }
            else if (licenseDetails.isDeveloper())
            {
                params.put("developer", Boolean.TRUE);
                licenseMessageClass = "licensemessage";
            }
            else if (licenseDetails.isPersonalLicense())
            {
                licenseMessageClass = "licensemessage";
                params.put("personal", licenseDetails.isPersonalLicense());
            }
        }

        if (licenseMessageClass != null)
        {
            params.put("licenseMessageClass", licenseMessageClass);
        }
        params.put("organisation", licenseDetails.getOrganisation());
        params.put("buildInformation", buildUtilsInfo.getBuildInformation());
        params.put("longFooterMessage", longFooterMessage);
        params.put("serverid", jiraLicenseService.getServerId());
        params.put("externalLinkUtil", ExternalLinkUtilImpl.getInstance());
        params.put("utilTimerStack", new UtilTimerStack());
        params.put("version", buildUtilsInfo.getVersion());
        params.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        params.put("req", request);

        return params;
    }
}
