package it.com.atlassian.jira.webtest;

import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;

import java.io.File;
import java.util.Properties;

/**
 * Base class for JIRA integration tests.
 *
 * @since v4.3
 */
public abstract class IntegrationTest extends JiraSeleniumTest
{
    /**
     * The path for the integration tests (relative to the project root).
     */
    private static final String INTEGRATION_TESTS_RELATIVE_PATH = "jira-distribution/jira-integration-tests";

    /**
     * The path for the XML files within this project.
     */
    private static final String SRC_TEST_XML = String.format("src%stest%sxml", File.separator, File.separator);

    /**
     * The name of the system property that is used to override the properties used during test runs.
     */
    private static final String PROPERTIES_FILENAME_PROP = "test.server.properties";

    /**
     * The properties for the test run.
     */
    private static Properties PROPS;

    public IntegrationTest()
    {
        super(new IntegrationEnvironmentData());
        initProperties();
    }

    /**
     * Returns the base URL for the Refapp as a String.
     *
     * @return a String containing the base URL for the Refapp
     */
    public String getRefappBaseUrl()
    {
        String host = PROPS.getProperty("refapp.host");
        String port = PROPS.getProperty("refapp.http.port");
        String contextPath = PROPS.getProperty("refapp.context.path");

        return String.format("http://%s:%s%s", host, port, contextPath);
    }

    /**
     * Loads the integration test properties from a file.
     */
    private static synchronized void initProperties()
    {
        if (PROPS == null)
        {
            PROPS = LocalTestEnvironmentData.loadProperties(PROPERTIES_FILENAME_PROP, "integration.default.properties");
        }
    }

    /**
     * Returns a File pointing to the XML data directory. First tries to use "src/test/xml", and if that's not found it
     * returns "{@value #INTEGRATION_TESTS_RELATIVE_PATH}/src/test/xml". The test harness code is such a mess already
     * that i don't feel at all guilty about adding this hack.
     *
     * @return a File
     */
    private static File getXmlDataLocation()
    {
        File file = new File(SRC_TEST_XML);
        if (file.exists() && file.isDirectory())
        {
            return file;
        }

        // damn you IDEA for making me do this!!!
        return new File(INTEGRATION_TESTS_RELATIVE_PATH + File.separator + SRC_TEST_XML);
    }

    static class IntegrationEnvironmentData extends LocalTestEnvironmentData
    {
        @Override
        public File getXMLDataLocation()
        {
            return getXmlDataLocation();
        }
    }
}
