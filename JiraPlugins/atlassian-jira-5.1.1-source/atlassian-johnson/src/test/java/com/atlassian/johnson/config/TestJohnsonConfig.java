package com.atlassian.johnson.config;

import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import junit.framework.TestCase;
import mock.MockEventCheck;
import mock.MockSetupConfig;
import mock.MockApplicationEventCheck;
import mock.MockRequestEventCheck;

public class TestJohnsonConfig extends TestCase
{
    public void testGetInstance() throws ConfigurationException
    {
        JohnsonConfig config = new JohnsonConfig("test-johnson-config.xml");

        // parameters
        assertEquals("bar", config.getParams().get("foo"));
        assertEquals("bat", config.getParams().get("baz"));

        // setup config
        assertTrue(config.getSetupConfig() instanceof MockSetupConfig);

        // event checks
        assertEquals(3, config.getEventChecks().size());
        assertTrue(config.getEventChecks().get(0) instanceof MockEventCheck);

        assertEquals(1, config.getRequestEventChecks().size());
        assertTrue(config.getEventChecks().get(1) instanceof MockRequestEventCheck);

        assertEquals(1, config.getApplicationEventChecks().size());
        assertTrue(config.getEventChecks().get(2) instanceof MockApplicationEventCheck);


        assertTrue(config.getEventCheck(1) instanceof MockEventCheck);
        assertTrue(config.getEventCheck(2) instanceof MockRequestEventCheck);
        assertNull(config.getEventCheck(3));

        // setup and error paths
        assertEquals("/the/setup/path.jsp", config.getSetupPath());
        assertEquals("/the/error/path.jsp", config.getErrorPath());

        // ignore paths
        assertEquals(2, config.getIgnorePaths().size());
        assertTrue(config.getIgnorePaths().contains("/ignore/path/1.jsp"));
        assertTrue(config.getIgnorePaths().contains("/ignore/path/*.html"));

        // some ignore mapping tests
        assertTrue(config.isIgnoredPath("/ignore/path/1.jsp"));
        assertTrue(config.isIgnoredPath("/ignore/path/2.html"));
        assertTrue(config.isIgnoredPath("/ignore/path/foo.html"));
        assertFalse(config.isIgnoredPath("/ignore/path"));

        // event levels
        EventLevel expectedError = new EventLevel("error", "Error");
        assertEquals(expectedError, config.getEventLevel("error"));
        EventLevel expectedWarning = new EventLevel("warning", "This is a warning buddy");
        assertEquals(expectedWarning, config.getEventLevel("warning"));

        // event types
        EventType expectedDatabase = new EventType("database", "Database");
        assertEquals(expectedDatabase, config.getEventType("database"));
        EventType expectedUpgrade = new EventType("upgrade", "Upgrade");
        assertEquals(expectedUpgrade, config.getEventType("upgrade"));
    }

    public void testBadEventCheck()
    {
        try
        {
            new JohnsonConfig("test-johnson-config-badeventcheck.xml");
            fail("Should have thrown a ConfigurationException.");
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage().indexOf("Eventcheck java.lang.Object does not implement EventCheck interface.") != -1);
        }
    }

    public void testBadEventCheckId()
    {
        try
        {
            new JohnsonConfig("test-johnson-config-badid.xml");
            fail("Should have thrown a ConfigurationException.");
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage().indexOf("Eventcheck id must be an integer.") != -1);
        }
    }

    public void testDuplicateEventCheckId()
    {
        try
        {
            new JohnsonConfig("test-johnson-config-duplicateid.xml");
            fail("Should have thrown a ConfigurationException.");
        }
        catch (ConfigurationException e)
        {
            assertTrue(e.getMessage().indexOf("Duplicate eventcheck id '" + 1 + "'.") != -1);
        }
    }
}
