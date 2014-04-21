package com.atlassian.applinks.core.property;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class EntityLinkPropertiesTest
{
    private EntityLinkProperties entityLinkProperties;
    private MockPluginSettingsPropertySet propertySet;

    @Before
    public void setUp()
    {
        propertySet = new MockPluginSettingsPropertySet();
        entityLinkProperties = new EntityLinkProperties(propertySet);
    }

    @Test
    public void testGetProperty() throws Exception 
    {
        entityLinkProperties.putProperty("name", "test");
        assertEquals("test", entityLinkProperties.getProperty("name"));
        assertEquals(null, entityLinkProperties.getProperty("doesNotExist"));
    }

    @Test
    public void testPutProperty() throws Exception
    {
        entityLinkProperties.putProperty("name", "test");
        final List<String> keys = (List<String>) propertySet.get("properties");
        assertTrue(keys.contains("name"));
    }

    @Test
    public void testRemove() throws Exception 
    {
        entityLinkProperties.putProperty("name", "test");
        entityLinkProperties.putProperty("name2", "test2");
        entityLinkProperties.putProperty("name3", "test3");
        entityLinkProperties.putProperty("name4", "test4");
        entityLinkProperties.removeAll();
        assertNull(propertySet.get("properties"));
    }

    @Test
    public void testRemoveProperty() throws Exception 
    {
        entityLinkProperties.putProperty("name", "test");
        entityLinkProperties.removeProperty("name");
        final List<String> keys = (List<String>) propertySet.get("properties");
        assertFalse(keys.contains("name"));
    }
}
