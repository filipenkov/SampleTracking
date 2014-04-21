package com.atlassian.jira.webtest.selenium.harness;

import com.atlassian.cargotestrunner.testrunner.TestRunner;
import com.atlassian.cargotestrunner.testrunner.TestRunnerConfig;
import com.atlassian.jira.functest.framework.CompositeSuiteListener;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.functest.framework.TomcatShutdownListener;
import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.webtest.selenium.JiraSeleniumConfiguration;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.SeleniumAcceptanceTestHarness;
import com.atlassian.jira.webtest.capture.FFMpegSuiteListener;
import com.atlassian.jira.webtests.cargo.CargoTestHarness;
import com.atlassian.jira.webtests.cargo.JIRACallbackFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.selenium.SeleniumConfiguration;
import com.atlassian.selenium.SeleniumStarter;
import com.google.common.base.Predicate;
import junit.framework.Test;
import junit.framework.TestResult;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class SeleniumTestHarness extends CargoTestHarness
{
    private static class SeleniumCategoryPredicate implements Predicate<WebTestDescription>
    {
        static SeleniumCategoryPredicate INSTANCE = new SeleniumCategoryPredicate();

        @Override
        public boolean apply(@Nullable WebTestDescription input)
        {
            return input.categories().contains(Category.SELENIUM_TEST) ||
                    JiraSeleniumTest.class.isAssignableFrom(input.testClass());
        }
    }

    public static Test suite() throws IOException
    {
        LocalTestEnvironmentData localEnvironmentData = new LocalTestEnvironmentData();
        Test test = SeleniumAcceptanceTestHarness.SUITE.createTest(localEnvironmentData);

        return makeSuiteFrom(localEnvironmentData, test);
    }

    public static Test makeSuiteFrom(LocalTestEnvironmentData localEnvironmentData, Test test) throws IOException
    {
        return suite(test, localEnvironmentData);
    }

    static Test suite(Test test, JIRAEnvironmentData localEnvironmentData) throws IOException
    {
        Properties containerProperties = getProperties();
        File war = initWarFile();

        return suite(war, containerProperties, localEnvironmentData, test);
    }

    static Test suite(File warLocation, Properties properties, JIRAEnvironmentData localEnvironmentData, Test testHarness) throws IOException
    {
        FuncTestOut.out.println("Constructing Integration test suite");

        Test test = TestRunner.suite(Collections.singletonList(testHarness), properties, new JIRACallbackFactory(URL_PREFIX),
                warLocation, new TestRunnerConfig(true, false, false));

        //We want to listen to the tests.
        test = new SuiteListenerWrapper(test, CompositeSuiteListener.of(new FFMpegSuiteListener(SeleniumCategoryPredicate.INSTANCE), new TomcatShutdownListener()));

        //We want to start the selenium server when we start the tests.
        test = new SeleniumWrapper(test, new JiraSeleniumConfiguration(localEnvironmentData));
        TestInformationKit.startTestSuite(test.countTestCases());

        return test;
    }

    private static class SeleniumWrapper implements Test
    {
        private final Test test;
        private final SeleniumConfiguration config;

        private SeleniumWrapper(final Test test, final SeleniumConfiguration config)
        {
            this.test = test;
            this.config = config;
        }

        public int countTestCases()
        {
            return test.countTestCases();
        }

        public void run(final TestResult testResult)
        {
            SeleniumStarter.getInstance().start(config);
            SeleniumStarter.getInstance().setManual(false);
            test.run(testResult);
            SeleniumStarter.getInstance().setManual(true);
            SeleniumStarter.getInstance().stop();
        }
    }
}
