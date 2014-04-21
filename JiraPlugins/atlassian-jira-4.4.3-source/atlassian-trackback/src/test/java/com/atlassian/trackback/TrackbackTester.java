package com.atlassian.trackback;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class TrackbackTester
{
    private static String content = "Some HTML with a <A href=\"http://fishbowl.pastiche.org/2003/02/22/shared_data_diverse_applications\">another</a> and <a href=\"http://fishbowl.pastiche.org/2003/02/22/organised_cows\">link in it</a> boo!";

    public static void main(String[] args) throws IOException
    {
        Trackback tb = new Trackback();
        tb.setTitle("Blame mike :)");
        tb.setUrl("http://nowhere.com");
        tb.setExcerpt("This is a test of the emergency broadcast system");
        tb.setBlogName("Atlassian-trackback");

        DefaultTrackbackFinder tbFinder = new DefaultTrackbackFinder();
        List urls = tbFinder.findPingUrls(content);

        TrackbackSender sender = new DefaultTrackbackSender();
        for (Iterator iterator = urls.iterator(); iterator.hasNext();)
        {
            String pingUrl = (String) iterator.next();
            sender.sendPing(pingUrl, tb);
        }
    }
}
