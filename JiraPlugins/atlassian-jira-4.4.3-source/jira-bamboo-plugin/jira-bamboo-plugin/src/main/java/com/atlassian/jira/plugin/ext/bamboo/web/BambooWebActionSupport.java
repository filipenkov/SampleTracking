package com.atlassian.jira.plugin.ext.bamboo.web;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.SortTool;

public abstract class BambooWebActionSupport extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(BambooWebActionSupport.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String BAMBOO_PLUGIN_KEY = "com.atlassian.jira.plugin.ext.bamboo";

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final BambooApplicationLinkManager applinkManager;
    private final WebResourceManager webResourceManager;
    private final SortTool sorter = new SortTool();

    // ---------------------------------------------------------------------------------------------------- Constructors
    protected BambooWebActionSupport(BambooApplicationLinkManager applinkManager, WebResourceManager webResourceManager)
    {
        this.applinkManager = applinkManager;
        this.webResourceManager = webResourceManager;
    }
    
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean hasPermissions()
    {
        return isHasPermission(Permissions.ADMINISTER);
    }

    public boolean hasMultipleBambooApplinks()
    {
        return applinkManager.getApplicationLinkCount() > 1;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    @Override
    public String doDefault() throws Exception
    {
        return hasPermissions() ? INPUT : PERMISSION_VIOLATION_RESULT;
    }

    @Override
    public String execute() throws Exception
    {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }
        webResourceManager.requireResource(BambooWebActionSupport.BAMBOO_PLUGIN_KEY + ":" + "css");
        return super.execute();
    }

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public SortTool getSorter()
    {
        return sorter;
    }
    
    public BambooApplicationLinkManager getApplinkManager()
    {
        return applinkManager;
    }

    protected I18nHelper getI18nHelper()
    {
        return ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper();
    }

    @Override
    public String getText(String i18nKey)
    {
        return getI18nHelper().getText(i18nKey);
    }
}