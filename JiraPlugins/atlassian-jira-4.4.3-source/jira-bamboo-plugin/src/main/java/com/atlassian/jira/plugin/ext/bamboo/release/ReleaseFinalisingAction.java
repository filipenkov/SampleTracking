package com.atlassian.jira.plugin.ext.bamboo.release;

import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.PlanStatusUpdateService;
import com.atlassian.jira.plugin.ext.bamboo.service.ReleaseErrorReportingService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import org.apache.log4j.Logger;

import java.util.Map;


public class ReleaseFinalisingAction implements PlanStatusUpdateService.FinalizingAction
{
    private static final Logger log = Logger.getLogger(ReleaseFinalisingAction.class);

    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final long versionId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final VersionManager versionManager;
    private final BambooReleaseService bambooReleaseService;
    private final ReleaseErrorReportingService releaseErrorReportingService;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public ReleaseFinalisingAction(final long versionId, final VersionManager versionManager, final BambooReleaseService bambooReleaseService, final ReleaseErrorReportingService releaseErrorReportingService)
    {
        this.versionId = versionId;
        this.versionManager = versionManager;
        this.bambooReleaseService = bambooReleaseService;
        this.releaseErrorReportingService = releaseErrorReportingService;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    public void execute(PlanStatus planStatus)
    {
        final Version version = versionManager.getVersion(versionId);
        final Project project = version.getProjectObject();

        final Map<String, String> buildData = bambooReleaseService.getBuildData(project.getKey(), version.getId());
        if (buildData == null || buildData.get(PluginConstants.PS_BUILD_RESULT) == null)
        {
            registerError(version, "Release build " + planStatus.getPlanResultKey() + " completed but no record of triggering the release can be found.  Version was not released.");
        }
        else if (!buildData.get(PluginConstants.PS_BUILD_RESULT).equals(planStatus.getPlanResultKey().getKey()))
        {
            registerError(version, "Release build " + planStatus.getPlanResultKey() + " completed but it does not match the Plan Result we were waiting for (" + buildData.get(PluginConstants.PS_BUILD_RESULT) + ").  Version was not released.");
        }
        else
        {
            bambooReleaseService.releaseIfRequired(planStatus, version);
        }
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    private void registerError(Version version, String errorMessage)
    {
        final Project project = version.getProjectObject();
        releaseErrorReportingService.recordError(project.getKey(), version.getId(), errorMessage);
        log.error(errorMessage);
    }

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
