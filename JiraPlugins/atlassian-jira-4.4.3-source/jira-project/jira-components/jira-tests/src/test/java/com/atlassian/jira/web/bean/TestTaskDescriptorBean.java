package com.atlassian.jira.web.bean;

import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.web.util.MockOutlookDate;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.task.TaskProgressIndicator;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.User;
import org.easymock.MockControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.13 */

public class TestTaskDescriptorBean extends ListeningTestCase
{
    private MockControl taskDescriptorControl;
    private TaskDescriptor taskDescriptor;

    private MockControl taskProgressIndicatorControl;
    private TaskProgressIndicator taskProgressIndicator;

    private I18nBean testI18nBean;
    private User testUser;
    private OutlookDateManager testDateManager;

    @Before
    public void setUp() throws Exception
    {
        taskDescriptorControl = MockControl.createControl(TaskDescriptor.class);
        taskDescriptor = (TaskDescriptor) taskDescriptorControl.getMock();

        taskProgressIndicatorControl = MockControl.createControl(TaskProgressIndicator.class);
        taskProgressIndicator = (TaskProgressIndicator) taskProgressIndicatorControl.getMock();

        testUser = TestTaskDescriptorBean.createUser("TestTaskDescriptorBean");
        testI18nBean = new MockI18nBean();
        testDateManager = new TestOutlookManager();
    }

