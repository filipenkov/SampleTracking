package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.jira.plugin.ext.bamboo.model.LifeCycleState;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.project.version.Version;
import org.jetbrains.annotations.NotNull;

/**
 * Async accessor for {@link BambooRestService#getPlanStatus(ApplicationLink, PlanResultKey)} designed for UI polling
 */
public interface PlanStatusUpdateService
{
    static final String INSTANCE_KEY = "INSTANCE";

    /**
     * Subscribe to status changes for the given project, version and planKey until the plan execution has stopped
     * @param version
     * @param planResultKey
     * @param username
     * @param finalizingAction
     * @throws IllegalStateException
     */
    void subscribe(@NotNull Version version, @NotNull PlanResultKey planResultKey, @NotNull String username, @NotNull FinalizingAction finalizingAction) throws IllegalStateException;

    /**
     * Unsubscribe from status changes for the given {@link PlanResultKey}
     * @param planResultKey
     */
    void unsubscribe(@NotNull PlanResultKey planResultKey);

    /**
     * Schedule subscriptions for updates
     * when they transition to {@link BuildState#FAILED} or {@link BuildState#SUCCESS}
     */
    void scheduleUpdates();

    public interface FinalizingAction
    {
        /**
         * @param planStatus when the remote Plan reaches a finalized {@link LifeCycleState}
         */
        void execute(PlanStatus planStatus);
    }
}
