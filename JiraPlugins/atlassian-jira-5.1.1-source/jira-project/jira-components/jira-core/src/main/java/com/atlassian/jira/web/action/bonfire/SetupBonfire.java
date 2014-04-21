package com.atlassian.jira.web.action.bonfire;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SetupBonfire extends JiraWebActionSupport
{
    private static final String COM_ATLASSIAN_BONFIRE_PLUGIN = "com.atlassian.bonfire.plugin";

    private static final String BONFIRE_LICENSE_KEY = "bonfire-license";
    private static final String KEY_EX_PROPS = "Excalibur.properties";
    private static final long GLOBAL_ENTITY_ID = 1l;

    private final ExternalLinkUtil externalLinkUtil;
    private final PluginAccessor pluginAccessor;
    private final JiraLicenseService licenseService;
    private final PluginController pluginController;
    private String license;


    public SetupBonfire(ExternalLinkUtil externalLinkUtil, PluginAccessor pluginAccessor, JiraLicenseService licenseService, PluginController pluginController)
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
        ErrorCollection errorCollection = invokeBonfireSetLicence();
        if (errorCollection.hasAnyErrors())
        {
            addErrorCollection(errorCollection);
            return ERROR;
        }
        return getRedirect("/secure/MyJiraHome.jspa");
    }

    private ErrorCollection invokeBonfireSetLicence() throws Exception
    {
        //
        // Bonfire doesnt implement the PluginLicenseManager because this is @since 4.4 and Bonfire needs to run on 4.3.3
        // So we call directly onto its licence service with a custom written method just for JIRA (since 1.6.0 onwards)
        //
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        Plugin plugin = pluginAccessor.getPlugin(COM_ATLASSIAN_BONFIRE_PLUGIN);
        if (plugin == null)
        {
            errorCollection.addErrorMessage("setup.bonfire.no.plugin");
        }
        else
        {
            // this doesnt throw exceptions if its not present
            pluginController.enablePlugins(COM_ATLASSIAN_BONFIRE_PLUGIN);

            ModuleDescriptor<?> moduleDescriptor = plugin.getModuleDescriptor("bonfire-license-service");
            if (moduleDescriptor == null)
            {
                errorCollection.addErrorMessage(getText("setup.bonfire.no.licence.module"));
            }
            else
            {
                Object bonfireLicenceService = moduleDescriptor.getModule();
                Method validateAndSetLicence = bonfireLicenceService.getClass().getMethod("validateAndSetLicence", ErrorCollection.class, String.class);

                validateAndSetLicence.invoke(bonfireLicenceService, errorCollection, getLicense());
            }
        }
        return errorCollection;
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
        url.append("/secure/SetupBonfire!returnFromMAC.jspa");
        String version = pluginAccessor.getPlugin(COM_ATLASSIAN_BONFIRE_PLUGIN).getPluginInformation().getVersion();
        String organisation = licenseService.getLicense().getOrganisation();

        return externalLinkUtil.getProperty("external.link.bonfire.license.view", Arrays.<String>asList(version, "Atlassian Bonfire : Evaluation", getServerId(), organisation, url.toString()));
    }
}
