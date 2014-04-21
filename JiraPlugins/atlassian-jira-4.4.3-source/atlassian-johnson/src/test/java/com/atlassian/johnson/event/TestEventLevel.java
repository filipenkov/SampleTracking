package com.atlassian.johnson.event;

import com.atlassian.johnson.config.ConfigurationException;
import com.atlassian.johnson.config.JohnsonConfig;
import junit.framework.TestCase;

public class TestEventLevel extends TestCase
{
    protected void tearDown() throws Exception
    {
        JohnsonConfig.setInstance(null);
        super.tearDown();
    }

    public void testEventLevel()
    {
        EventLevel level = new EventLevel("foo", "bar");
        assertEquals("foo", level.getLevel());
        assertEquals("bar", level.getDescription());
    }

    public void testGetEventLevel() throws ConfigurationException
    {
        JohnsonConfig.setInstance(new JohnsonConfig("test-johnson-config.xml"));

        EventLevel expectedWarning = new EventLevel("warning", "This is a warning buddy");
        assertEquals(expectedWarning, EventLevel.get("warning"));
    }
}
