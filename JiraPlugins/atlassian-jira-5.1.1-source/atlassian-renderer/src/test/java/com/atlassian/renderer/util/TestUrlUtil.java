package com.atlassian.renderer.util;

import junit.framework.TestCase;

public class TestUrlUtil extends TestCase
{
    public void testSimpleHrefUrls()
    {
        String html = " <a href=\"Server.jspa\"> <a href=\"/Server.jspa\"> <a href=\"http://server.com/Server.jspa\">";
        String baseUrl = "http://www.server.com/dir/file.html";
        String result = " <a href=\"http://www.server.com/dir/Server.jspa\"> <a href=\"http://www.server.com/Server.jspa\"> <a href=\"http://server.com/Server.jspa\">";
        assertEquals(result, UrlUtil.correctBaseUrls(html, baseUrl));
    }

    public void testSimpleImgSrc()
    {
        String html = " <img src=\"Server.jspa\"> <img src=\"/Server.jspa\"> <img src=\"http://server.com/Server.jspa\">";
        String baseUrl = "http://www.server.com/dir/file.html";
        String result = " <img src=\"http://www.server.com/dir/Server.jspa\"> <img src=\"http://www.server.com/Server.jspa\"> <img src=\"http://server.com/Server.jspa\">";
        assertEquals(result, UrlUtil.correctBaseUrls(html, baseUrl));
    }

    
    public void testQuotesAreNotEncoded()
    {
        String html = " quote'";
        String html2 = " quote\"";
        assertEquals(" quote'", UrlUtil.escapeSpecialCharacters(html));
        assertEquals(" quote\"", UrlUtil.escapeSpecialCharacters(html2));
    }

    public void testComplex1()
    {
        String html = "[Open Issues]: <b><a href=\"IssueNavigator.jspa?reset=true&assigneeSelect=\">[Assigned To Me]</a></b><td nowrap width=1%>\n "
                + "<a href=\"/browse/TST-1\"><img src=\"/images/icons/bug.gif\" height=16 width=16 border=0 align=absmiddle alt=\"Bug\" title=\"Bug - A problem which impairs or prevents the functions of the product.\">\n" +
                "</a>";

        String baseUrl = "http://www.server.com/myserver/feed.html";

        String result = "[Open Issues]: <b><a href=\"http://www.server.com/myserver/IssueNavigator.jspa?reset=true&assigneeSelect=\">[Assigned To Me]</a></b><td nowrap width=1%>\n "
                + "<a href=\"http://www.server.com/browse/TST-1\"><img src=\"http://www.server.com/images/icons/bug.gif\" height=16 width=16 border=0 align=absmiddle alt=\"Bug\" title=\"Bug - A problem which impairs or prevents the functions of the product.\">\n" +
                "</a>";

        assertEquals(result, UrlUtil.correctBaseUrls(html, baseUrl));
    }

    public void testComplex2()
    {
        String html = "[Open Issues]: <b><a href=\"IssueNavigator.jspa?reset=true&assigneeSelect=\">[Assigned To Me]</a></b><td nowrap width=1%>\n "
                + "<a href=\"/browse/TST-1\"><img src=\"/images/icons/bug.gif\" height=16 width=16 border=0 align=absmiddle alt=\"Bug\" title=\"Bug - A problem which impairs or prevents the functions of the product.\">\n" +
                "</a>";

        String baseUrlNoSlash = "http://www.server.com";

        String result = "[Open Issues]: <b><a href=\"http://www.server.com/IssueNavigator.jspa?reset=true&assigneeSelect=\">[Assigned To Me]</a></b><td nowrap width=1%>\n "
                + "<a href=\"http://www.server.com/browse/TST-1\"><img src=\"http://www.server.com/images/icons/bug.gif\" height=16 width=16 border=0 align=absmiddle alt=\"Bug\" title=\"Bug - A problem which impairs or prevents the functions of the product.\">\n" +
                "</a>";

        assertEquals(result, UrlUtil.correctBaseUrls(html, baseUrlNoSlash));
    }

    public void testSingleQuotesAttributes()
    {
        String html = "<a href='/foo'><img src='/bar'></a><a href='/baz'>quux</a>";
        String result = "<a href='http://www.example.com/foo'><img src='http://www.example.com/bar'></a><a href='http://www.example.com/baz'>quux</a>";
        assertEquals(result, UrlUtil.correctBaseUrls(html, "http://www.example.com"));
    }

    public void testSrcAttributeNotFirst()
    {
        String html = "<area id=\"myid\" href=\"/bar\">";
        String result = "<area id=\"myid\" href=\"http://www.example.com/bar\">";
        assertEquals(result, UrlUtil.correctBaseUrls(html, "http://www.example.com"));
    }
}

