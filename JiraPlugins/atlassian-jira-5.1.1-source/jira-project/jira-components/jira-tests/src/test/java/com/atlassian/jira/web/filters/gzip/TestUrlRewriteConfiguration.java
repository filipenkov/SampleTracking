package com.atlassian.jira.web.filters.gzip;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.tuckey.web.MockRequest;
import org.tuckey.web.MockResponse;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple test case to determine if the rewrite configuration works as
 * expected.
 * <p>
 * Test idea stolen from http://sujitpal.blogspot.com/search/label/junit
 */
public class TestUrlRewriteConfiguration extends ListeningTestCase
{

    private static final Logger log = Logger.getLogger(TestUrlRewriteConfiguration.class);
    private static final LazyReference<Conf> confMain = new LazyReference<Conf>()
    {
        //loading the config is slow, and doesn't need to happen every test.  Make it static
        @Override
        protected Conf create() throws Exception
        {
//            org.tuckey.utils.Log.setLevel("DEBUG"); // comment this in if you want to see urlrewrite's log messages

            try
            {
                return loadConf("../jira-webapp/src/main/webapp/WEB-INF/urlrewrite.xml");
            }
            catch (FileNotFoundException e)
            {
                // when running in IDEA the path is a bit different
                return loadConf("jira-components/jira-webapp/src/main/webapp/WEB-INF/urlrewrite.xml");
            }
        }
    };

    @Test
    public void testRewriteForKickAss() throws Exception
    {
        String fromUrl = "/issues";
        String toUrl = "issues/";
        assertRewriteSuccess(fromUrl, toUrl, confMain.get());
    }

    private static Conf loadConf(String path) throws FileNotFoundException
    {
        FileInputStream inputStream = new FileInputStream(path);
        try
        {
            return new Conf(inputStream, null);
        }
        finally
        {
            try {
                inputStream.close();
            }
            catch (IOException e)
            {
                log.error("Failed to close: " + inputStream, e);
            }
        }
    }

    /**
     * Assertion to rewrite the URL using the UrlRewriteFilter and verify
     * that fromUrl is rewritten to toUrl using rewriting rules in confMain.
     *
     * @param fromUrl the URL to be rewritten from.
     * @param toUrl   the URL to be rewritten to.
     * @param conf    the UrlRewriteFilter configuration.
     * @throws Exception if one is thrown.
     */
    private void assertRewriteSuccess(String fromUrl, String toUrl, Conf conf) throws Exception
    {
        UrlRewriter rewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest(fromUrl);
        MockResponse response = new MockResponse();
        RewrittenUrl rewrittenUrl = rewriter.processRequest(request, response);
        assertNotNull("Could not rewrite URL from:" + fromUrl + " to:" + toUrl, rewrittenUrl);
        String rewrittenUrlString = rewrittenUrl.getTarget();
        log.debug("URL Rewrite from:[" + fromUrl + "] to [" + rewrittenUrlString + "]");
        assertEquals("Rewrite from:" + fromUrl + " to:" + toUrl + " did not succeed", toUrl, rewrittenUrlString);
    }
}

