package com.atlassian.jira.servlet;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

public class TestBrowseProjectUrlHelper extends ListeningTestCase
{

    public TestBrowseProjectUrlHelper()
    {
        super();
    }

    @Test
    public void testNullIsRejected()
    {
        try
        {
            new BrowseProjectUrlHelper(null);
            fail("Null is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testShortStringRejected()
    {
        try
        {
            new BrowseProjectUrlHelper("");
            fail("Empty string is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            new BrowseProjectUrlHelper("/");
            fail("Empty string is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        try
        {
            new BrowseProjectUrlHelper("a");
            fail("Empty string is not allowed");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testValidProject()
    {
        BrowseProjectUrlHelper linkHelper = new BrowseProjectUrlHelper("/A");
        assertEquals("A", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/MKY");
        assertEquals("MKY", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/whatever");
        assertEquals("TEST/whatever", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/whatever/else");
        assertEquals("TEST/whatever/else", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        // version

        linkHelper = new BrowseProjectUrlHelper("/TEST/fixforversion");
        assertEquals("TEST/fixforversion", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/fixforversion/");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/fixforversion/ABC");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/fixforversion/123");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertEquals(new Long(123), linkHelper.getVersionId());

        // component

        linkHelper = new BrowseProjectUrlHelper("/TEST/component");
        assertEquals("TEST/component", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/component/");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/component/ABC");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertNull(linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());

        linkHelper = new BrowseProjectUrlHelper("/TEST/component/123");
        assertEquals("TEST", linkHelper.getProjectKey());
        assertEquals(new Long(123), linkHelper.getComponentId());
        assertNull(linkHelper.getVersionId());
    }

}
