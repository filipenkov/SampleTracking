package com.atlassian.johnson.event;

import com.atlassian.johnson.config.ConfigurationException;
import com.atlassian.johnson.config.JohnsonConfig;
import junit.framework.TestCase;

public class TestEventType extends TestCase
{
    protected void tearDown() throws Exception
    {
        JohnsonConfig.setInstance(null);
        super.tearDown();
    }

    public void testEventType()
    {
        EventType type = new EventType("foo", "bar");
        assertEquals("foo", type.getType());
        assertEquals("bar", type.getDescription());
    }

    public void testGetEventType() throws ConfigurationException
    {
        JohnsonConfig.setInstance(new JohnsonConfig("test-johnson-config.xml"));

        EventType expectedWarning = new EventType("database", "Database");
        assertEquals(expectedWarning, EventType.get("database"));
    }
}
