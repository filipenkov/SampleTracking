package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Tests that when JIRA is not setup, the required error responses are sent
 * for various request URL patterns. For reasons that should be obvious, this
 * test is only valid if JIRA is not yet set up.
 */
@SystemTenantOnly
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.SETUP })
public class TestJohnsonFiltersWhileNotSetup extends JIRAWebTest
{

    private static final Logger log = Logger.getLogger(TestJohnsonFiltersWhileNotSetup.class);


    public TestJohnsonFiltersWhileNotSetup(String name)
    {
        super(name);
    }

    /**
     * Checks that the contentless 503 response is returned for a few URLs
     *
     * @throws IOException if web client does
     */
    public void test503Only() throws IOException
    {
        assert503Only("/rpc/trackback/fooobar");
        assert503Only("/si/whatever");
        assert503Only("/sr/whatever");
        assert503Only("/download/*");
        assert503Only("/plugins/servlet/*");
        assert503Only("/secure/attachment/blah-tricky.gif");
        assert503Only("/rest/some/rest");
        assert503Only("/rest/a");
    }

    /**
     * Checks that the setup page is shown when JIRA is not setup.
     */
    public void testNotSetupMessage()
    {
        beginAt("/browse/ABC-123");
        WebResponse webResponse = getDialog().getResponse();
        //We use a 200 response the braindead browser (IE) hides error status pages
        assertEquals("should be a 200 response", 200, webResponse.getResponseCode());
        assertTextPresent("JIRA Setup");
        assertTextPresent("Step 1 of 4");
    }

    private void assert503Only(String atUrl) throws IOException
    {
        try
        {
            beginAt(atUrl);
        }
        catch (RuntimeException re)
        {
            // if we get here, it's possible that we are running on java 1.5 +
            // and the server is websphere. in this combination (and only this
            // one AFAIK) the HttpUnit client receives a null from
            // HttpUrlConnection.getErrorStream() which, according to the JDK javadoc
            // is supposed to indicate that there is no error. It certainly means
            // nothing can be read from it!
            // Unfortunately, when HttpUnit then follows the getInputStream() call
            // tree it ends up with an IOException it didn't expect.
            // To add insult to injury, this IOException is not set as the cause
            // of the RuntimeException jwebunit wraps it in (probably because it
            // was written for jdk 1.3.)
            // So we just make some small efforts to ensure the basis of the
            // exception is the 503 we were hoping for.
            log.warn("Not able to properly assert the response code, using crude (websphere) workaround instead");
            assertTrue(re.getMessage().indexOf("IOException") != -1);
            assertTrue(re.getMessage().indexOf("503") != -1);

            return;
        }
        WebResponse webResponse = getDialog().getResponse();
        assertEquals("expected service unavailable response for url: '" + atUrl + "'", 503, webResponse.getResponseCode());
        assertEquals("should be no content for this url: '" + atUrl + "'", 0, webResponse.getText().length());
    }


    public void setUp()
    {
        log.info("not running normal test setup for " + getName());
        getTestContext().setBaseUrl(getEnvironmentData().getBaseUrl().toExternalForm());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);
    }


    public void tearDown()
    {
        log.info("not running normal test teardown for " + getName());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    }
}
