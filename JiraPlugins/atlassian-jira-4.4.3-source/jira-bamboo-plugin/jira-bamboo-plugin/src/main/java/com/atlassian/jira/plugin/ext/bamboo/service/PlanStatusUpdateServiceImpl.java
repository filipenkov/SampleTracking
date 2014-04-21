package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.model.LifeCycleState;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PlanStatusUpdateServiceImpl implements PlanStatusUpdateService, LifecycleAware
{
    private static final Logger log = Logger.getLogger(PlanStatusUpdateServiceImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    private static final String JOBNAME = PlanStatusUpdateServiceImpl.class.getName() + ":job";

    private static final long DEFAULT_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    /**
     * System property for changing interval
     */
    private static final String BAMBOO_STATUS_UPDATE_INTERVAL = "bamboo.status.update.interval";

    // ------------------------------------------------------------------------------------------------- Type Properties

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 10, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), ThreadFactories.namedThreadFactory("Bamboo Status Update"));
    private final ConcurrentMap<PlanResultKey, Subscription> planResultKeyToSubscriptionMap = new MapMaker().makeMap();
    private final ConcurrentMap<Subscription, Future<?>> subscriptionFutureMap = new MapMaker().makeMap();

    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final PluginScheduler pluginScheduler;
    private final ProjectManager projectManager;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;
    private final BambooRestService bambooRestService;
    private final ReleaseErrorReportingService releaseErrorReportingService;
    private final ImpersonationService impersonationService;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public PlanStatusUpdateServiceImpl(final PluginScheduler pluginScheduler,
                                       final ProjectManager projectManager,
                                       final BambooApplicationLinkManager bambooApplicationLinkManager,
                                       final BambooRestService bambooRestService,
                                       final ReleaseErrorReportingService releaseErrorReportingService,
                                       final ImpersonationService impersonationService)
    {
        this.pluginScheduler = pluginScheduler;
        this.projectManager = projectManager;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
        this.bambooRestService = bambooRestService;
        this.releaseErrorReportingService = releaseErrorReportingService;
        this.impersonationService = impersonationService;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods

    public void subscribe(@NotNull final Version version, final @NotNull PlanResultKey planResultKey, final @NotNull String username, final @NotNull FinalizingAction finalizingAction)
    {
        final Project project = version.getProjectObject();
        log.info("Bamboo Release Plugin waiting for result for build '" + planResultKey + "' for project '" + project.getKey() + "' and version '" + version.getName() + "'");
        final Subscription subscription = new Subscription(planResultKey, project.getKey(), version.getId(), username, finalizingAction);
        this.planResultKeyToSubscriptionMap.putIfAbsent(planResultKey, subscription);
        scheduleUpdate(subscription);
    }

    public void unsubscribe(@NotNull final PlanResultKey planResultKey)
    {
        final Subscription subscription = planResultKeyToSubscriptionMap.remove(planResultKey);
        if (subscription != null)
        {
            removeFuture(subscription);
        }
    }

    public void scheduleUpdates()
    {
        for (final Subscription subscription : planResultKeyToSubscriptionMap.values())
        {
            if (!subscriptionFutureMap.containsKey(subscription))
            {
                scheduleUpdate(subscription);
            }
        }
    }

    public void onStart()
    {
        final long interval = getInterval();
        final Map<String, Object> map = Maps.newHashMap();
        map.put(INSTANCE_KEY, this);
        pluginScheduler.scheduleJob(JOBNAME, PlanStatusUpdateJob.class, map, new Date(), interval);
        log.info(String.format("Job '" + JOBNAME + "' scheduled to run every %dms", interval));
    }

    // -------------------------------------------------------------------------------------------------- Action Methods

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    private void scheduleUpdate(@NotNull Subscription subscription)
    {
        final ApplicationLink applicationLink = subscription.getApplicationLink();
        if (applicationLink == null)
        {
            applicationLinkUnavailable(subscription);
        }
        else
        {
            log.info("Scheduling update from '" + applicationLink + "' for Plan '" + subscription.planResultKey + "'");
            final Future<?> planStatusFuture = threadPoolExecutor.submit(subscription.planStatusRunnable);
            subscriptionFutureMap.putIfAbsent(subscription, planStatusFuture);
        }
    }

    /**
     * @return interval for the scheduling for the {@link PlanStatusUpdateJob}
     */
    private long getInterval()
    {
        return Long.getLong(BAMBOO_STATUS_UPDATE_INTERVAL, DEFAULT_INTERVAL);
    }

    /**
     * Report that the application link was unavailable
     * @param subscription
     */
    private void applicationLinkUnavailable(@NotNull Subscription subscription)
    {
        releaseErrorReportingService.recordError(subscription.projectKey, subscription.versionId, "Could not connect to Plan '" + subscription.planResultKey.getPlanKey() + "' for Project '" + subscription.projectKey + "'");
        unsubscribe(subscription.planResultKey);
    }

    private void removeFuture(final Subscription subscription)
    {
        subscriptionFutureMap.remove(subscription);
    }

    private final class Subscription
    {
        private final PlanResultKey planResultKey;
        private final String projectKey;
        private final long versionId;
        private final FinalizingAction finalizingAction;
        private final Runnable planStatusRunnable;

        private Subscription(@NotNull PlanResultKey planResultKey, @NotNull String projectKey, @NotNull long versionId, @NotNull String username, @NotNull FinalizingAction finalizingAction)
        {
            this.planResultKey = planResultKey;
            this.projectKey = projectKey;
            this.versionId = versionId;
            this.finalizingAction = finalizingAction;
            this.planStatusRunnable = impersonationService.runAsUser(username, new UpdatePlanStatus(this));
        }

        @Nullable
        public ApplicationLink getApplicationLink()
        {
            final Project project = projectManager.getProjectObjByKey(projectKey);
            if (project != null)
            {
                return bambooApplicationLinkManager.getApplicationLink(project.getKey());
            }
            log.error("Could not find project '" + projectKey + "'");
            return null;
        }
    }

    private final class UpdatePlanStatus implements Runnable
    {
        private final Subscription subscription;

        private UpdatePlanStatus(@NotNull Subscription subscription)
        {
            this.subscription = subscription;
        }

        public void run()
        {
            final ApplicationLink applicationLink = subscription.getApplicationLink();
            if (applicationLink != null)
            {
                final ApplicationLinkRequestFactory authenticatedRequestFactory = applicationLink.createAuthenticatedRequestFactory();
                if (authenticatedRequestFactory != null)
                {
                    try
                    {
                        final RestResult<PlanStatus> restResult =  bambooRestService.getPlanStatus(authenticatedRequestFactory, subscription.planResultKey);
                        final PlanStatus planStatus = restResult.getResult();
                        if (planStatus != null)
                        {
                            runFinalizingAction(planStatus, subscription);
                        }
                        else
                        {
                            releaseErrorReportingService.recordErrors(subscription.projectKey, subscription.versionId, restResult.getErrors());
                        }
                    }
                    catch (CredentialsRequiredException e)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Authorisation Required", e);
                        }
                        log.info("JIRA could not connect to the Bamboo instance '" + applicationLink.getName() + "' to complete the release. This may require user authentications");
                    }
                }
                else
                {
                    applicationLinkUnavailable(subscription);
                }
            }
            else
            {
                applicationLinkUnavailable(subscription);
            }
        }

        private void runFinalizingAction(@NotNull PlanStatus status, @NotNull Subscription subscription)
        {
            final PlanResultKey planResultKey = subscription.planResultKey;
            final ApplicationLink applicationLink = subscription.getApplicationLink();

            if (applicationLink == null)
            {
                applicationLinkUnavailable(subscription);
            }
            else
            {
                log.info("Plan '" + status.getPlanResultKey() + "' BuildState: '" + status.getBuildState() + "' LifeCycleState: '" + status.getLifeCycleState() + "'");

                if (status.isValid() && LifeCycleState.isFinalized(status.getLifeCycleState()))
                {
                    log.info("Bamboo Release Plugin detected that " + planResultKey + " from " + applicationLink.getDisplayUrl() + " has finished.");
                    try
                    {
                        subscription.finalizingAction.execute(status);
                    }
                    catch (Throwable e)
                    {
                        log.error("Could not run action for subscription '" + planResultKey + "' for Bamboo Server '" + applicationLink + "'", e);
                    }
                    finally
                    {
                        unsubscribe(subscription.planResultKey);
                    }
                }
                else if (!status.isRecoverable())
                {
                    unsubscribe(subscription.planResultKey);
                    log.error("Status update reached an unrecoverable state for '" + planResultKey + "' from Bamboo Server '" + applicationLink + "'. JIRA will no longer update the status.");
                }

                log.info("Removing subscription to '" + applicationLink + "' for Plan '" + subscription.planResultKey + "'");
                removeFuture(subscription);
            }
        }
    }
}
