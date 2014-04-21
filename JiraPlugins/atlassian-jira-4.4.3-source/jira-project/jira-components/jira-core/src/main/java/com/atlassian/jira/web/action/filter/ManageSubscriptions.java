package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.util.SimpleToCronUtil;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.UserManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * Action class for Managaing Subscriptions.
 * Was previously a command of ManageFilters, but has been extracted for increase security.  Action now is protected by
 * the user role.
 */
public class ManageSubscriptions extends SearchDescriptionEnabledAction implements FilterOperationsAction
{
    private Long filterId;
    private SearchRequest filter;

    private final SearchRequestService searchRequestService;
    private final SubscriptionManager subscriptionManager;
    private final FilterSubscriptionService filterSubscriptionService;
    private final MailServerManager mailServerManager;
    private Collection subscriptions;

    public ManageSubscriptions(final IssueSearcherManager issueSearcherManager, final SearchRequestService searchRequestService,
            final SubscriptionManager subscriptionManager, final FilterSubscriptionService filterSubscriptionService,
            final MailServerManager mailServerManager, final SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.subscriptionManager = subscriptionManager;
        this.filterSubscriptionService = filterSubscriptionService;
        this.mailServerManager = mailServerManager;
    }

    public String doDefault() throws Exception
    {
        // If filter doesn't exist, go to Manage Filters
        return (filterId == null) ? getRedirect("ManageFilters.jspa") : super.doDefault();
    }

    public int getSubscriptionCount()
    {

        return getSubscriptions().size();
    }

    public Collection getSubscriptions()
    {
        if (subscriptions == null)
        {
            subscriptions = filterSubscriptionService.getVisibleSubscriptions(getRemoteUser(), getFilter());
        }

        return subscriptions;
    }

    public boolean isMailConfigured()
    {
        try
        {
            final List smtpServers = mailServerManager.getSmtpMailServers();
            return !smtpServers.isEmpty();
        }
        catch (MailException e)
        {
            return false;
        }
    }

    public String doView()
    {
        return SUCCESS;
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(Long filterId)
    {
        this.filterId = filterId;
    }

    public String getFilterName()
    {
        final SearchRequest filter = getFilter();
        return (filter == null) ? null : filter.getName();
    }

    /**
     * Get the associated filter
     *
     * @return the filter with subscriptions
     */
    private SearchRequest getFilter()
    {
        if (filter == null && filterId != null)
        {
            filter = searchRequestService.getFilter(getJiraServiceContext(), filterId);
        }
        return filter;
    }

    /**
     * Get the tooltip for the for a subscription.
     *
     * @param sub The subscrion to get the tooltip for
     * @return The tooltip
     */
    public String getCronTooltip(GenericValue sub)
    {
        final CronTrigger trigger = getTrigger(sub);

        return trigger == null ? "" : getText("cron.editor.cronstring") + " '" + trigger.getCronExpression() + "'";
    }

    /**
     * Get a pretty version of the cron trigger.  E.g. Every day at 12
     *
     * @param sub The subscription to get the value for.
     * @return A description of the cron trigger id pretty format
     */
    public String getPrettySchedule(GenericValue sub)
    {
        final CronTrigger trigger = getTrigger(sub);

        return (trigger == null) ? "" : filterSubscriptionService.getPrettySchedule(getJiraServiceContext(), trigger.getCronExpression());
    }

    /**
     * Get the las sent date for a subscription
     *
     * @param sub The subscription to get last send for
     * @return A date suitable for displaying
     */
    public String getLastSent(GenericValue sub)
    {
        final Timestamp ts = sub.getTimestamp("lastRun");

        return (ts == null) ? "Never" : getOutlookDate().formatDMYHMS(ts);

    }

    /**
     * Get the next sent date for a subscription
     *
     * @param sub The subscription to get next send for
     * @return A date suitable for displaying
     */
    public String getNextSend(GenericValue sub)
    {
        final CronTrigger trigger = getTrigger(sub);

        return (trigger == null) ? "" : getOutlookDate().formatDMYHMS(trigger.getNextFireTime());
    }


    /**
     * Gets teh associated trigger for a subscription.  It should always be a CronTrigger.  If, somehow, it is a
     * simple trigger, convert it to a CronTrigger
     *
     * @param sub The subscription with the associated trigger
     * @return The associated CronTrigger
     */
    private CronTrigger getTrigger(final GenericValue sub)
    {
        try
        {
            Trigger trigger = subscriptionManager.getTriggerFromSubscription(sub);
            if (trigger instanceof SimpleTrigger)
            {
                trigger = convertSimpleToCron((SimpleTrigger) trigger);
            }

            if (trigger == null)
            {
                log.error("unable to find trigger for subscription " + sub.getLong("id"));
            }
            return (CronTrigger) trigger;

        }
        catch (SchedulerException e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * This code converts a simple trigger to a cron trigger.   This should never happen as Simple Triggers are coverted
     * at during an upgrade task.  This is a "Just in case" method
     *
     * @param trigger The SimpleTrigger to convert
     * @return An aproximae CronTrigger
     */
    private CronTrigger convertSimpleToCron(final SimpleTrigger trigger)
    {
        final SimpleToCronUtil util = new SimpleToCronUtil(ManagerFactory.getScheduler(), new SimpleToCronTriggerConverter());
        final CronTrigger cronTrigger;
        boolean restartScheduler = util.pauseScheduler();
        try
        {
            cronTrigger = util.convertSimpleToCronTrigger(trigger);

        }
        finally
        {
            util.restartScheduler(restartScheduler);
        }

        return cronTrigger;
    }


    public boolean isGroupValid(GenericValue filter)
    {
        String groupName = filter.getString("group");
        

        if (TextUtils.stringSet(groupName))
        {
            try
            {
                UserManager.getInstance().getGroup(groupName);
                return true;
            }
            catch (EntityNotFoundException enfe)
            {
                // if we get this then there is no group
            }
        }
        return false;
    }

}