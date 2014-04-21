package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahServer;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahServer;
import com.atlassian.buildeng.hallelujah.listener.SlowTestsListener;
import com.atlassian.buildeng.hallelujah.listener.TestRetryingServerListener;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import junit.framework.TestSuite;

import javax.jms.JMSException;
import java.io.File;

public class JIRAHallelujahServer
{

    public static void main (String[] args) throws Exception
    {
        System.out.println("JIRA Hallelujah Server starting...");
        System.out.println(System.getProperties());

        final LocalTestEnvironmentData localTestEnvironmentData = SeleniumAcceptanceTestHarness.getLocalTestEnvironmentData();
        final TestSuite testSuite = (TestSuite) ((SuiteListenerWrapper)SeleniumAcceptanceTestHarness.SUITE.createTest(localTestEnvironmentData)).delegate();
        final String junitFilename = "TEST-Hallelujah.xml";
        final String suiteName = "SeleniumTestHarness";

        HallelujahServer hallelujahServer = null;
        try
        {
            hallelujahServer = new JMSHallelujahServer.Builder()
                    .setJmsConfig(JIRAHallelujahConfig.getConfiguration())
                    .setSuite(testSuite)
                    .setTestResultFileName(junitFilename)
                    .setSuiteName(suiteName)
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .build()
                    .registerListeners(
                            new TestRetryingServerListener(1, new File("flakyTests.txt")),
                            new SlowTestsListener(20)
                    );
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }

        hallelujahServer.run();

        System.out.println("JIRA Hallelujah Server finished.");
    }

}
