package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahServer;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahServer;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import junit.framework.TestSuite;

import javax.jms.JMSException;
import java.io.IOException;

public class JIRAHallelujahServer
{
    public static void main (String[] args) throws IOException
    {
        System.out.println("JIRA Hallelujah Server starting...");

        /* run all the tests */
        System.setProperty("jira.edition", "all");

        final TestSuite testSuite = (TestSuite) ((SuiteListenerWrapper) AcceptanceTestHarness.suite()).delegate();
        final String junitFilename = "TEST-Hallelujah.xml";
        final String suiteName = "AcceptanceTestHarness";

        HallelujahServer hallelujahServer = null;
        try
        {
            hallelujahServer = new JMSHallelujahServer.Builder()
                    .setJmsConfig(JIRAHallelujahConfig.getConfiguration())
                    .setSuite(testSuite)
                    .setTestResultFileName(junitFilename)
                    .setSuiteName(suiteName)
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .build();
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }

        hallelujahServer.run();

        System.out.println("JIRA Hallelujah Server finished.");
    }

}
