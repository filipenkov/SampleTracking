package com.atlassian.jira.web.action.greenhopper;

import com.atlassian.extras.common.LicenseException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.plugin.license.PluginLicenseManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;

import java.util.Arrays;

public class SetupGreenHopper extends JiraWebActionSupport
{
    private static final String COM_PYXIS_GREENHOPPER_JIRA = "com.pyxis.greenhopper.jira";
    private static final String GREENHOPPER_LICENSE_MANAGER = "greenhopper-license-manager";
    private final ExternalLinkUtil externalLinkUtil;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final JiraLicenseService licenseService;
    private String license;


    public SetupGreenHopper(ExternalLinkUtil externalLinkUtil, PluginAccessor pluginAccessor, JiraLicenseService licenseService, PluginController pluginController)
    {
        this.externalLinkUtil = externalLinkUtil;
        this.pluginAccessor = pluginAccessor;
        this.licenseService = licenseService;
        this.pluginController = pluginController;
    }

    public String doFetchLicense() throws Exception
    {
        return INPUT;
    }

    public String doReturnFromMAC() throws Exception
    {
        // Just repopulate and reshow all the fields
        return INPUT;
    }


    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected void doValidation()
    {
        super.doValidation();
    }

    // Do not make this Constructor Injected! That is WRONG. Because we reset the PICO world in the middle of the import
    // we don't want to reference things from the old PICO. So we need to dynamically get it everytime to ensure we
    // always get it from the correct PICO.
    private JiraLicenseService getLicenseService()
    {
        return ComponentManager.getComponent(JiraLicenseService.class);
    }

    protected String doExecute() throws Exception
    {
        pluginController.enablePlugins(COM_PYXIS_GREENHOPPER_JIRA);

        Plugin plugin = pluginAccessor.getPlugin(COM_PYXIS_GREENHOPPER_JIRA);
        PluginLicenseManager pluginLicenseManager = (PluginLicenseManager) plugin.getModuleDescriptor(GREENHOPPER_LICENSE_MANAGER).getModule();
        try
        {
            pluginLicenseManager.setLicense(license);
        }
        catch (LicenseException e)
        {
            String m = getText(e.getMessage());
            addError("license", m);
            pluginController.disablePlugin(COM_PYXIS_GREENHOPPER_JIRA);
            return ERROR;
        }

        return getRedirect("/secure/MyJiraHome.jspa");
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(final String license)
    {
        this.license = license;
    }

    public String getRequestLicenseURL()
    {
        StringBuilder url = new StringBuilder();
        url.append(JiraUrl.constructBaseUrl(request));
        url.append("/secure/SetupGreenHopper!returnFromMAC.jspa");
        String version = pluginAccessor.getPlugin(COM_PYXIS_GREENHOPPER_JIRA).getPluginInformation().getVersion();
        String organisation = licenseService.getLicense().getOrganisation();

        return externalLinkUtil.getProperty("external.link.greenhopper.license.view", Arrays.<String>asList(version, "GreenHopper for JIRA 4: Evaluation", getServerId(), organisation, url.toString()));
    }
}