    @After
    public void tearDown() throws Exception
    {
        taskDescriptor = null;
        taskDescriptorControl = null;
        testUser = null;
        testI18nBean = null;
        testDateManager = null;
    }

    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new TaskDescriptorBean(null, testI18nBean, testDateManager, testUser);
            fail("There should be a delegate bean.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }

        try
        {
            new TaskDescriptorBean(taskDescriptor, null, testDateManager, testUser);
            fail("There should be a testI18nBean.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }

        try
        {
            new TaskDescriptorBean(taskDescriptor, testI18nBean, null, testUser);
            fail("There should be a date maneger.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }

        inialiseMocks(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetTaskDescriptor() throws Exception
    {
        inialiseMocks(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        assertSame(taskDescriptor, bean.getTaskDescriptor());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    public void getFormattedElapsedRunTime() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getElapsedRunTime();
        taskDescriptorControl.setReturnValue(0);
        taskDescriptor.getElapsedRunTime();
        taskDescriptorControl.setReturnValue(50000);
        taskDescriptorControl.replay();

        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals("0 seconds", bean.getFormattedElapsedRunTime());
        assertEquals("50 seconds", bean.getFormattedElapsedRunTime());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();

    }

    @Test
    public void testGetResult() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getResult();
        taskDescriptorControl.setReturnValue(new Long(Long.MAX_VALUE));
        taskDescriptor.getResult();
        taskDescriptorControl.setThrowable(new ExecutionException("MyThrowable", new Exception()));

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(new Long(Long.MAX_VALUE), bean.getResult());
        try
        {
            bean.getResult();
            fail("The callable should throw an exception.");
        }
        catch (final ExecutionException e)
        {
            //expected.    
        }

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testIsStarted() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.isStarted();
        taskDescriptorControl.setReturnValue(true);
        taskDescriptor.isStarted();
        taskDescriptorControl.setReturnValue(false);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(true, bean.isStarted());
        assertEquals(false, bean.isStarted());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testIsFinished() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(true);
        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(false);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(true, bean.isFinished());
        assertEquals(false, bean.isFinished());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();

    }

    @Test
    public void testGetTaskId() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getTaskId();
        taskDescriptorControl.setReturnValue(new Long(Long.MAX_VALUE));
        taskDescriptor.getTaskId();
        taskDescriptorControl.setReturnValue(new Long(Long.MIN_VALUE));

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(new Long(Long.MAX_VALUE), bean.getTaskId());
        assertEquals(new Long(Long.MIN_VALUE), bean.getTaskId());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetStartedTimestamp() throws Exception
    {
        inialiseMocks(null);

        final Calendar cal = Calendar.getInstance();
        cal.set(2006, 11, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2005, 10, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        taskDescriptor.getStartedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptor.getStartedTimestamp();
        taskDescriptorControl.setReturnValue(date2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(date1, bean.getStartedTimestamp());
        assertEquals(date2, bean.getStartedTimestamp());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetStartedFinishedTimestamp() throws Exception
    {
        final Date date1 = new Date();

        inialiseMocks(null);

        taskDescriptor.getStartedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptorControl.setReturnValue(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        final OutlookDate date = testDateManager.getOutlookDate(testI18nBean.getLocale());
        assertEquals(date.format(date1), bean.getFormattedStartedTimestamp());
        assertEquals("", bean.getFormattedStartedTimestamp());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetFinishedTimestamp() throws Exception
    {
        inialiseMocks(null);

        final Calendar cal = Calendar.getInstance();
        cal.set(2006, 11, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2005, 10, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        taskDescriptor.getFinishedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptor.getFinishedTimestamp();
        taskDescriptorControl.setReturnValue(date2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(date1, bean.getFinishedTimestamp());
        assertEquals(date2, bean.getFinishedTimestamp());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetFormattedFinishedTimestamp() throws Exception
    {
        final Date date1 = new Date();

        inialiseMocks(null);

        taskDescriptor.getFinishedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptorControl.setReturnValue(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        final OutlookDate date = testDateManager.getOutlookDate(testI18nBean.getLocale());
        assertEquals(date.format(date1), bean.getFormattedFinishedTimestamp());
        assertEquals("", bean.getFormattedFinishedTimestamp());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetSubmittedTimestamp() throws Exception
    {
        inialiseMocks(null);

        final Calendar cal = Calendar.getInstance();
        cal.set(2006, 11, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2004, 10, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        taskDescriptor.getSubmittedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptor.getSubmittedTimestamp();
        taskDescriptorControl.setReturnValue(date2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(date1, bean.getSubmittedTimestamp());
        assertEquals(date2, bean.getSubmittedTimestamp());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetFormattedSubmittedTimestamp() throws Exception
    {
        final Date date1 = new Date();

        inialiseMocks(null);

        taskDescriptor.getSubmittedTimestamp();
        taskDescriptorControl.setReturnValue(date1);
        taskDescriptorControl.setReturnValue(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        final OutlookDate date = testDateManager.getOutlookDate(testI18nBean.getLocale());
        assertEquals(date.format(date1), bean.getFormattedSubmittedTimestamp());
    }

    @Test
    public void testGetElapsedRunTime() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getElapsedRunTime();
        taskDescriptorControl.setReturnValue(0L);
        taskDescriptor.getElapsedRunTime();
        taskDescriptorControl.setReturnValue(Long.MAX_VALUE);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(0L, bean.getElapsedRunTime());
        assertEquals(Long.MAX_VALUE, bean.getElapsedRunTime());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetUser() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getUser();
        taskDescriptorControl.setReturnValue(testUser);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(testUser, bean.getUser());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetDescription() throws Exception
    {
        inialiseMocks(null);

        final String description1 = "Description1";
        final String description2 = "Description2";

        taskDescriptor.getDescription();
        taskDescriptorControl.setReturnValue(description1);
        taskDescriptor.getDescription();
        taskDescriptorControl.setReturnValue(description2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(description1, bean.getDescription());
        assertEquals(description2, bean.getDescription());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetContext() throws Exception
    {
        inialiseMocks(null);

        final TestContext ctx1 = new TestContext();
        final TestContext ctx2 = new TestContext();

        taskDescriptor.getTaskContext();
        taskDescriptorControl.setReturnValue(ctx1);
        taskDescriptor.getTaskContext();
        taskDescriptorControl.setReturnValue(ctx2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(ctx1, bean.getTaskContext());
        assertEquals(ctx2, bean.getTaskContext());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetTaskProgressIndicator() throws Exception
    {
        inialiseMocks(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetFormattedProgress() throws Exception
    {
        final Date submittedDate = new Date();

        final SimpleTaskDescritpor descriptor = new SimpleTaskDescritpor();
        descriptor.setSubmittedTime(submittedDate);
        TaskDescriptorBean bean = new TaskDescriptorBean(descriptor, testI18nBean, testDateManager, testUser);

        //it should not have started.
        String expectedText = testI18nBean.getText("common.tasks.info.starting", bean.getFormattedSubmittedTimestamp());
        assertEquals(expectedText, bean.getFormattedProgress());

        //it should now be started.
        final Date startedDate = new Date();
        descriptor.setStartedTime(startedDate);
        expectedText = testI18nBean.getText("common.tasks.info.progress.unknown", bean.getFormattedElapsedRunTime());
        assertEquals(expectedText, bean.getFormattedProgress());

        //should finish without error.
        final Date finishedDate = new Date();
        descriptor.setFinishedTime(finishedDate);
        descriptor.setElapsedRunTime(1000);
        expectedText = testI18nBean.getText("common.tasks.info.completed", bean.getFormattedElapsedRunTime());

        assertEquals(expectedText, bean.getFormattedProgress());

        //should finish with error.
        bean.setExceptionCause(new Exception());
        expectedText = testI18nBean.getText("common.tasks.info.completed.with.error", bean.getFormattedElapsedRunTime());

        assertEquals(expectedText, bean.getFormattedProgress());

        final TaskProgressEvent lastEvent = new TaskProgressEvent(new Long(7), Long.MAX_VALUE, 43, "SubTask", "ThisIsAMessage");

        taskProgressIndicator.getLastProgressEvent();
        taskProgressIndicatorControl.setReturnValue(lastEvent);
        taskProgressIndicatorControl.replay();

        //lets check that progress is seen.
        descriptor.setTaskProgressIndicator(taskProgressIndicator);
        descriptor.setFinishedTime(null);

        bean = new TaskDescriptorBean(descriptor, testI18nBean, testDateManager, testUser);

        expectedText = testI18nBean.getText("common.tasks.info.progressing", String.valueOf(43), bean.getFormattedElapsedRunTime());
        assertEquals(expectedText, bean.getFormattedProgress());

        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetProgressWithBadProgress() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(new Long(7), Long.MAX_VALUE, Long.MAX_VALUE, "SubTask", "ThisIsAMessage");

        inialiseMocks(lastEvent);

        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(false, 2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        assertSame(lastEvent, bean.getLastProgressEvent());
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetProgressWithGoodProgress() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(new Long(7), Long.MAX_VALUE, 56, "SubTask", "ThisIsAMessage");

        inialiseMocks(lastEvent);

        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(false, 2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        assertSame(lastEvent, bean.getLastProgressEvent());
        assertEquals(56, bean.getProgressNumber());
        assertEquals(44, bean.getInverseProgressNumber());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetProgressWhenNoIndicator() throws Exception
    {
        taskDescriptor.getTaskProgressIndicator();
        taskDescriptorControl.setDefaultReturnValue(null);
        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(false, 2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetProgressWhenFinished() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.isFinished();
        taskDescriptorControl.setReturnValue(true, 2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testException() throws Exception
    {
        inialiseMocks(null);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        final Exception exception1 = new Exception("Exception1");
        final Exception exception2 = new Exception("Exception2");

        bean.setExceptionCause(exception1);
        assertSame(exception1, bean.getExceptionCause());
        assertNotNull(bean.getFormattedExceptionCause());

        bean.setExceptionCause(exception2);
        assertSame(exception2, bean.getExceptionCause());
        assertNotNull(bean.getFormattedExceptionCause());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();

    }

    @Test
    public void testIsUserWhoStartedTask() throws Exception
    {
        final User otherUser = TestTaskDescriptorBean.createUser("OtherUser");

        inialiseMocks(null);

        taskDescriptor.getUser();
        taskDescriptorControl.setReturnValue(testUser);
        taskDescriptor.getUser();
        taskDescriptorControl.setReturnValue(otherUser);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        assertTrue(bean.isUserWhoStartedTask());
        assertFalse(bean.isUserWhoStartedTask());

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetProgressUrl() throws Exception
    {
        final String url1 = "/root/dir/progress.action";
        final String url2 = "/IndexAdmin.action";

        inialiseMocks(null);

        taskDescriptor.getProgressURL();
        taskDescriptorControl.setReturnValue(url1);
        taskDescriptor.getProgressURL();
        taskDescriptorControl.setReturnValue(url2);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        String progressUrl = bean.getProgressURL();
        assertNotNull(progressUrl);
        assertTrue(progressUrl.indexOf(url1) != -1);

        progressUrl = bean.getProgressURL();
        assertNotNull(progressUrl);
        assertTrue(progressUrl.indexOf(url2) != -1);

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetUserUrl() throws Exception
    {
        inialiseMocks(null);

        taskDescriptor.getUser();
        taskDescriptorControl.setReturnValue(testUser, 4);

        taskDescriptorControl.replay();
        taskProgressIndicatorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);
        String usrUrl = bean.getUserURL();
        assertNotNull(usrUrl);
        assertTrue("URL '" + usrUrl + "' does not start with a /", usrUrl.startsWith("/"));

        usrUrl = bean.getUserURL();
        assertNotNull(usrUrl);
        assertTrue("URL '" + usrUrl + "' does not start with a /", usrUrl.startsWith("/"));

        taskDescriptorControl.verify();
        taskProgressIndicatorControl.verify();
    }

    @Test
    public void testGetLastProgressEvent() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(new Long(7), Long.MAX_VALUE, 56, "SubTask", "ThisIsAMessage");
        inialiseMocks(lastEvent);

        taskProgressIndicatorControl.replay();
        taskDescriptorControl.replay();

        final TaskDescriptorBean bean = new TaskDescriptorBean(taskDescriptor, testI18nBean, testDateManager, testUser);

        assertSame(lastEvent, bean.getLastProgressEvent());
    }

    private static User createUser(final String name)
    {
        final ProviderAccessor providerAccessorProxy = new MockProviderAccessor();
        return new User(name, providerAccessorProxy, new MockCrowdService());
    }

    private void inialiseMocks(final TaskProgressEvent event)
    {
        taskDescriptor.getTaskProgressIndicator();
        taskDescriptorControl.setDefaultReturnValue(taskProgressIndicator);

        taskProgressIndicator.getLastProgressEvent();
        taskProgressIndicatorControl.setReturnValue(event);
    }

    private static class TestContext implements TaskContext
    {
        public String buildProgressURL(final Long taskId)
        {
            return null;
        }
    }

    private static class SimpleTaskDescritpor implements TaskDescriptor
    {
        private Object result;
        private Date submittedTime;
        private Date startedTime;
        private Date finishedTime;
        private TaskProgressIndicator taskProgressIndicator;
        private String description;
        private Long taskId;
        private long elapsedRunTime;
        private TaskContext taskContext;

        public SimpleTaskDescritpor()
        {
            clear();
        }

        public void clear()
        {
            elapsedRunTime = 0;
            taskId = null;
            result = null;
            submittedTime = null;
            startedTime = null;
            finishedTime = null;
            taskProgressIndicator = null;
            description = null;
            taskContext = null;
        }

        public Object getResult()
        {
            return result;
        }

        public boolean isStarted()
        {
            return startedTime != null;
        }

        public boolean isFinished()
        {
            return finishedTime != null;
        }

        public Long getTaskId()
        {
            return taskId;
        }

        public Date getStartedTimestamp()
        {
            return startedTime;
        }

        public Date getFinishedTimestamp()
        {
            return finishedTime;
        }

        public Date getSubmittedTimestamp()
        {
            return submittedTime;
        }

        public long getElapsedRunTime()
        {
            return elapsedRunTime;
        }

        public User getUser()
        {
            return null;
        }

        public String getDescription()
        {
            return description;
        }

        public TaskContext getTaskContext()
        {
            return taskContext;
        }

        public String getProgressURL()
        {
            return "/userUrl?user=";
        }

        public TaskProgressIndicator getTaskProgressIndicator()
        {
            return taskProgressIndicator;
        }

        public void setResult(final Object result)
        {
            this.result = result;
        }

        public void setSubmittedTime(final Date submittedTime)
        {
            this.submittedTime = submittedTime;
        }

        public void setStartedTime(final Date startedTime)
        {
            this.startedTime = startedTime;
        }

        public void setFinishedTime(final Date finishedTime)
        {
            this.finishedTime = finishedTime;
        }

        public void setTaskProgressIndicator(final TaskProgressIndicator taskProgressIndicator)
        {
            this.taskProgressIndicator = taskProgressIndicator;
        }

        public void setDescription(final String description)
        {
            this.description = description;
        }

        public void setTaskId(final Long taskId)
        {
            this.taskId = taskId;
        }

        public void setElapsedRunTime(final long elapsedRunTime)
        {
            this.elapsedRunTime = elapsedRunTime;
        }

        public void setTaskContext(final TaskContext taskContext)
        {
            this.taskContext = taskContext;
        }
    }

    private static class TestOutlookDate extends MockOutlookDate
    {
        private final DateFormat format;

        public TestOutlookDate(final Locale locale)
        {
            super(locale);

            format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        }

        @Override
        public String format(final Date date)
        {
            return format.format(date);
        }

        @Override
        public void flushCache()
        {
        //do nothing.
        }
    }

    private static class TestOutlookManager implements OutlookDateManager
    {

        public void refresh()
        {}

        public OutlookDate getOutlookDate(final Locale locale)
        {
            return new TestOutlookDate(locale);
        }
    }
}
