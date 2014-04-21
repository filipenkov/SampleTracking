/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.SimpleToCronUtil;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import com.atlassian.jira.web.bean.FilterUtils;
import com.atlassian.jira.web.component.cron.CronEditorBean;
import com.atlassian.jira.web.component.cron.CronEditorWebComponent;
import com.atlassian.jira.web.component.cron.generator.CronExpressionGenerator;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import webwork.action.ActionContext;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * Action for CRUD of a scheduled email subscription to search filter results.
 */
public class FilterSubscription extends SearchDescriptionEnabledAction implements FilterOperationsAction
{
    private Long subId = null;
    private Long filterId = null;
    private String groupName;
    private Boolean emailOnEmpty = Boolean.FALSE;
    private Timestamp lastRun;
    private Timestamp nextRun;
    private SearchRequest searchRequest;
    private SubscriptionManager subscriptionManager = ManagerFactory.getSubscriptionManager();
    private CronEditorBean cronEditorBean;
    private final FilterSubscriptionService filterSubscriptionService;
    private final SearchRequestService searchRequestService;


    public FilterSubscription(IssueSearcherManager issueSearcherManager, FilterSubscriptionService filterSubscriptionService, SearchRequestService searchRequestService, SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);

        this.filterSubscriptionService = filterSubscriptionService;
        this.searchRequestService = searchRequestService;
    }

    public String doDefault() throws Exception
    {
        if (subId == null && filterId == null)
        {
            addErrorMessage(getText("filtersubscription.please.select.a.subscription.or.filter"));
            return ERROR;
        }
        else
        {
            if (subId != null)
            {
                GenericValue subscription = subscriptionManager.getSubscription(getLoggedInUser(), subId);
                if (subscription == null)
                {
                    return PERMISSION_VIOLATION_RESULT;
                }
                else
                {
                    groupName = subscription.getString("group");
                    emailOnEmpty = Boolean.valueOf(subscription.getString("emailOnEmpty"));
                    lastRun = subscription.getTimestamp("lastRun");

                    //Get from the trigger
                    Trigger trigger = subscriptionManager.getTriggerFromSubscription(subscription);
                    if (trigger instanceof SimpleTrigger)
                    {
                        //convert the simple trigger to cron. (Should really never happen as the upgrade task/integrity checker
                        //should have already taken care of this.
                        SimpleToCronUtil util = new SimpleToCronUtil(ComponentAccessor.getScheduler(), new SimpleToCronTriggerConverter());
                        boolean restartScheduler = util.pauseScheduler();
                        try
                        {
                            trigger = util.convertSimpleToCronTrigger((SimpleTrigger) trigger);
                        }
                        finally
                        {
                            util.restartScheduler(restartScheduler);
                        }
                    }

                    if (trigger instanceof CronTrigger)
                    {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        CronExpressionParser cronExpresionParser = new CronExpressionParser(cronExpression);
                        cronEditorBean = cronExpresionParser.getCronEditorBean();
                    }
                    else
                    {
                        log.error("Invalid trigger (" + trigger.getClass().getName()
                                + ") returned from subscriptionManager, expected CronTrigger as of JIRA v3.9. subScriptionId: " + subId);
                    }

                    nextRun = new Timestamp(trigger.getNextFireTime().getTime());
                }
            }

            if (filterId != null)
            {
                searchRequest = searchRequestService.getFilter(getJiraServiceContext(), filterId);
                if (searchRequest == null)
                {
                    return ERROR;
                }
            }
        }
        return super.doDefault();
    }


    public void doValidation()
    {
        cronEditorBean = new CronEditorBean("filter.subscription.prefix", ActionContext.getParameters());
        CronEditorWebComponent component = new CronEditorWebComponent();
        addErrorCollection(component.validateInput(cronEditorBean, "cron.editor.name"));
        if (!hasAnyErrors())
        {
            JiraServiceContext serviceContext = getJiraServiceContext();
            String cronString = component.getCronExpressionFromInput(cronEditorBean);
            filterSubscriptionService.validateCronExpression(serviceContext, cronString);
        }
    }

    protected String doExecute() throws Exception
    {
        String cronExpression = new CronExpressionGenerator().getCronExpressionFromInput(cronEditorBean);
        if (subId != null)
        {
            // we have a subscription id so we are editing it
            GenericValue subscription = subscriptionManager.getSubscription(getLoggedInUser(), subId);
            if (subscription == null)
            {
                return PERMISSION_VIOLATION_RESULT;
            }


            filterSubscriptionService.updateSubscription(getJiraServiceContext(), subId, getGroupName(), cronExpression, emailOnEmpty.booleanValue());
        }
        else
        {
            // no subscription id so we are creating a new one
            searchRequest = searchRequestService.getFilter(getJiraServiceContext(), filterId);
            if (searchRequest == null)
            {
                return "securitybreach";
            }
            filterSubscriptionService.storeSubscription(getJiraServiceContext(), filterId, getGroupName(), cronExpression, getEmailOnEmpty().booleanValue());
        }
        return getRedirect("ViewSubscriptions.jspa?filterId=" + filterId);
    }

    public String doDelete() throws Exception
    {
        GenericValue subscription = subscriptionManager.getSubscription(getLoggedInUser(), subId);

        if (subscription == null)
        {
            addErrorMessage(getText("subscriptions.error.delete.subscriptiondoesnotexist"));
            return ERROR;
        }
        ManagerFactory.getSubscriptionManager().deleteSubscription(subId);
        return getRedirect("ViewSubscriptions.jspa?filterId=" + filterId);
    }

    public String doRunNow() throws Exception
    {
        GenericValue subscription = subscriptionManager.getSubscription(getLoggedInUser(), subId);

        if (subscription == null)
        {
            addErrorMessage(getText("subscriptions.error.runnow.subscriptiondoesnotexist"));
            return ERROR;
        }

        ManagerFactory.getSubscriptionManager().runSubscription(getLoggedInUser(), subId);
        return getRedirect("ViewSubscriptions.jspa?filterId=" + filterId);
    }

    public boolean hasGroupPermission() throws GenericEntityException
    {
        return ManagerFactory.getPermissionManager().hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, getLoggedInUser());
    }

    public String getSubmitName() throws GenericEntityException
    {
        if (subId == null)
        {
            return getText("filtersubscription.subscribe");
        }
        else
        {
            return getText("common.forms.update");
        }
    }

    public String getCancelStr() throws GenericEntityException
    {
        if (subId == null)
        {
            return "ManageFilters.jspa";
        }
        else
        {
            return "ViewSubscriptions.jspa?filterId=" + filterId;
        }
    }

    public String getFilterId()
    {
        if (filterId == null)
        {
            return null;
        }
        else
        {
            return filterId.toString();
        }
    }

    public void setFilterId(String filterId)
    {
        if (TextUtils.stringSet(filterId))
        {
            this.filterId = new Long(filterId);
        }
    }

    public String getSubId()
    {
        if (subId == null)
        {
            return null;
        }
        else
        {
            return subId.toString();
        }
    }

    public void setSubId(String subId)
    {
        if (TextUtils.stringSet(subId))
        {
            this.subId = new Long(subId);
        }
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public Boolean getEmailOnEmpty()
    {
        return emailOnEmpty;
    }

    public void setEmailOnEmpty(Boolean emailOnEmpty)
    {
        this.emailOnEmpty = emailOnEmpty;
    }

    public String getLastRun()
    {
        if (lastRun == null)
        {
            return null;
        }
        else
        {
            return String.valueOf(lastRun.getTime());
        }
    }

    public void setLastRun(String lastRun)
    {
        if (TextUtils.stringSet(lastRun))
        {
            this.lastRun = new Timestamp(Long.parseLong(lastRun));
        }
    }

    public String getNextRun()
    {
        if (nextRun == null)
        {
            return null;
        }
        else
        {
            return String.valueOf(nextRun.getTime());
        }
    }

    public void setNextRun(String nextRun)
    {
        if (TextUtils.stringSet(nextRun))
        {
            this.nextRun = new Timestamp(Long.parseLong(nextRun));
        }
    }

    public String getLastRunStr()
    {
        if (lastRun == null)
        {
            return null;
        }
        else
        {
            return ManagerFactory.getOutlookDateManager().getOutlookDate(getLocale()).formatDMYHMS(lastRun);
        }
    }

    public String getNextRunStr()
    {
        if (nextRun == null)
        {
            return null;
        }
        else
        {
            return ManagerFactory.getOutlookDateManager().getOutlookDate(getLocale()).formatDMYHMS(nextRun);
        }
    }

    public Collection getGroups()
    {
        return FilterUtils.getGroups(getLoggedInUser());
    }

    public CronEditorBean getCronEditorBean()
    {
        if (cronEditorBean == null)
        {
            cronEditorBean = new CronExpressionParser().getCronEditorBean();
        }
        return cronEditorBean;
    }
}
