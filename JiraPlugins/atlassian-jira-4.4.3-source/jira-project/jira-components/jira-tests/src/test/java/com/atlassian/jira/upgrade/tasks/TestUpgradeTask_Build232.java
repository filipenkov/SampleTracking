package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheduler.cron.ConversionResult;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class TestUpgradeTask_Build232 extends LegacyJiraMockTestCase
{
    private static final MockGenericValue SUBSCRIPTION1_GV = new MockGenericValue("FilterSubscription", EasyMap.build("id", new Long(1), "username", "bob", "filterID", new Long(1)));
    private static final MockGenericValue SUBSCRIPTION2_GV = new MockGenericValue("FilterSubscription", EasyMap.build("id", new Long(2), "username", "bob", "filterID", new Long(1)));
    private static final String TRIGGER_NAME = "triggerName";
    private static final String TRIGGER_GROUP = "triggerGroup";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_GROUP = "jobGroup";
    private static final String CRON_STRING = "40 27 7 28 2 ?";
    private static final Date NEXT_FIRE_TIME = new Date();
    private User bob;

    public void testUpgradeTaskInfo()
    {
        UpgradeTask_Build232 upgradeTask_build232 = new UpgradeTask_Build232(null, null, new MockI18nBean.MockI18nBeanFactory());
        assertEquals("232", upgradeTask_build232.getBuildNumber());
        assertEquals("Converts all the old SimpleTriggers to CronTriggers for all filter subscriptions.", upgradeTask_build232.getShortDescription());
        bob = UtilsForTests.getTestUser("bobBuild232");
    }

    /**
     * Test to ensure only SimpleCronTriggers get Updated.  It should ignore the CronTrigger already defined.
     *
     * @throws Exception
     * @throws ParseException
     */
    public void testDoUpgrade() throws Exception, ParseException
    {
        // Create a BS simple trigger, scheduler and converter
        SimpleTrigger simpleTrigger = createSimpleTrigger();
        CronTrigger cronTrigger = new CronTrigger(TRIGGER_NAME, TRIGGER_GROUP, " 0 0/1 * * * ?");
        SubscriptionManager mockSubscriptionManager = createSubscriptionManager(simpleTrigger, cronTrigger);
        Mock mockScheduler = createScheduler();
        SimpleToCronTriggerConverter mockSimpleToCronTriggerConverter = createSimpleToCronTriggerConverter();

        UpgradeTask_Build232 upgradeTask_build232 = new UpgradeTask_Build232(mockSubscriptionManager, (Scheduler) mockScheduler.proxy(), mockSimpleToCronTriggerConverter, createOfBizDelegator(), new MockI18nBean.MockI18nBeanFactory())
        {
            @Override
            UserDetailBean getUserDetailBean(final String userName) throws GenericEntityException
            {
                return  new UserDetailBean(userName, "Bob", "bob@atlassian.com", Locale.getDefault());
            }
        };
        upgradeTask_build232.doUpgrade(false);

        mockScheduler.verify();
    }

    public void testConvertSimpleToCronTrigger() throws SchedulerException, ParseException, EntityNotFoundException
    {
        SimpleTrigger simpleTrigger = createSimpleTrigger();
        Mock mockScheduler = createScheduler();
        SimpleToCronTriggerConverter mockSimpleToCronTriggerConverter = createSimpleToCronTriggerConverter();

        // Build the upgrade task and convert a simple trigger
        UpgradeTask_Build232 upgradeTask_build232 = new UpgradeTask_Build232(null, (Scheduler) mockScheduler.proxy(), mockSimpleToCronTriggerConverter, createOfBizDelegator(), new MockI18nBean.MockI18nBeanFactory())
        {
            @Override
            UserDetailBean getUserDetailBean(final String userName) throws GenericEntityException
            {
                return  new UserDetailBean(userName, "Bob", "bob@atlassian.com", Locale.getDefault());
            }
        };
        CronTrigger cronTrigger = upgradeTask_build232.convertSimpleToCronTrigger(simpleTrigger, SUBSCRIPTION1_GV);

        // Test that reschedule gets called, also test that the new CronTrigger has the same job and name information
        // as the old trigger. Also test that the cron string is what we said it would be.
        mockScheduler.verify();
        assertEquals(TRIGGER_NAME, cronTrigger.getName());
        assertEquals(TRIGGER_GROUP, cronTrigger.getGroup());
        assertEquals(JOB_NAME, cronTrigger.getJobName());
        assertEquals(JOB_GROUP, cronTrigger.getJobGroup());
        assertEquals("40 27 7 28 2 ?", cronTrigger.getCronExpression());
    }

    private SimpleToCronTriggerConverter createSimpleToCronTriggerConverter()
    {
        MockControl controlSimpleToCronTriggerConverter = MockClassControl.createControl(SimpleToCronTriggerConverter.class);
        SimpleToCronTriggerConverter mockSimpleToCronTriggerConverter = (SimpleToCronTriggerConverter) controlSimpleToCronTriggerConverter.getMock();
        mockSimpleToCronTriggerConverter.convertToCronString(NEXT_FIRE_TIME, 60000L);
        controlSimpleToCronTriggerConverter.setReturnValue(new ConversionResult(true, CRON_STRING));
        controlSimpleToCronTriggerConverter.replay();
        return mockSimpleToCronTriggerConverter;
    }

    private SubscriptionManager createSubscriptionManager(SimpleTrigger simpleTrigger, CronTrigger cronTrigger)
            throws SchedulerException
    {
        MockControl controlSubscriptionManager = MockControl.createControl(SubscriptionManager.class);
        SubscriptionManager mockSubscriptionManager = (SubscriptionManager) controlSubscriptionManager.getMock();
        mockSubscriptionManager.getAllSubscriptions();
        controlSubscriptionManager.setReturnValue(getAllSubscriptions());
        mockSubscriptionManager.getTriggerFromSubscription(SUBSCRIPTION1_GV);
        controlSubscriptionManager.setReturnValue(simpleTrigger);
        mockSubscriptionManager.getTriggerFromSubscription(SUBSCRIPTION2_GV);
        controlSubscriptionManager.setReturnValue(cronTrigger);
        controlSubscriptionManager.replay();

        return mockSubscriptionManager;
    }

    private OfBizDelegator createOfBizDelegator() throws EntityNotFoundException
    {
        MockControl controlOfBizDelegator = MockControl.createNiceControl(OfBizDelegator.class);
        OfBizDelegator ofBizDelegator = (OfBizDelegator) controlOfBizDelegator.getMock();
        ofBizDelegator.findByPrimaryKey("SearchRequest", EasyMap.build("id", new Long(1)));
        MockGenericValue requestGv = new MockGenericValue("SearchRequest", EasyMap.build("id", new Long(1), "name", "Filter"));
        controlOfBizDelegator.setDefaultReturnValue(requestGv);
        controlOfBizDelegator.replay();

        return ofBizDelegator;
    }

    private Mock createScheduler()
    {
        Mock mockScheduler = new Mock(Scheduler.class);
        Constraint cronTriggerConstraint = new Constraint()
        {

            public boolean eval(Object object)
            {
                if (object instanceof CronTrigger)
                {
                    CronTrigger trigger = (CronTrigger) object;
                    if (trigger.getName().equals(TRIGGER_NAME) &&
                        trigger.getGroup().equals(TRIGGER_GROUP) &&
                        trigger.getJobName().equals(JOB_NAME) &&
                        trigger.getJobGroup().equals(JOB_GROUP) &&
                        trigger.getCronExpression().equals(CRON_STRING))
                    {
                        return true;
                    }
                }
                return false;
            }
        };

        mockScheduler.expectAndReturn("getTrigger", P.args(P.eq(TRIGGER_NAME), P.eq(TRIGGER_GROUP)), new SimpleTrigger());
        mockScheduler.expectAndReturn("unscheduleJob", P.args(P.eq(TRIGGER_NAME), P.eq(TRIGGER_GROUP)), Boolean.TRUE);
        mockScheduler.expectAndReturn("scheduleJob", P.args(cronTriggerConstraint), new Date());
        return mockScheduler;
    }

    private SimpleTrigger createSimpleTrigger()
    {
        // Create a BS simple trigger, scheduler and converter
        SimpleTrigger simpleTrigger = new SimpleTrigger(TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, new Date(), new Date(), SimpleTrigger.REPEAT_INDEFINITELY, 60000L);
        simpleTrigger.setNextFireTime(NEXT_FIRE_TIME);
        return simpleTrigger;
    }

    private List getAllSubscriptions()
    {
        List result = new ArrayList();
        result.add(SUBSCRIPTION1_GV);
        result.add(SUBSCRIPTION2_GV);

        return result;
    }

}
