package test.atlassian.scheduler;

import junit.framework.TestCase;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.atlassian.scheduler.SchedulerLauncher;

public class TestSchedulerLauncher extends TestCase
{
    public TestSchedulerLauncher(String s)
    {
        super(s);
    }

    public void testContextInitialized() throws SchedulerException
    {
        SchedulerLauncher launcher = new SchedulerLauncher();
        launcher.contextInitialized(null);

        Scheduler scheduler = launcher.getScheduler();

        assertTrue(!scheduler.isShutdown());

        assertEquals(1, scheduler.getJobNames(Scheduler.DEFAULT_GROUP).length);
        assertEquals("FooJob", scheduler.getJobNames(Scheduler.DEFAULT_GROUP)[0]);
        assertEquals(1, scheduler.getTriggerNames(Scheduler.DEFAULT_GROUP).length);
        assertEquals("SimpleTrigger", scheduler.getTriggerNames(Scheduler.DEFAULT_GROUP)[0]);

        launcher.contextDestroyed(null);
        assertTrue(scheduler.isShutdown());
    }
}
