package com.atlassian.trackback;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsInstanceOf;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

public class TestTrackbackHelper extends TestCase {

    public void testNotifyTrackbacks() throws IOException
    {

        Mock finder = new Mock(TrackbackFinder.class);
        Mock sender = new Mock(TrackbackSender.class);

        TrackbackHelper helper = new DefaultTrackbackHelper((TrackbackFinder) finder.proxy(), (TrackbackSender) sender.proxy());

        List trackbackUrls = new ArrayList();
        trackbackUrls.add("trackback.cgi");
        finder.expectAndReturn("findPingUrls", P.args(new IsEqual("Some content text")), trackbackUrls);
        sender.expectVoid("sendPing", P.args(new IsEqual("trackback.cgi"), new IsInstanceOf(Trackback.class)));

        Trackback ping = new Trackback();
        ping.setUrl("blogurl");
        ping.setTitle("blogtitle");
        helper.pingTrackbacksInContent("Some content text", ping);

        finder.verify();
        sender.verify();
    }
}
