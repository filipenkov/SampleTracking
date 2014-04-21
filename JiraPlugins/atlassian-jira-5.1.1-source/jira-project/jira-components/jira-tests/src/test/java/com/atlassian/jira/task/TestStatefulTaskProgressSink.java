package com.atlassian.jira.task;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;

import java.util.Collection;
import java.util.Iterator;

/** @since v3.13 */
public class TestStatefulTaskProgressSink extends ListeningTestCase
{
    private static final String MESSAGE_A = "MessageA";
    private static final String SUB_TASK_A = "SubTaskA";
    private static final String SUB_TASK_B = "SubTaskB";
    private static final String MESSAGE_B = "MessageB";

    @Test
    public void testConstruction()
    {
        StatefulTaskProgressSink stateSink = new StatefulTaskProgressSink(0, 100, 50, TaskProgressSink.NULL_SINK);
        assertEquals(0, stateSink.getMinProgress());
        assertEquals(50, stateSink.getProgress());
        assertEquals(100, stateSink.getMaxProgress());

        stateSink = new StatefulTaskProgressSink(10, 100, -10, TaskProgressSink.NULL_SINK);
        assertEquals(10, stateSink.getMinProgress());
        assertEquals(10, stateSink.getProgress());
        assertEquals(100, stateSink.getMaxProgress());

        stateSink = new StatefulTaskProgressSink(-50, -20, -5, TaskProgressSink.NULL_SINK);
        assertEquals(-50, stateSink.getMinProgress());
        assertEquals(-20, stateSink.getProgress());
        assertEquals(-20, stateSink.getMaxProgress());

        stateSink = new StatefulTaskProgressSink(-50, -50, -50, TaskProgressSink.NULL_SINK);
        assertEquals(-50, stateSink.getMinProgress());
        assertEquals(-50, stateSink.getProgress());
        assertEquals(-50, stateSink.getMaxProgress());

        stateSink = new StatefulTaskProgressSink(-50, 100, TaskProgressSink.NULL_SINK);
        assertEquals(-50, stateSink.getMinProgress());
        assertEquals(-50, stateSink.getProgress());
        assertEquals(100, stateSink.getMaxProgress());

        stateSink = new StatefulTaskProgressSink(99, 99, TaskProgressSink.NULL_SINK);
        assertEquals(99, stateSink.getMinProgress());
        assertEquals(99, stateSink.getProgress());
        assertEquals(99, stateSink.getMaxProgress());
    }

