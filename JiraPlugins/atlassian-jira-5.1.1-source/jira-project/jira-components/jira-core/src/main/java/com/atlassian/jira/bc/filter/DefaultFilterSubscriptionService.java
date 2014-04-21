package com.atlassian.jira.bc.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserNames;
import com.atlassian.jira.util.FilterCronValidationErrorMappingUtil;
import com.atlassian.jira.web.component.cron.CronEditorBean;
import com.atlassian.jira.web.component.cron.generator.CronExpressionDescriptor;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Uses quartz implementation of {@link org.quartz.CronTrigger}
 */
public class DefaultFilterSubscriptionService implements FilterSubscriptionService
{

    private static final Logger log = Logger.getLogger(DefaultFilterSubscriptionService.class);
    private final FilterCronValidationErrorMappingUtil errorMapper;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SubscriptionManager subscriptionManager;

    public DefaultFilterSubscriptionService(JiraAuthenticationContext jiraAuthenticationContext, SubscriptionManager subscriptionManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.subscriptionManager = subscriptionManager;
        errorMapper = new FilterCronValidationErrorMappingUtil(jiraAuthenticationContext);
    }

    DefaultFilterSubscriptionService(FilterCronValidationErrorMappingUtil errorMapper, JiraAuthenticationContext jiraAuthenticationContext, SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.errorMapper = errorMapper;
    }

    @Override
    public void validateCronExpression(JiraServiceContext context, String expr)
    {
        validateAndCreateCronTrigger(context, expr);
    }

    /**
     * Validate and create {@link CronTrigger} using expr.
     * Errors are passed back via the context
     *
     * @param context jira service context
     * @param expr    the Cron Expression to validate and turn into trigger
     * @return null if error occurs during creation
     */
    private CronTrigger validateAndCreateCronTrigger(JiraServiceContext context, String expr)
    {
        CronTrigger trigger = null;
        try
        {
            trigger = new CronTrigger("temp", "temp", expr);
            //Test the trigger by calculating next fire time.  This will catch some extra errors
            Date nextFireTime = trigger.getFireTimeAfter(null);
            if (nextFireTime == null)
            {
                String str = getText("filter.subsription.cron.errormessage.filter.never.run", expr);
                context.getErrorCollection().addErrorMessage(str);
            }
        }
        catch (ParseException e) // Generally known validations problems
        {
            errorMapper.mapError(context, e);
        }
        catch (IllegalArgumentException e) // Null expression
        {
            log.info("Cron expression was null. Probably the 'dailyWeeklyMonthly' request param was invalid", e);
            context.getErrorCollection().addErrorMessage(getText("filter.subsription.cron.errormessage.mode.error"));
        }
        catch (Exception e) // Unknown validation problems.
        {
            log.info("Unknown error when validating cron expression", e);
            context.getErrorCollection().addErrorMessage(getText("filter.subsription.cron.errormessage.general.error", expr));
        }

        return trigger;

    }

    @Override
    public void storeSubscription(JiraServiceContext context, Long filterId, String groupName, String expr, boolean emailOnEmpty)
    {
        Trigger trigger = validateAndCreateCronTrigger(context, expr);
        if (trigger != null && !context.getErrorCollection().hasAnyErrors())
        {
            subscriptionManager.createSubscription(context.getLoggedInUser(), filterId, groupName, trigger, Boolean.valueOf(emailOnEmpty));
        }
    }

    @Override
    public void updateSubscription(JiraServiceContext context, Long subscriptionId, String groupName, String expr, boolean emailOnEmpty)
    {
        Trigger trigger = validateAndCreateCronTrigger(context, expr);
        if (trigger != null && !context.getErrorCollection().hasAnyErrors())
        {
            subscriptionManager.updateSubscription(context.getLoggedInUser(), subscriptionId, groupName, trigger, Boolean.valueOf(emailOnEmpty));
        }
    }

    @Override
    public String getPrettySchedule(JiraServiceContext context, String cronExpression)
    {
        CronExpressionParser cronExpresionParser = new CronExpressionParser(cronExpression);
        if (cronExpresionParser.isValidForEditor())
        {
            CronEditorBean cronEditorBean = cronExpresionParser.getCronEditorBean();
            return new CronExpressionDescriptor(jiraAuthenticationContext.getI18nHelper()).getPrettySchedule(cronEditorBean);
        }
        else
        {
            return cronExpression;
        }
    }

    @Override
    public Collection<GenericValue> getVisibleSubscriptions(final User user, final SearchRequest filter)
    {

        if (filter != null)
        {
            // Anonymous users can't see any subscriptions
            if (user != null)
            {
                try
                {
                    // Owner can see all subscriptions
                    if (UserNames.equal(filter.getOwnerUserName(), user))
                    {
                        return subscriptionManager.getAllSubscriptions(filter.getId());
                    }
                    else
                    {
                        // only return owned subscriptions
                        return subscriptionManager.getSubscriptions(user, filter.getId());
                    }
                }
                catch (GenericEntityException e)
                {
                    throw new DataAccessException(e);
                }
            }
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    protected String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    @VisibleForTesting
    protected String getText(String key, Object param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

}
