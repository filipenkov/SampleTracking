package com.atlassian.applinks.spi.application;

import com.atlassian.applinks.api.ApplicationId;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.applinks.spi.application.ApplicationIdUtil.generate;
import static junit.framework.Assert.fail;

public class TestApplicationIdUtil
{

    @Test
    public void testGenerateApplicationId() throws Exception
    {
        final URI one = new URI("http://humpty.example.com/refapp/wall");
        final URI onceMoreWithTrailingSlash = new URI("http://humpty.example.com/refapp/wall/");
        final URI oneDenormalised = new URI("http://humpty.example.com/refapp/../refapp/wall/.././././wall");
        final URI two = new URI("http://dumpty.example.com");
        final URI three = new URI("https://dumpty.example.com");

        assertEqual(one, one);
        assertEqual(one, onceMoreWithTrailingSlash);
        assertEqual(one, oneDenormalised);

        assertNotEqual(one, two);
        assertNotEqual(one, three);
        assertNotEqual(two, one);
        assertNotEqual(two, three);
    }

    private void assertEqual(final URI uri0, final URI uri1)
    {
        assertEquality(uri0, uri1, true);
    }

    private void assertNotEqual(final URI uri0, final URI uri1)
    {
        assertEquality(uri0, uri1, false);
    }

    private void assertEquality(final URI uri0, final URI uri1, final boolean expectEqual)
    {
        final ApplicationId id0 = generate(uri0);
        final ApplicationId id1 = generate(uri1);
        if (expectEqual ^ id0.equals(id1))
        {
            fail(String.format("\nGenerated id for:\n\t %s (%s)\n is %sequal to id for:\n\t %s (%s)\n",
                    uri0, id0, (expectEqual ? "NOT " : ""), uri1, id1));
        }
    }


}
