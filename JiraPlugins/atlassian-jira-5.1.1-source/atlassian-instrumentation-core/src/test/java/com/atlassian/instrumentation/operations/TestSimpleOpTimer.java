package com.atlassian.instrumentation.operations;

import junit.framework.TestCase;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.SecureRandom;
import java.util.zip.CRC32;

/**
 * A test case for SimpleOpTimer
 *
 * @since v4.0
 */
public class TestSimpleOpTimer extends TestCase
{
    public void testConstruction()
    {
        SimpleOpTimer opTimer = new SimpleOpTimer("name");
        assertEquals("name", opTimer.getName());
        assertNotNull(opTimer.snapshot());
    }

    public void testEndMechanisms()
    {
        SimpleOpTimer opTimer = new SimpleOpTimer("name");
        OpSnapshot snapshot = opTimer.end();
        assertNotNull(snapshot);
        assertEquals(0, snapshot.getResultSetSize());

        opTimer = new SimpleOpTimer("name");
        snapshot = opTimer.end(666);
        assertNotNull(snapshot);
        assertEquals(666, snapshot.getResultSetSize());

        opTimer = new SimpleOpTimer("name");
        snapshot = opTimer.end(new OpTimer.HeisenburgResultSetCalculator()
        {
            public long calculate()
            {
                return 777;
            }
        });
        assertNotNull(snapshot);
        assertEquals(777, snapshot.getResultSetSize());

        opTimer = new SimpleOpTimer("name");
        snapshot = opTimer.end(null);
        assertNotNull(snapshot);
        assertEquals(0, snapshot.getResultSetSize());

        // now they cant call end twice
        try
        {
            opTimer.end();
            fail("barfarama");
        }
        catch (IllegalStateException ignored)
        {
        }
    }

    public void testStableSnapShot() throws InterruptedException
    {
        SimpleOpTimer opTimer = new SimpleOpTimer("name");
        Thread.sleep(100);

        OpSnapshot snapshot1 = opTimer.snapshot();
        assertNotNull(snapshot1);
        assertTrue(snapshot1.getMillisecondsTaken() > 0);
        assertEquals(1, snapshot1.getInvocationCount());
        assertEquals(0, snapshot1.getResultSetSize());


        Thread.sleep(100);
        OpSnapshot snapshot2 = opTimer.snapshot();

        assertNotNull(snapshot2);
        assertTrue(snapshot2.getMillisecondsTaken() > 0);
        assertEquals(1, snapshot2.getInvocationCount());
        assertEquals(0, snapshot2.getResultSetSize());
        assertNotSame(snapshot1, snapshot2);
        assertFalse(snapshot1.equals(snapshot2));

        Thread.sleep(100);
        OpSnapshot snapshot3 = opTimer.end(666);
        assertNotNull(snapshot3);
        assertTrue(snapshot3.getMillisecondsTaken() > 0);
        assertEquals(1, snapshot3.getInvocationCount());
        assertEquals(666, snapshot3.getResultSetSize());
        assertNotSame(snapshot2, snapshot3);
        assertFalse(snapshot2.equals(snapshot3));

        Thread.sleep(100);
        OpSnapshot snapshot4 = opTimer.snapshot();
        assertNotNull(snapshot4);
        assertSame(snapshot3, snapshot4);
    }

    public void testCpuRecording()
    {
        final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
        if (!threadMX.isCurrentThreadCpuTimeSupported())
        {
            return;
        }
        SimpleOpTimer opTimer = new SimpleOpTimer("name");

        doSomeCpuIntensiveWork();

        OpSnapshot snapshot1 = opTimer.snapshot();
        assertNotNull(snapshot1);
        //
        // is this really reliable?  Will more than 0 CPU be used.  Stay tuned....
        assertTrue(snapshot1.getCpuTime() > 0);
    }

    private void doSomeCpuIntensiveWork()
    {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int k = 0; k < 10000; k++)
        {
            for (int i = 0; i < s.length(); i++)
            {
                final SecureRandom random = new SecureRandom();
                random.nextGaussian();
                
                String sub = s.substring(i);
                final CRC32 crc32 = new CRC32();
                crc32.update(sub.getBytes());
                crc32.getValue();
            }
        }
    }

}
