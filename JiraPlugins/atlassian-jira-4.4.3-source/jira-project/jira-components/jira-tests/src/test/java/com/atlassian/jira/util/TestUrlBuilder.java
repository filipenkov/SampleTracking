package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Simple unit test for {@link com.atlassian.jira.util.UrlBuilder}.
 *
 * @since v4.0
 */
public class TestUrlBuilder extends ListeningTestCase
{
    @Test
    public void testAddAnchor() throws Exception
    {
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addAnchor("boo\u00a5");
        assertEquals("froo.com/#boo%C2%A5", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParameterUnsafe()
    {
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/");
        urlBuilder.addParameterUnsafe("f", "%20%3aboo");
        assertEquals("froo.com/?f=%20%3aboo", urlBuilder.asUrlString());

        //The passed parameter should not be escaped.
        urlBuilder = new UrlBuilder("froo.com/");
        urlBuilder.addParameterUnsafe("f", "b=c");
        assertEquals("froo.com/?f=b=c", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParameter()
    {
        //make sure the parameter is escaped with the correct encoding.
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addParameter("f", "boo\u00a5");
        assertEquals("froo.com/?f=boo%C2%A5", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "ISO-8859-1", false);
        urlBuilder.addParameter("f", "boo\u00a5");
        assertEquals("froo.com/?f=boo%A5", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "US-ASCII", false);
        urlBuilder.addParameter("f", "a").addParameter("f", "b");
        assertEquals("froo.com/?f=a&f=b", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/?already=here", "US-ASCII", false);
        urlBuilder.addParameter("f", "a").addParameter("f", "b").addParameter("%", "blah").addParameter("1", new StringBuilder("%"));
        assertEquals("froo.com/?already=here&f=a&f=b&%25=blah&1=%25", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParametersFromMap() throws Exception
    {
        final Map<String, String> map = MapBuilder.<String, String> newBuilder().add("f", "boo\u00a5").add("g", "hoo").toMap();

        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addParametersFromMap(map);
        assertEquals("froo.com/?f=boo%C2%A5&g=hoo", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "ISO-8859-1", false);
        urlBuilder.addParametersFromMap(map);
        assertEquals("froo.com/?f=boo%A5&g=hoo", urlBuilder.asUrlString());

        final Map<String, String> map1 = MapBuilder.<String, String> newBuilder().add("f", "a").toMap();
        final Map<String, Object> map2 = MapBuilder.<String, Object> newBuilder().add("f", 56).toMap();

        urlBuilder = new UrlBuilder("froo.com/?already=here", "US-ASCII", false);
        urlBuilder.addParametersFromMap(map1).addParametersFromMap(map2).addParameter("%", "blah");
        assertEquals("froo.com/?already=here&f=a&f=56&%25=blah", urlBuilder.asUrlString());
    }

    @Test
    public void testSnippet() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("", "UTF-8", true);
        urlBuilder.addParameter("f", "boo");
        assertEquals("&f=boo", urlBuilder.asUrlString());
    }

    @Test
    public void testPercent() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("", "UTF-8", true);
        urlBuilder.addParameter("f", "per%cent");
        assertEquals("&f=per%25cent", urlBuilder.asUrlString());
    }
}
