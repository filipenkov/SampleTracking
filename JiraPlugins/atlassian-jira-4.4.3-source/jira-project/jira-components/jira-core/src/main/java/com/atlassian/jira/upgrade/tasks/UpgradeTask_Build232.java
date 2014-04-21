package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheduler.cron.ConversionResult;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.util.SimpleToCronUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.component.cron.generator.CronExpressionDescriptor;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Upgrade task that converts all the old SimpleTriggers to CronTriggers for
 * filter subscriptions.
 */
public class UpgradeTask_Build232 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build232.class);

    private static final String EMAIL_TEMPLATE_LOSSY = "lossysubscriptionconversion.vm";
    private static final String LOSSY_SUBJECT = "template.filters.schedule.lossy.subject";
    private static final String ERROR_SUBJECT = "template.filters.schedule.error.subject";
    private static final String EMAIL_TEMPLATE_ERROR = "errorsubscriptionconversion.vm";

    private final Map<String, Set<LossySubscription>> lossySubscriptions = new HashMap<String, Set<LossySubscription>>();
    private final SubscriptionManager subscriptionManager;
    private final OfBizDelegator ofBizDelegator;
    private final I18nHelper.BeanFactory i18n;

    private int numberWithLoss = 0;
    private final SimpleToCronUtil simpleToCronUtil;

    public UpgradeTask_Build232(final SubscriptionManager subscriptionManager, final OfBizDelegator ofBizDelegator, I18nHelper.BeanFactory i18n)
    {
        this(subscriptionManager, ManagerFactory.getScheduler(), new SimpleToCronTriggerConverter(), ofBizDelegator, i18n);
    }

    /*
     * Package level constructor for testing.
     */
    UpgradeTask_Build232(SubscriptionManager subscriptionManager, Scheduler scheduler, SimpleToCronTriggerConverter simpleToCronTriggerConverter, final OfBizDelegator ofBizDelegator, I18nHelper.BeanFactory i18n)
    {
        this.subscriptionManager = subscriptionManager;
        this.ofBizDelegator = ofBizDelegator;
        this.i18n = i18n;
        this.simpleToCronUtil = new SimpleToCronUtil(scheduler, simpleToCronTriggerConverter);
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "Converts all the old SimpleTriggers to CronTriggers for all filter subscriptions.";
    }

    public String getBuildNumber()
    {
        return "232";
    }

    public void doUpgrade(boolean setupMode)
    {
        // Get all filter subscriptions
        List<GenericValue> allSubscriptions = subscriptionManager.getAllSubscriptions();

        int totalSubs = allSubscriptions.size();

        boolean restartScheduler = simpleToCronUtil.pauseScheduler();

        try
        {
            for (final GenericValue filterSubscriptionGV : allSubscriptions)
            {
                try
                {
                    // Get the current Trigger from the subscription
                    Trigger triggerFromSubscription = subscriptionManager.getTriggerFromSubscription(filterSubscriptionGV);
                    if (triggerFromSubscription == null)
                    {
                        removeSubscription(filterSubscriptionGV);
                    }
                    else
                    {
                        // Only convert SimpleTriggers
                        if (triggerFromSubscription instanceof SimpleTrigger)
                        {
                            convertSimpleToCronTrigger((SimpleTrigger) triggerFromSubscription, filterSubscriptionGV);
                        }
                    }
                }
                catch (SchedulerException e)
                {
                    log.error("Problem retrieveing trigger for a subscription.  Please run integrity checker after upgrade.", e);
                }
            }
        }
        finally
        {
            simpleToCronUtil.restartScheduler(restartScheduler);
            sendLossyMails();
        }

        log.info("Total Subscriptions updated: " + totalSubs);
        log.info("Total Subscriptions converted with loss: " + numberWithLoss);
    }

    private void removeSubscription(GenericValue filterSubscriptionGV)
    {
        try
        {
            Long id = filterSubscriptionGV.getLong("id");
            log.info("Removing subsciption with no trigger. Subscription Id: " + id);
            subscriptionManager.deleteSubscription(id);
        }
        catch (Exception e)
        {
            log.error("Error while removing invalid subscription", e);
        }
    }

    // This is returning the cron trigger for testing purposes
    CronTrigger convertSimpleToCronTrigger(SimpleTrigger triggerFromSubscription, GenericValue filterSubscriptionGV)
    {
        CronTrigger cronTrigger = null;
        try
        {
            ConversionResult conversionResult = simpleToCronUtil.convertToCronString(triggerFromSubscription);
            cronTrigger = simpleToCronUtil.createCronTrigger(triggerFromSubscription, conversionResult);

            if (conversionResult.hasLoss)
            {
                numberWithLoss++;
                recordLossySubscription(filterSubscriptionGV, triggerFromSubscription, cronTrigger);
            }
        }
        catch (ParseException e)
        {
            log.error("Subscription with interval: " + triggerFromSubscription.getRepeatInterval() + " and nextFireTime: " + triggerFromSubscription.getNextFireTime() +
                      " failed during parsing", e);

            //notify users of errors and unschedule their subscription.
            simpleToCronUtil.unscheduleJob(triggerFromSubscription);
            sendErrorMail(filterSubscriptionGV, triggerFromSubscription);
        }
        catch (SchedulerException e)
        {
            log.error("Un/Scheduling subscription failed with name: " + triggerFromSubscription.getName(), e);

            //notify users of errors
            sendErrorMail(filterSubscriptionGV, triggerFromSubscription);
        }


        return cronTrigger;
    }

    /**
     * Stores lossySubscriptions in a map with user -> filterId[].
     * <p/>
     * This is used to send out digest e-mails to the users affected.
     *
     * @param filterSubscriptionGV The filter subscription for which we are recording loss.
     * @param oldTrigger           The old Quartz trigger to schedule the subscription.
     * @param newTrigger           The new Cron trigger to schedule the subscription.
     */
    private void recordLossySubscription(GenericValue filterSubscriptionGV, SimpleTrigger oldTrigger, CronTrigger newTrigger)
    {
        String username = filterSubscriptionGV.getString("username");

        LossySubscription lossySubscription;

        UserDetailBean userDetails = null;
        Locale userLocale;
        try
        {
            userDetails = getUserDetailBean(username);
            userLocale = userDetails.getLocale();
        }
        catch (GenericEntityException e)
        {
            userLocale = ComponentAccessor.getApplicationProperties().getDefaultLocale();
            log.warn("Lossy subscription converted for user [" + username + "] with default locale.");
        }

        final Long filterId = filterSubscriptionGV.getLong("filterID");
        final GenericValue searchRequestGv = ofBizDelegator.findByPrimaryKey("SearchRequest", EasyMap.build("id", filterId));

        if (searchRequestGv != null)
        {
            lossySubscription = new LossySubscription(userDetails.getLocale(), filterSubscriptionGV.getLong("id"), oldTrigger, newTrigger.getCronExpression(), filterId, searchRequestGv.getString("name"), i18n);

            Set<LossySubscription> lossySubscriptionsSet = lossySubscriptions.get(username);
            if (lossySubscriptionsSet == null)
            {
                lossySubscriptionsSet = new HashSet<LossySubscription>();
                lossySubscriptions.put(username, lossySubscriptionsSet);
            }

            lossySubscriptionsSet.add(lossySubscription);
        }
        else
        {
            log.info("Lossy subscription converted for user [" + username + "].  No matching searchrequest could be found. No e-mail will be sent to user. Please run the Integrity checker 'Check for invalid subscriptions'");
        }

    }


    /**
     * Helper class used to record information regarding a lossy subscription.
     */
    private static class LossySubscription
    {
        private final Long subscriptionId;
        private final Long filterId;
        private final String filterName;
        private final String oldPrettyInterval;
        private final String newCronTrigger;

        public LossySubscription(Locale locale, Long subscriptionId, SimpleTrigger oldTrigger, String cronExpression, Long searchRequestId, String searchRequestName, I18nHelper.BeanFactory i18n)
        {

            I18nHelper i18nBean = i18n.getInstance(locale);
            ResourceBundle bundle = ResourceBundle.getBundle(JiraWebActionSupport.class.getName(), locale);

            this.filterId = searchRequestId;
            this.subscriptionId = subscriptionId;


            filterName = searchRequestName;
            oldPrettyInterval = DateUtils.getDurationPretty((oldTrigger.getRepeatInterval() / DateUtils.SECOND_MILLIS), bundle);

            if (cronExpression != null)
            {
                CronExpressionParser parser = new CronExpressionParser(cronExpression);
                CronExpressionDescriptor descriptor = new CronExpressionDescriptor(i18nBean);
                newCronTrigger = descriptor.getPrettySchedule(parser.getCronEditorBean());
            }
            else
            {
                newCronTrigger = null;
            }
        }

        public Long getSubscriptionId()
        {
            return subscriptionId;
        }

        public Long getFilterId()
        {
            return filterId;
        }

        public String getFilterName()
        {
            return filterName;
        }

        public String getOldPrettyInterval()
        {
            return oldPrettyInterval;
        }

        public String getNewCronTrigger()
        {
            return newCronTrigger;
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            LossySubscription that = (LossySubscription) o;

            if (filterId != null ? !filterId.equals(that.filterId) : that.filterId != null)
            {
                return false;
            }
            if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null)
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            int result;
            result = (subscriptionId != null ? subscriptionId.hashCode() : 0);
            result = 31 * result + (filterId != null ? filterId.hashCode() : 0);
            return result;
        }

    }

    private void sendLossyMails()
    {
        try
        {
            for (Map.Entry<String, Set<LossySubscription>> entry : lossySubscriptions.entrySet())
            {
                final String key = entry.getKey();
                try
                {
                    final Set<LossySubscription> subscriptions = entry.getValue();
                    if (subscriptions != null && !subscriptions.isEmpty())
                    {
                        UserDetailBean userDetail = getUserDetailBean(key);
                        UpgradeTask_Build232MailItem mailItem = new UpgradeTask_Build232MailItem(userDetail.getEmail(), userDetail.getFullName(), userDetail.getLocale(), LOSSY_SUBJECT, EMAIL_TEMPLATE_LOSSY, subscriptions, i18n);

                        ManagerFactory.getMailQueue().addItem(mailItem);
                        log.info("Lossy subscription conversion for user [" + key + "]. User notified via e-mail.");
                    }

                }
                catch (Exception e)
                {
                    log.error("Sending email to user [" + key + "]  for lossy subscription failed.", e);
                }
            }
        }
        finally
        {
            //not 100% sure if this is necessary, as I'm quite certain that the UpgradeTasks don't hang around,
            // but it never hurts to be safe.  Don't want to leak anything.
            lossySubscriptions.clear();
        }
    }

    private void sendErrorMail(GenericValue filterSubscriptionGV, SimpleTrigger oldTrigger)
    {
        String username = filterSubscriptionGV.getString("username");
        final HashSet<LossySubscription> errorSubscriptionsSet = new HashSet<LossySubscription>();
        // Get the user details we need


        final Long filterId = filterSubscriptionGV.getLong("filterID");

        final GenericValue searchRequestGv = ofBizDelegator.findByPrimaryKey("SearchRequest", EasyMap.build("id", filterId));

        UserDetailBean userDetail = null;
        try
        {
            userDetail = getUserDetailBean(username);
            if (searchRequestGv != null)
            {
                LossySubscription lossySubscription = new LossySubscription(userDetail.getLocale(), filterSubscriptionGV.getLong("id"), oldTrigger, null, filterId, searchRequestGv.getString("name"), i18n);
                errorSubscriptionsSet.add(lossySubscription);
            }

            UpgradeTask_Build232MailItem mailItem = new UpgradeTask_Build232MailItem(userDetail.getEmail(), userDetail.getFullName(), userDetail.getLocale(), ERROR_SUBJECT, EMAIL_TEMPLATE_ERROR, errorSubscriptionsSet, i18n);

            ManagerFactory.getMailQueue().addItem(mailItem);
        }
        catch (GenericEntityException e)
        {
            log.error("Sending email to user [" + username + "] for subscription conversion failure, failed.", e);
        }
    }

    UserDetailBean getUserDetailBean(final String userName) throws GenericEntityException
    {
        return new UserDetailBean(userName);
    }

    /**
     * Bean to hold very basic detail of a user.
     */
    class UserDetailBean
    {
        private String email;
        private String fullName;
        private Locale locale;

        UserDetailBean (String username, String fullName, String email, Locale locale) throws GenericEntityException
        {
            this.fullName = fullName;
            this.email = email;
            this.locale = locale;
        }

        UserDetailBean (String username) throws GenericEntityException
        {
            // Try to get an Internal entity first.
            if (!getUserDetail(username, "OSUser"))
            {
                // Try for an External user
                if (!getUserDetail(username, "OSUser"))
                {
                    throw new GenericEntityException("User not found");
                }
            }
        }

        private boolean getUserDetail(final String username, final String userEntityType)
        {
            List OSUserGVs = null;
            try
            {
                OSUserGVs = getDelegator().findByAnd(userEntityType, EasyMap.build("name", username), EasyList.build("name ASC"));
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException(e);
            }
            // There should only ever be one, so we will take the first.
            if (OSUserGVs.size() == 0)
            {
                return false;
            }
            GenericValue osUser = (GenericValue) OSUserGVs.iterator().next();
            PropertySet ofbizPs = OFBizPropertyUtils.getPropertySet(osUser);
            email = ofbizPs.getString("email");
            fullName = ofbizPs.getString("fullName");
            String localeStr = ofbizPs.getString(PreferenceKeys.USER_LOCALE);

            if (StringUtils.isBlank(localeStr))
            {
                locale = ComponentAccessor.getApplicationProperties().getDefaultLocale();
            }
            else
            {
                locale = LocaleParser.parseLocale(localeStr);
            }
            return true;
        }

        public String getEmail()
        {
            return email;
        }

        public String getFullName()
        {
            return fullName;
        }

        public Locale getLocale()
        {
            return locale;
        }
    }

 }