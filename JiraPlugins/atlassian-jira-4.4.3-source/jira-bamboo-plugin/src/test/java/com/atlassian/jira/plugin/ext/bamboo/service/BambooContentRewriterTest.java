package com.atlassian.jira.plugin.ext.bamboo.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BambooContentRewriterTest
{
    private BambooContentRewriter bambooContentRewriter;
    private static final String BAMBOO_SERVER_NO_CONTEXT = "http://localhost/";
    private static final String BAMBOO_SERVER = "http://localhost/bamboo";

    @Test
    public void testCorrectContextPathIsReturned() throws Exception
    {
        final String contextPath = bambooContentRewriter.getContextPathWithSlash("http://xxxxgold:8095");
        assertEquals("Should be root", "/" , contextPath );
    }

    @Test
    public void testCorrectContextPathIsReturnedForSlashEnding() throws Exception
    {
        final String contextPath = bambooContentRewriter.getContextPathWithSlash("http://xxxxgold:8095/");
        assertEquals("Should be root", "/" , contextPath );
    }

    @Test
    public void testExtractsContextPathWithHttp() throws Exception
    {
        String url = "http://my.site.com:1231/bamboopath/contextPath";
        String contextPath = bambooContentRewriter.getContextPathWithSlash(url);
        assertEquals("The server definition including protocol should be extracted", "/bamboopath/contextPath/", contextPath);
    }

    @Test
    public void testBlankContextPathWhenNoContextPath() throws Exception
    {
        String url = "https://my.site.com:1231";
        String contextPath = bambooContentRewriter.getContextPathWithSlash(url);
        assertEquals("The server definition including protocol should be extracted", "/", contextPath);
    }

    @Test
    public void testExtractsContextPathWithHttps() throws Exception
    {
        String url = "https://my.site.com:1231/bamboopath/contextPath";
        String contextPath = bambooContentRewriter.getContextPathWithSlash(url);
        assertEquals("The server definition including protocol should be extracted", "/bamboopath/contextPath/", contextPath);
    }

    @Test
    public void testShouldIgnoreHashedUrl() throws Exception
    {
        final String originalLink = "<a href=\"#menu\">Skip to navigation</a>";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER);
        assertEquals(originalLink, path);
    }

    @Test
    public void testShouldIgnoreAbsoluteUrl() throws Exception
    {
        final String originalLink = "<a title=\"View this issue in JIRA\" class=\"jiraLinkIcon\" href=\"http://jira.atlassian.com/browse/BAM-1802?page=com.atlassian.jira.plugin.ext.bamboo%3Abamboo-build-results-tabpanel\" id=\"viewIssueInJira:BAM-1802\">BAM-1802</a>";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER);
        assertEquals(originalLink, path);

    }

    @Test
    public void testShouldAppendServerToPath() throws Exception
    {
        final String originalLink = "<a href=\"/browse/user/mark\">mchai</a>";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER_NO_CONTEXT);
        assertEquals("<a href=\"" + BAMBOO_SERVER_NO_CONTEXT + "browse/user/mark\">mchai</a>", path);

    }

    @Test
    public void testShouldAppendServerToPathStillWorksWithCrappyContext() throws Exception
    {
        final String originalLink = "<a href=\"/browse/user/mark\">mchai</a>";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER_NO_CONTEXT);
        assertEquals("<a href=\"" + BAMBOO_SERVER_NO_CONTEXT + "browse/user/mark\">mchai</a>", path);

    }

    @Test
    public void testShouldAppendServerToPathStillWorksWithContext() throws Exception
    {
        final String originalLink = "<a href=\"/bamboo/browse/user/mark\">mchai</a>";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER);
        assertEquals("<a href=\"" + BAMBOO_SERVER + "/browse/user/mark\">mchai</a>", path);

    }

    @Test
    public void testShouldAppendServerToSrcWithContext() throws Exception
    {
        final String originalLink = "<img class=\"profileImage\" src=\"/bamboo/images/icons/businessman.gif\" alt=\"mchai\" />";
        final String path = bambooContentRewriter.rewriteHtml(originalLink, BAMBOO_SERVER);
        assertEquals("<img class=\"profileImage\" src=\"" + BAMBOO_SERVER + "/images/icons/businessman.gif\" alt=\"mchai\" />", path);

    }

    @Before
    public void setUp() throws Exception
    {
        bambooContentRewriter = new BambooContentRewriter();
    }

    @After
    public void tearDown() throws Exception
    {
        bambooContentRewriter = null;
    }
}
