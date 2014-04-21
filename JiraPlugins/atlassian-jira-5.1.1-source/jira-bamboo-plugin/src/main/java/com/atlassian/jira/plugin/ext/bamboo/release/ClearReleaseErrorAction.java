package com.atlassian.jira.plugin.ext.bamboo.release;

import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.panel.BambooPanelHelper;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.ReleaseErrorReportingService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import org.apache.log4j.Logger;

public class ClearReleaseErrorAction extends ProjectActionSupport
{

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(ClearReleaseErrorAction.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private long versionId;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private VersionManager versionManager;
    private ReleaseErrorReportingService releaseErrorReportingService;
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods

    @Override
    protected String doExecute() throws Exception
    {
        // clears errors
        Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            addErrorMessage("Could not clear release errors for version, no version with the id '" + versionId + "' could be found");
            return ERROR;
        }

        Project projectObject = version.getProjectObject();

        releaseErrorReportingService.clearErrors(projectObject.getKey(), version.getId());
        return returnComplete(getBaseUrl(projectObject, version));
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Helper Methods
    private String getBaseUrl(Project project, Version version)
    {
        return "/browse/" + project.getKey() +
               "/fixforversion/" + version.getId() +
               "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + PluginConstants.BAMBOO_RELEASE_TABPANEL_MODULE_KEY;
    }

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public void setVersionId(long versionId)
    {
        this.versionId = versionId;
    }

    public void setVersionManager(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public void setReleaseErrorReportingService(ReleaseErrorReportingService releaseErrorReportingService)
    {
        this.releaseErrorReportingService = releaseErrorReportingService;
    }
}
