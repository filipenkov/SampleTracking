package com.atlassian.gadgets.dashboard.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith (org.mockito.runners.MockitoJUnitRunner.class)
public class HelpLinkResolverTest
{

    private HelpLinkResolver resolver ;

    @Before
    public void setUp() throws Exception
    {
        resolver = new HelpLinkResolver(null);
    }

    @Test
    public void assertDefaultLinkUsedOnUndefinedHelpKey()
    {
        assertTrue(resolver.getLink("__badkey__").startsWith("http://"));
    }

    @Test
    public void assertDefaultLinkUsedOnEmptyKey()
    {
        assertTrue(resolver.getLink("").startsWith("http://"));
        assertEquals(resolver.getLink(""), resolver.getLink("__badkey__"));
    }

    @Test(expected=NullPointerException.class)
    public void assertNullPointExceptionOnNullKey()
    {
        resolver.getLink(null);
    }

}
