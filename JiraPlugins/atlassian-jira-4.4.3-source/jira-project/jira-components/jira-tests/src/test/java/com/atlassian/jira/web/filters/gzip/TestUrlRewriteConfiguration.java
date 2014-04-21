package com.atlassian.jira.web.filters.gzip;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.apache.log4j.Logger;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Simple test case to determine if the rewrite configuration works as
 * expected.
 * <p>
 * Test idea stolen from http://sujitpal.blogspot.com/search/label/junit
 */
public class TestUrlRewriteConfiguration extends ListeningTestCase
{

    private static final Logger log = Logger.getLogger(TestUrlRewriteConfiguration.class);
    private static final String MAIN_URLREWRITE_XML = "../jira-webapp/src/main/webapp/WEB-INF/urlrewrite.xml";
    private static Conf confMain; //loading the config is slow, and doesn't need to happen every test.  Make it static

    static {
//        org.tuckey.utils.Log.setLevel("DEBUG"); // comment this in if you want to see urlrewrite's log messages

        try
        {
            File file = new File(MAIN_URLREWRITE_XML);
            if (!file.exists())
                throw new RuntimeException("Could not find file '" + MAIN_URLREWRITE_XML + "'");

            confMain = new Conf(new FileInputStream(file), null);

        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testRewriteForSingleIssue() throws Exception
    {
        String fromUrl = "/browse/JRA-1?view=rss";
        String toUrl = "../si/jira.issueviews:issue-xml/JRA-1/JRA-1.xml";
        assertRewriteSuccess(fromUrl, toUrl, confMain);
    }


    /**
     * At some point (JIRA 3.3 I think), we changed the format for our request parameters.  If anyone had bookmarked a request, we want to rewrite it for them.
     * @throws Exception
     */
    @Test
    public void testInvalidUpdatedPreviousDatePeriods() throws Exception
    {
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&updatedPrevious=3600000&summary=abc", "IssueNavigator.jspa?pid=10&updated%3Aprevious=-1h&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&updatedPrevious=86400000&summary=abc", "IssueNavigator.jspa?pid=10&updated%3Aprevious=-1d&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&updatedPrevious=604800000&summary=abc", "IssueNavigator.jspa?pid=10&updated%3Aprevious=-1w&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&updatedPrevious=2592000000&summary=abc", "IssueNavigator.jspa?pid=10&updated%3Aprevious=-30d&summary=abc", confMain);
    }

    @Test
    public void testInvalidCreatedPreviousDatePeriods() throws Exception
    {
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&createdPrevious=3600000&summary=abc", "IssueNavigator.jspa?pid=10&created%3Aprevious=-1h&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&createdPrevious=86400000&summary=abc", "IssueNavigator.jspa?pid=10&created%3Aprevious=-1d&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&createdPrevious=604800000&summary=abc", "IssueNavigator.jspa?pid=10&created%3Aprevious=-1w&summary=abc", confMain);
        assertRewriteSuccess("/secure/IssueNavigator.jspa?pid=10&createdPrevious=2592000000&summary=abc", "IssueNavigator.jspa?pid=10&created%3Aprevious=-30d&summary=abc", confMain);
    }

    /**
     * Test for http://jira.atlassian.com/browse/JRA-12469
     * @throws Exception
     */
    @Test
    public void testUrlRewriteForPagesHandlesSuffixes() throws Exception
    {
        assertRewriteSuccess("/browse/JRA?abc=def&page=all", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel", confMain);
        //test having params after 'all'
        assertRewriteSuccess("/browse/JRA?abc=def&page=all&xyz=123", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel&xyz=123", confMain);
        assertRewriteSuccess("/browse/JRA?abc=def&page=comments&xyz=123", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel&xyz=123", confMain);
        assertRewriteSuccess("/browse/JRA?abc=def&page=history&xyz=123", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel&xyz=123", confMain);
        assertRewriteSuccess("/browse/JRA?abc=def&page=vcs&xyz=123", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:cvs-tabpanel&xyz=123", confMain);
        assertRewriteSuccess("/browse/JRA?abc=def&page=worklog&xyz=123", "JRA?abc=def&page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel&xyz=123", confMain);
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
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupGetRequestURI(fromUrl);
        MockHttpServletResponse response = new MockHttpServletResponse();
        RewrittenUrl rewrittenUrl = rewriter.processRequest(request, response);
        assertNotNull("Could not rewrite URL from:" + fromUrl + " to:" + toUrl, rewrittenUrl);
        String rewrittenUrlString = rewrittenUrl.getTarget();
        log.debug("URL Rewrite from:[" + fromUrl + "] to [" + rewrittenUrlString + "]");
        assertEquals("Rewrite from:" + fromUrl + " to:" + toUrl + " did not succeed", toUrl, rewrittenUrlString);
    }
}