    @Test
    public void testNullSink4ArgCtor()
    {
        //lets try some invalid constructions.
        try
        {
            new StatefulTaskProgressSink(0, 100, 50, null);
            fail("IllegalArgumentException should be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            //exception is expected.
        }
    }

    @Test
    public void testNullSink3ArgCtor()
    {
        try
        {
            new StatefulTaskProgressSink(0, 100, null);
            fail("NullPointerException should be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            //exception is expected.
        }
}

    @Test
    public void testMinGreaterThanMax4ArgCtor()
    {
        try
        {
            new StatefulTaskProgressSink(-1, -20, -5, TaskProgressSink.NULL_SINK);
        }
        catch (IllegalArgumentException e)
        {
            //exception is expected.
        }
    }

    @Test
    public void testMinGreaterThanMax3ArgCtor()
    {
        try
        {
            new StatefulTaskProgressSink(-19, -20, TaskProgressSink.NULL_SINK);
        }
        catch (IllegalArgumentException e)
        {
            //exception is expected.
        }
    }

    @Test
    public void testMakeProgress()
    {
        MockControl control = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) control.getMock();

        mockSink.makeProgress(10, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(10, SUB_TASK_A, null);
        mockSink.makeProgress(20, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(15, SUB_TASK_B, MESSAGE_A);
        mockSink.makeProgress(0, null, null);
        mockSink.makeProgress(100, null, MESSAGE_A);
        mockSink.makeProgress(0, null, MESSAGE_B);
        mockSink.makeProgress(100, SUB_TASK_B, null);

        control.replay();

        StatefulTaskProgressSink sink = new StatefulTaskProgressSink(0, 100, mockSink);
        assertEquals(0, sink.getMinProgress());
        assertEquals(0, sink.getProgress());
        assertEquals(100, sink.getMaxProgress());

        sink.makeProgress(10, SUB_TASK_A, MESSAGE_A);
        assertEquals(10, sink.getProgress());
        sink.makeProgress(SUB_TASK_A, null);
        assertEquals(10, sink.getProgress());
        sink.makeProgressIncrement(10, SUB_TASK_B, MESSAGE_B);
        assertEquals(20, sink.getProgress());
        sink.makeProgressIncrement(-5, SUB_TASK_B, MESSAGE_A);
        assertEquals(15, sink.getProgress());
        sink.makeProgress(-5, null, null);
        assertEquals(0, sink.getProgress());
        sink.makeProgress(Long.MAX_VALUE, null, MESSAGE_A);
        assertEquals(100, sink.getProgress());
        sink.makeProgressIncrement(Long.MIN_VALUE, null, MESSAGE_B);
        assertEquals(0, sink.getProgress());
        sink.makeProgressIncrement(Long.MAX_VALUE, SUB_TASK_B, null);
        assertEquals(100, sink.getProgress());

        control.verify();

        control.reset();
        mockSink.makeProgress(5, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(5, SUB_TASK_A, null);
        mockSink.makeProgress(5, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(5, SUB_TASK_B, MESSAGE_A);
        mockSink.makeProgress(5, null, null);
        mockSink.makeProgress(5, null, MESSAGE_A);
        mockSink.makeProgress(5, null, MESSAGE_B);
        mockSink.makeProgress(5, SUB_TASK_B, null);
        control.replay();

        sink = new StatefulTaskProgressSink(5, 5, mockSink);

        sink.makeProgress(10, SUB_TASK_A, MESSAGE_A);
        assertEquals(5, sink.getProgress());
        sink.makeProgress(SUB_TASK_A, null);
        assertEquals(5, sink.getProgress());
        sink.makeProgressIncrement(10, SUB_TASK_B, MESSAGE_B);
        assertEquals(5, sink.getProgress());
        sink.makeProgressIncrement(-5, SUB_TASK_B, MESSAGE_A);
        assertEquals(5, sink.getProgress());
        sink.makeProgress(-5, null, null);
        assertEquals(5, sink.getProgress());
        sink.makeProgress(Long.MAX_VALUE, null, MESSAGE_A);
        assertEquals(5, sink.getProgress());
        sink.makeProgressIncrement(Long.MIN_VALUE, null, MESSAGE_B);
        assertEquals(5, sink.getProgress());
        sink.makeProgressIncrement(Long.MAX_VALUE, SUB_TASK_B, null);
        assertEquals(5, sink.getProgress());
    }

    @Test
    public void testCreateActionView()
    {
        MockControl control = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) control.getMock();

        //make sure it steps correctly 0, 5, 10, 15, 20

        for (int i = 0; i <= 20; i += 5)
        {
            mockSink.makeProgress(i, SUB_TASK_A, MESSAGE_A);
        }

        control.replay();

        StatefulTaskProgressSink statefulSink = new StatefulTaskProgressSink(0, 100, mockSink);
        TaskProgressSink taskSink = statefulSink.createStepSinkView(0, 20, 4);

        for (int i = 0; i <= 4; i++)
        {
            taskSink.makeProgress(i, SUB_TASK_A, MESSAGE_A);
            assertEquals(i * 5, statefulSink.getProgress());
        }

        control.verify();

        //make sure it steps correctly 40, 45, 50

        control.reset();

        for (int i = 40; i <= 50; i += 5)
        {
            mockSink.makeProgress(i, SUB_TASK_B, null);
        }

        mockSink.makeProgress(45, SUB_TASK_A, MESSAGE_B);

        control.replay();

        taskSink = statefulSink.createStepSinkView(40, 10, 2);

        for (int i = 0; i <= 2; i++)
        {
            taskSink.makeProgress(i, SUB_TASK_B, null);
            assertEquals(40 + 5 * i, statefulSink.getProgress());
        }

        taskSink.makeProgress(1, SUB_TASK_A, MESSAGE_B);
        assertEquals(45, statefulSink.getProgress());

        control.verify();

        //make sure it steps correctly from 0, 33, 67, 100

        control.reset();

        mockSink.makeProgress(0, SUB_TASK_B, null);
        mockSink.makeProgress(33, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(67, null, MESSAGE_B);
        mockSink.makeProgress(100, null, null);

        control.replay();

        taskSink = statefulSink.createStepSinkView(-50, Long.MAX_VALUE, 3);

        taskSink.makeProgress(0, SUB_TASK_B, null);
        assertEquals(0, statefulSink.getProgress());
        taskSink.makeProgress(1, SUB_TASK_B, MESSAGE_B);
        assertEquals(33, statefulSink.getProgress());
        taskSink.makeProgress(2, null, MESSAGE_B);
        assertEquals(67, statefulSink.getProgress());
        taskSink.makeProgress(3, null, null);
        assertEquals(100, statefulSink.getProgress());

        control.verify();

        //make sure it steps correctly from -10, 0, 10

        control.reset();

        mockSink.makeProgress(-10, SUB_TASK_B, null);
        mockSink.makeProgress(0, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(10, null, MESSAGE_B);

        control.replay();

        statefulSink = new StatefulTaskProgressSink(-10, Long.MAX_VALUE, mockSink);
        taskSink = statefulSink.createStepSinkView(-10, 20, 2);

        taskSink.makeProgress(0, SUB_TASK_B, null);
        assertEquals(-10, statefulSink.getProgress());
        taskSink.makeProgress(1, SUB_TASK_B, MESSAGE_B);
        assertEquals(0, statefulSink.getProgress());
        taskSink.makeProgress(2, null, MESSAGE_B);
        assertEquals(10, statefulSink.getProgress());

        control.verify();

        //make sure it steps correcly from 10, 15, 20

        control.reset();

        mockSink.makeProgress(10, SUB_TASK_B, null);
        mockSink.makeProgress(15, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(20, null, MESSAGE_B);

        control.replay();

        taskSink = statefulSink.createStepSinkView(10, 2);

        taskSink.makeProgress(0, SUB_TASK_B, null);
        assertEquals(10, statefulSink.getProgress());
        taskSink.makeProgress(1, SUB_TASK_B, MESSAGE_B);
        assertEquals(15, statefulSink.getProgress());
        taskSink.makeProgress(2, null, MESSAGE_B);
        assertEquals(20, statefulSink.getProgress());

        //make sure it steps correcly for length of 0.

        control.reset();

        mockSink.makeProgress(20, SUB_TASK_B, null);
        mockSink.makeProgress(20, SUB_TASK_B, MESSAGE_B);
        mockSink.makeProgress(20, null, MESSAGE_B);

        control.replay();

        taskSink = statefulSink.createStepSinkView(0, 3);

        taskSink.makeProgress(0, SUB_TASK_B, null);
        assertEquals(20, statefulSink.getProgress());
        taskSink.makeProgress(1, SUB_TASK_B, MESSAGE_B);
        assertEquals(20, statefulSink.getProgress());
        taskSink.makeProgress(2, null, MESSAGE_B);
        assertEquals(20, statefulSink.getProgress());


        control.verify();

        //lets check some error conditions.
        try
        {
            taskSink = statefulSink.createStepSinkView(-20, -2, 2);
            fail("Negative length should not be accepted.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            taskSink = statefulSink.createStepSinkView(10, 2, -5);
            fail("Negative steps should be accepted.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testCreatePercentageSinksForRange()
    {
        MockControl control = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) control.getMock();

        for (int i = 0; i < 10; i++)
        {
            mockSink.makeProgress(10 * i, SUB_TASK_A, MESSAGE_A);
            mockSink.makeProgress(10 * (i + 1), SUB_TASK_A, MESSAGE_B);
        }

        control.replay();

        Collection sinks = StatefulTaskProgressSink.createPercentageSinksForRange(0, 100, 10, mockSink);
        assertEquals(10, sinks.size());

        for (Iterator iterator = sinks.iterator(); iterator.hasNext();)
        {
            StatefulTaskProgressSink statefulTaskProgressSink = (StatefulTaskProgressSink) iterator.next();
            statefulTaskProgressSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
            statefulTaskProgressSink.makeProgress(100, SUB_TASK_A, MESSAGE_B);
        }

        control.verify();

        //does it work with zero divisions?
        sinks = StatefulTaskProgressSink.createPercentageSinksForRange(50, 51, 0, mockSink);
        assertTrue(sinks.isEmpty());

        //does it work the start and end the same.
        control.reset();
        for (int i = 0; i < 5; i++)
        {
            mockSink.makeProgress(-2, SUB_TASK_A, MESSAGE_A);
            mockSink.makeProgress(-2, SUB_TASK_B, MESSAGE_B);
        }

        control.replay();

        sinks = StatefulTaskProgressSink.createPercentageSinksForRange(-2, -2, 5, mockSink);
        assertEquals(5, sinks.size());

        for (Iterator iterator = sinks.iterator(); iterator.hasNext();)
        {
            StatefulTaskProgressSink statefulTaskProgressSink = (StatefulTaskProgressSink) iterator.next();
            statefulTaskProgressSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
            statefulTaskProgressSink.makeProgress(100, SUB_TASK_B, MESSAGE_B);
        }

        control.verify();
    }

    @Test
    public void testNullSinkFactoryMethod()
    {
        try
        {
            StatefulTaskProgressSink.createPercentageSinksForRange(0, 100, 10, null);
            fail("IllegalArgumentEx should be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }
    }

    @Test
    public void testNegativeDivisionsFactoryMethod()
    {
        try
        {
            StatefulTaskProgressSink.createPercentageSinksForRange(0, 100, -1, TaskProgressSink.NULL_SINK);
            fail("An IllegalArgumentException should be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }
    }

    @Test
    public void testStartRangeGreaterThanEndRangeFactoryMethod()
    {
        try
        {
            StatefulTaskProgressSink.createPercentageSinksForRange(0, -10, -1, TaskProgressSink.NULL_SINK);
            fail("An IllegalArgumentException should be thrown.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }
    }
}
