package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.check.DeleteTriggerAmendmentImpl;
import com.atlassian.jira.appconsistency.integrity.check.SimpleTriggerCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.quartz.*;

import java.util.Date;
import java.util.List;

/**
 * Unit test to verify the Integrity check to remove/convert SimpleTriggers is working correctly.
 */
public class TestSimpleTriggerCheck extends AbstractUsersTestCase
{
    private GenericDelegator genericDelegator;
    private SimpleTriggerCheck triggerCheck;
    private Scheduler scheduler;
    private static final String SEND_SUBSCRIPTION = "SEND_SUBSCRIPTION";
    private static final SimpleTrigger TRIGGER_1 = new SimpleTrigger("SUBSCRIPTION_1", SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY, 60000L);
    private static final SimpleTrigger TRIGGER_56 = new SimpleTrigger("SUBSCRIPTION_56", SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY, 60000L);

    public TestSimpleTriggerCheck(String s)
    {
        super(s);
    }

    /**
     * Setup some test data, with 2 subscriptions (1 cron and 1 simple).  Also adds 3 Triggers (2 simple and 1 cron).
     * @throws Exception
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);

        // Create 3 subs but only one has a search request
        UtilsForTests.getTestUser("nick");
        UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", new Long(1), "name", "my search request", "project", new Long(1), "author", "nick", "request", "<searchrequest name='my search request'> <sort class='com.atlassian.jira.issue.search.SearchSort'> <searchSort field='issuekey' order='DESC'/> </sort> </searchrequest>"));
        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(1), "filterID", new Long(1), "username", "nick"));

        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(2), "filterID", new Long(1), "username", "nick"));


        genericDelegator = CoreFactory.getGenericDelegator();
        scheduler = ManagerFactory.getScheduler();
        //lets add a simple trigger linked to a subscription
        Job job = new Job()
        {

            public void execute(JobExecutionContext context) throws JobExecutionException
            {
                //do nothihg.
            }
        };
        scheduler.addJob(new JobDetail(SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, job.getClass()), true);
        scheduler.scheduleJob(TRIGGER_1);
        // and one without.
        scheduler.scheduleJob(TRIGGER_56);
        // lets add a cron trigger
        CronTrigger cronTrigger = new CronTrigger("SUBSCRIPTION_2", SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, SEND_SUBSCRIPTION, "0 0 10 * * ?");
        scheduler.scheduleJob(cronTrigger);

        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        triggerCheck = new SimpleTriggerCheck(ofBizDelegator, 1);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        scheduler.unscheduleJob("SUBSCRIPTION_1", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_2", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_56", SEND_SUBSCRIPTION);
        this.genericDelegator = null;
        this.triggerCheck = null;
        this.scheduler = null;

    }

    /**
     * ensure that the test data has been setup correctly.
     */
    public void testCheckDatabase() throws GenericEntityException, SchedulerException
    {
        String[] qrtzTriggers = scheduler.getTriggerNames(SEND_SUBSCRIPTION);

        assertNotNull(qrtzTriggers);
        assertEquals(3, qrtzTriggers.length);

        assertTriggerCounts(qrtzTriggers, 2, 1);
    }

    /**
     * Ensure that the preview function works if there are no subscriptions in the database.
     * @throws SchedulerException
     * @throws IntegrityException
     */
    public void testPreviewEmpty() throws SchedulerException, IntegrityException
    {
        scheduler.unscheduleJob("SUBSCRIPTION_1", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_2", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_56", SEND_SUBSCRIPTION);

        List amendments = triggerCheck.preview();
        assertNotNull(amendments);
        assertEquals(0, amendments.size());
    }

    /**
     * Ensure that preview displays all the correct amendments.
     * @throws IntegrityException
     * @throws GenericEntityException
     * @throws SchedulerException
     */
    public void testPreview() throws IntegrityException, GenericEntityException, SchedulerException
    {

        List amendments = triggerCheck.preview();
        assertNotNull(amendments);
        assertEquals(2, amendments.size());

        DeleteTriggerAmendmentImpl amendment = new DeleteTriggerAmendmentImpl(Amendment.ERROR, "SimpleTrigger with name SUBSCRIPTION_1 will be converted to a CronTrigger", TRIGGER_1);
        DeleteTriggerAmendmentImpl amendment2 = new DeleteTriggerAmendmentImpl(Amendment.ERROR, "SimpleTrigger with name SUBSCRIPTION_56 is missing its corresponding filter subscription and will be removed", TRIGGER_56);

        assertTrue(amendments.contains(amendment));
        assertTrue(amendments.contains(amendment2));

        assertTriggerCounts(scheduler.getTriggerNames(SEND_SUBSCRIPTION), 2, 1);
    }

    /**
     * Ensure that correct can handle no subscriptions.
     * @throws SchedulerException
     * @throws IntegrityException
     */
    public void testCorrectEmpty() throws SchedulerException, IntegrityException
    {
        scheduler.unscheduleJob("SUBSCRIPTION_1", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_2", SEND_SUBSCRIPTION);
        scheduler.unscheduleJob("SUBSCRIPTION_56", SEND_SUBSCRIPTION);

        List amendments = triggerCheck.correct();
        assertNotNull(amendments);
        assertEquals(0, amendments.size());
    }

    /**
     * Ensure that correct produces the correct ammendments and changes.
     * @throws IntegrityException
     * @throws GenericEntityException
     * @throws SchedulerException
     */
    public void testCorrect() throws IntegrityException, GenericEntityException, SchedulerException
    {
        List amendments = triggerCheck.correct();
        assertNotNull(amendments);
        assertEquals(2, amendments.size());

        DeleteTriggerAmendmentImpl amendment = new DeleteTriggerAmendmentImpl(Amendment.CORRECTION, "SimpleTrigger with name SUBSCRIPTION_1 has been converted to a CronTrigger", TRIGGER_1);
        DeleteTriggerAmendmentImpl amendment2 = new DeleteTriggerAmendmentImpl(Amendment.CORRECTION, "SimpleTrigger with name SUBSCRIPTION_56 is missing its corresponding filter subscription and has been removed", TRIGGER_56);

        assertTrue(amendments.contains(amendment));
        assertTrue(amendments.contains(amendment2));

        assertTriggerCounts(scheduler.getTriggerNames(SEND_SUBSCRIPTION), 0, 2);
    }

    private void assertTriggerCounts(String[] qrtzTriggers, int expectedSimple, int expectedCron)
            throws SchedulerException
    {
        int simpleTriggerCount = 0;
        int cronTriggerCount = 0;

        for (int i = 0; i < qrtzTriggers.length; i++)
        {
            String qrtzTrigger = qrtzTriggers[i];
            Trigger trigger = scheduler.getTrigger(qrtzTrigger, SEND_SUBSCRIPTION);
            if (trigger instanceof SimpleTrigger)
            {
                simpleTriggerCount++;
            }
            else if (trigger instanceof CronTrigger)
            {
                cronTriggerCount++;
            }
        }
        assertEquals(expectedSimple, simpleTriggerCount);
        assertEquals(expectedCron, cronTriggerCount);
    }
}
