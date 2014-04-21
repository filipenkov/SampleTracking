package it.com.atlassian.jira.webtest;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.webtest.selenium.harness.JiraSeleniumTestSuite;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.junit.Ignore;

import java.util.Properties;
import java.util.Set;

/**
 * Test harness for integration tests.
 *
 * @since v4.3
 */
public class IntegrationAcceptanceTestHarness extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new IntegrationAcceptanceTestHarness();
    public static final String SELENIUM_PROPERTY_LOCATION = "jira.functest.seleniumproperties";
    private static final String DEFAULT_SELENIUMTEST_PROPERTIES = "integration.seleniumtest.properties";
    private final Logger log = Logger.getLogger(IntegrationAcceptanceTestHarness.class);

    public static Test suite()
    {
        final LocalTestEnvironmentData environmentData = getLocalTestEnvironmentData();
        final TestSuite suite = new JiraSeleniumTestSuite(environmentData);
        final Set<Class<? extends TestCase>> tests = SUITE.getTests(environmentData);

        for (final Class test : tests)
        {
            suite.addTestSuite(test);
        }
        TestInformationKit.startTestSuite(suite.countTestCases());
        return suite;
    }

    private IntegrationAcceptanceTestHarness()
    {
        // add all the non-ignored tests
        addTests(Collections2.filter(TestClassUtils.getJUnit3TestClasses("it.com.atlassian.jira.webtest"), new Predicate<Class<? extends TestCase>>()
        {
            @Override
            public boolean apply(Class<? extends TestCase> testCase)
            {
                Ignore ignore = testCase.getAnnotation(Ignore.class);
                if (ignore != null)
                {
                    log.warn(String.format("IGNORED %s (%s)", testCase.getName(), ignore.value()));
                    return false;
                }

                return true;
            }
        }));
    }

    /**
     * @return The default setup of the LocalTestEnvironmentData
     */
    public static LocalTestEnvironmentData getLocalTestEnvironmentData()
    {
        //this is a bit of a hack to pick up the xml data location from the integration.seleniumtest.properties file.
        return new LocalTestEnvironmentData(getSeleniumProperties().getProperty("jira.xml.data.location"));
    }

    private static Properties getSeleniumProperties()
    {
        return LocalTestEnvironmentData.loadProperties(SELENIUM_PROPERTY_LOCATION, DEFAULT_SELENIUMTEST_PROPERTIES);
    }
}
