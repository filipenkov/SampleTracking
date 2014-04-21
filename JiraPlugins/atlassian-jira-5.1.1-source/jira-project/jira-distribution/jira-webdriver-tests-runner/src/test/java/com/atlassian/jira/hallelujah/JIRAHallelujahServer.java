package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.HallelujahServer;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseName;
import com.atlassian.buildeng.hallelujah.core.JUnitUtils;
import com.atlassian.buildeng.hallelujah.jms.JMSConnectionFactory.DeliveryMode;
import com.atlassian.buildeng.hallelujah.jms.JMSHallelujahServer;
import com.atlassian.buildeng.hallelujah.listener.SlowTestsListener;
import com.atlassian.buildeng.hallelujah.listener.TestRetryingServerListener;
import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;
import com.atlassian.jira.functest.framework.suite.WebTestRunners;
import com.atlassian.jira.functest.framework.suite.WebTestSuiteRunner;
import com.atlassian.jira.functest.framework.util.junit.DescriptionWalker;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.atlassian.webdriver.LifecycleAwareWebDriverGrid;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

import javax.jms.JMSException;
import java.io.File;
import java.util.List;

public class JIRAHallelujahServer
{
    // Feeling dirty now...
    private static class HackyWebDriverSuiteRunner extends WebTestSuiteRunner
    {

        public HackyWebDriverSuiteRunner(Class<?> webTestSuiteClass) throws InitializationError
        {
            super(webTestSuiteClass);
        }

        @Override
        protected Runner delegateRunner()
        {
            try
            {
                return WebTestRunners.newRunner(suite,
                        new AllDefaultPossibilitiesBuilder(true),
                        Predicates.<Description>alwaysTrue(), // HTFU, all tests are splittable!
                        testClasses.toArray(new Class<?>[testClasses.size()]));
            }
            catch (InitializationError initializationError)
            {
                throw new RuntimeException(initializationError);
            }
        }
        
        public List<Test> getTests()
        {
            final List<Test> tests = Lists.newArrayList();
            DescriptionWalker.walk(new Consumer<Description>()
            {
                @Override
                public void consume(@NotNull Description description)
                {
                    if (description.getClassName() != null && description.getMethodName() != null
                            && description.getAnnotation(org.junit.Test.class) != null)
                    {
                        final TestCaseName testCaseName = new TestCaseName(description.getClassName(), description.getMethodName());
                        tests.add(JUnitUtils.testFromTestCaseName(testCaseName));
                    }
                }
            },
            delegateRunner().getDescription());
            return tests;
        }
    }
    
    public static void main (String[] args) throws Exception
    {
        System.out.println("JIRA Hallelujah Server starting...");
        System.out.println(System.getProperties());


        final HackyWebDriverSuiteRunner hackyWebDriverSuiteRunner = new HackyWebDriverSuiteRunner(SystemPropertyBasedSuite.class);

        final List<Test> tests = hackyWebDriverSuiteRunner.getTests();
        final TestSuite testSuite = new TestSuite();
        for (final Test test : tests)
        {
            testSuite.addTest(test);
        }

        final String junitFilename = "TEST-Hallelujah.xml";
        final String suiteName = "WebDriverCargoTestHarness";

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

        // Why won't you die?
        LifecycleAwareWebDriverGrid.getCurrentDriver().quit();

    }

}
