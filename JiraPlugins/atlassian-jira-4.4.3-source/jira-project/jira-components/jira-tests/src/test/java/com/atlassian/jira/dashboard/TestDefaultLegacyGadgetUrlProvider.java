package com.atlassian.jira.dashboard;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;

public class TestDefaultLegacyGadgetUrlProvider extends MockControllerTestCase
{
    @Test
    public void testGetLegacyURI()
    {
        DefaultLegacyGadgetUrlProvider urlProvider = mockController.instantiateAndReplay(DefaultLegacyGadgetUrlProvider.class);
        try
        {
            urlProvider.getLegacyURI(null);
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            //
        }

        final URI legacyURI = urlProvider.getLegacyURI("some.random:portlet");
        assertEquals(URI.create("rest/gadget/1.0/legacy/spec/some.random:portlet.xml"), legacyURI);
    }

    @Test
    public void testIsLegacyGadget()
    {
        DefaultLegacyGadgetUrlProvider provider = new DefaultLegacyGadgetUrlProvider();
        try
        {
            provider.isLegacyGadget(null);
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            //
        }

        assertFalse(provider.isLegacyGadget(URI.create("")));
        assertFalse(provider.isLegacyGadget(URI.create("http://www.google.com/ig/some.gadget.spec.xml")));
        assertTrue(provider.isLegacyGadget(URI.create("http://localhost:8080/jira/rest/gadget/1.0/legacy/spec/some.random:portlet.xml")));
    }

    @Test
    public void testExtractPortletKey()
    {
        DefaultLegacyGadgetUrlProvider provider = new DefaultLegacyGadgetUrlProvider();
        try
        {
            provider.extractPortletKey(null);
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            //
        }

        try
        {
            provider.extractPortletKey(URI.create(""));
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {

        }

        try
        {
            provider.extractPortletKey(URI.create("http://www.google.com/ig/some.gadget.spec.xml"));
            fail("should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {

        }
        assertEquals("some.random:portlet", provider.extractPortletKey(URI.create("http://localhost:8080/jira/rest/gadget/1.0/legacy/spec/some.random:portlet.xml")));
    }
}
