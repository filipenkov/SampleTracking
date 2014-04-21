package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.sal.api.scheduling.PluginJob;
import org.apache.log4j.Logger;

import java.util.Map;

public final class PlanStatusUpdateJob implements PluginJob
{
    private static final Logger log = Logger.getLogger(PlanStatusUpdateJob.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods

    public void execute(final Map<String, Object> jobDataMap)
    {
        PlanStatusUpdateService service = (PlanStatusUpdateService)jobDataMap.get(PlanStatusUpdateService.INSTANCE_KEY);
        service.scheduleUpdates();
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
