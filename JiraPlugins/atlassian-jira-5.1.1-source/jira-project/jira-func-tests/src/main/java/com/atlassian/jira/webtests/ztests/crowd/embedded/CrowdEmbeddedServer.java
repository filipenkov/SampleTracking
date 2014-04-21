package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.crowd.acceptance.rest.RestServer;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.log.LogOnBothSides;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.AtlassianTenantFilter;
import com.sun.jersey.api.client.Client;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * This RestServer implementation is used for running Crowd's REST API acceptance tests against a JIRA instance in the
 * context of the JIRA func test framework.
 *
 * @since v4.3
 */
class CrowdEmbeddedServer implements RestServer
{
    /**
     * The name of the XML backup to use for testing Crowd's REST API. This backup mirrors the setup used in Crowd's own
     * acceptance tests.
     */
    static final String XML_BACKUP = "TestCrowdRestAPI.xml";

    /**
     * The configuration of the JIRA instance to run against.
     */
    private final JIRAEnvironmentData environmentData;

    /**
     * The test case
     */
    private final TestCase testCase;

    /**
     * An XML backup to restore from, or null.
     */
    private String xmlBackup = null;

    /**
     * Creates a new CrowdEmbeddedRestServer that runs against a local instance of JIRA.
     */
    public CrowdEmbeddedServer()
    {
        this(null, new LocalTestEnvironmentData());
    }

    /**
     * Creates a new CrowdEmbeddedRestServer with an externally-supplied JIRAEnvironmentData.
     *
     * @param testCase The test case that is using this server
     * @param environmentData a JIRAEnvironmentData
     */
    public CrowdEmbeddedServer(TestCase testCase, JIRAEnvironmentData environmentData)
    {
        this.testCase = testCase;
        this.environmentData = environmentData;
    }

    /**
     * Sets the given file as the XML backup to restore from.
     *
     * @param xmlBackup a String containing the name of an XML backup
     * @return this
     */
    public CrowdEmbeddedServer usingXmlBackup(String xmlBackup)
    {
        this.xmlBackup = xmlBackup;
        return this;
    }

    @Override
    public void before() throws Exception
    {
        if (xmlBackup != null)
        {
            // call setUp()
            FakeTestCase ftc = new FakeTestCase();
            ftc.setEnvironmentData(environmentData);
            Method setUp = TestCase.class.getDeclaredMethod("setUp", (Class[]) null);
            ReflectionUtils.makeAccessible(setUp);
            setUp.invoke(ftc, (Object[]) null); // call setUp

            // restore the XML
            ftc.restore(xmlBackup);
        }
    }

    @Override
    public void after()
    {
    }

    @Override
    public String getHost()
    {
        return environmentData.getBaseUrl().getHost();
    }

    @Override
    public int getPort()
    {
        return environmentData.getBaseUrl().getPort();
    }

    @Override
    public String getContextPath()
    {
        return environmentData.getContext();
    }

    @Override
    public Client decorateClient(Client client)
    {
        if (StringUtils.isNotBlank(environmentData.getTenant()))
        {
            client.addFilter(new AtlassianTenantFilter(environmentData.getTenant()));
        }
        return client;
    }

    /**
     * A "fake" test case, so that we can reuse the "XML Restore" functionality provided by that class.
     */
    static class FakeTestCase extends FuncTestCase
    {
        public void restore(String xmlBackup)
        {
            administration.restoreData(xmlBackup);
        }
    }
}
