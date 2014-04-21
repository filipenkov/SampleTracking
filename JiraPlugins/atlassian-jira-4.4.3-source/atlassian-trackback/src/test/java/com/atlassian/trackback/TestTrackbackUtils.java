package com.atlassian.trackback;

import junit.framework.TestCase;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class TestTrackbackUtils extends TestCase
{
    public void testGetHttpLinks()
    {
        String content = "Some HTML with a <a href=\"http://fishbowl.pastiche.org/2003/02/22/shared_data_diverse_applications\">anot" +
                "her</a> and <a href=\"http://fishbowl.pastiche.org/2003/02/22/organised_cows\">link in" +
                " it</a> boo!";

        List urls = TrackbackUtils.getHttpLinks(content);
        assertEquals(2, urls.size());
        assertEquals("http://fishbowl.pastiche.org/2003/02/22/shared_data_diverse_applications", urls.get(0));
        assertEquals("http://fishbowl.pastiche.org/2003/02/22/organised_cows", urls.get(1));
    }

    public void testGetUrlContent() throws IOException
    {
        assertNull(TrackbackUtils.getUrlContent("http://www.google.com/images/logo.gif"));
        assertNotNull(TrackbackUtils.getUrlContent("http://www.google.com/about.html"));
        int retrievedSize = TrackbackUtils.getUrlContent("http://www.javablogs.com/ViewDaysBlogs.jspa?date=4&month=1&year=2004&view=rss").length();
        assertTrue(retrievedSize < 104 * 1024);
    }

    public void testGetTrackBackUrl() throws IOException
    {
        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testhtml.html");
        InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
        CharArrayWriter writer = new CharArrayWriter();

        int i;
        while ((i = inputStreamReader.read()) != -1)
        {
            writer.write(i);
        }

        String html = new String(writer.toCharArray());

        assertEquals("http://blogs.atlassian.com/mt/mt-tb.cgi/338", TrackbackUtils.getTrackbackUrl(html, "http://blogs.atlassian.com/rebelutionary/archives/000387.html"));
    }
}
