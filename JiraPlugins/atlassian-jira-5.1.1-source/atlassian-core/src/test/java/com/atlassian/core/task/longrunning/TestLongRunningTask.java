package com.atlassian.core.task.longrunning;

import junit.framework.TestCase;

import java.util.ResourceBundle;

public class TestLongRunningTask extends TestCase
{
    public void testElapsedTime() throws InterruptedException
    {
        AbstractLongRunningTask alrt = new AbstractLongRunningTask() {


            public void run()
            {
                try { super.run() ; Thread.sleep(100); stopTimer(); } catch (InterruptedException e) {}
            }

            protected ResourceBundle getResourceBundle()
            {
                return null;
            }

            public String getName()
            {
                return null;
            }
        };

        alrt.run();
        

        long elapsedTime = alrt.getElapsedTime();
        assertTrue(elapsedTime >= 100);

        // Test it's not still counting.
        Thread.sleep(100);
        assertEquals("counter has stopped", elapsedTime, alrt.getElapsedTime());
    }
}
