/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 5, 2002
 * Time: 5:50:24 PM
 * To change this template use Options | File Templates.
 */
package test.atlassian.scheduler;

import junit.framework.TestCase;
import com.atlassian.scheduler.SchedulerConfig;
import com.atlassian.core.util.DateUtils;

import java.util.List;
import java.util.Date;
import java.util.Map;

import org.quartz.*;
import mock.atlassian.scheduler.SimpleJob;

public class TestSchedulerConfig extends TestCase
{
    public TestSchedulerConfig(String s)
    {
        super(s);
    }

    public void testLoadJobs()
    {
        SchedulerConfig config = new SchedulerConfig("simple-scheduler-config.xml");
        Map jobs = config.getJobs();
        assertEquals(2, jobs.size());

        JobDetail job = (JobDetail)jobs.get(Scheduler.DEFAULT_GROUP + ".FooJob");
        assertEquals("FooJob", job.getName());
        assertEquals(SimpleJob.class, job.getJobClass());
        assertEquals(Scheduler.DEFAULT_GROUP, job.getGroup());

        job = (JobDetail)jobs.get("BarJobGroup.BarJob");
        assertEquals("BarJob", job.getName());
        assertEquals(SimpleJob.class, job.getJobClass());
        assertEquals("BarJobGroup", job.getGroup());
    }

    public void testTriggers()
    {
        SchedulerConfig config = new SchedulerConfig("simple-scheduler-config.xml");
        List triggers = config.getTriggers();
        assertEquals(4, triggers.size());

        SimpleTrigger trigger = (SimpleTrigger)triggers.get(0);
        assertEquals("SimpleTrigger", trigger.getName());
        assertEquals(Scheduler.DEFAULT_GROUP, trigger.getGroup());
        assertEquals("FooJob", trigger.getJobName());
        assertEquals(Scheduler.DEFAULT_GROUP, trigger.getJobGroup());
        assertEquals(DateUtils.HOUR_MILLIS, trigger.getRepeatInterval());
        assertEquals(SimpleTrigger.REPEAT_INDEFINITELY, trigger.getRepeatCount());
        assertTrue(new Date(System.currentTimeMillis() + DateUtils.HOUR_MILLIS - 100).before(trigger.getStartTime()));
        assertTrue(new Date(System.currentTimeMillis() + DateUtils.HOUR_MILLIS + 100).after(trigger.getStartTime()));

        trigger = (SimpleTrigger)triggers.get(1);
        assertEquals("AnotherSimpleTrigger", trigger.getName());
        assertEquals("ATriggerGroup", trigger.getGroup());
        assertEquals("BarJob", trigger.getJobName());
        assertEquals("BarJobGroup", trigger.getJobGroup());
        assertEquals(DateUtils.MINUTE_MILLIS, trigger.getRepeatInterval());
        assertEquals(10, trigger.getRepeatCount());
        Date d = new Date();
        assertTrue(d.equals(trigger.getStartTime()) || d.after(trigger.getStartTime()));

        CronTrigger ctrigger = (CronTrigger)triggers.get(2);
        assertEquals("TestCronTrigger", ctrigger.getName());
        assertEquals("FooJob", ctrigger.getJobName());
        assertEquals("0 0/5 * * * ?", ctrigger.getCronExpression());
        assertTrue("CronTriggers should be volatile by default", ctrigger.isVolatile());

        CronTrigger midnightTrigger = (CronTrigger)triggers.get(3);

        assertEquals("MidnightCronTrigger", midnightTrigger.getName());
        assertEquals("FooJob", midnightTrigger.getJobName());
        assertEquals("0 0 0 * * ?", midnightTrigger.getCronExpression());
        assertTrue("CronTriggers should be volatile by default", midnightTrigger.isVolatile());
    }
}
