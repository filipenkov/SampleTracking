package com.atlassian.configurable;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

public class TestClassObjectConfigurationProperty extends ListeningTestCase
{
    @Test
    public void testNullEnabledCondition()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, MyValues.class.getName(), null);
        assertTrue(property.isEnabled());
    }

    @Test
    public void testNotEnabledCondition()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, MyValues.class.getName(), NotEnabledCondition.class.getName());
        assertFalse(property.isEnabled());
    }

    @Test
    public void testValuesGenerator()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, MyValues.class.getName(), NotEnabledCondition.class.getName());
        assertEquals("1", property.get("one"));
    }

    @Test
    public void testNullValuesGenerator()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, null, NotEnabledCondition.class.getName());
        assertNull(property.get("one"));
    }

    @Test
    public void testBogusValuesGenerator()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, "blah.de.1234.blah", NotEnabledCondition.class.getName());
        assertNull(property.get("one"));
    }

    @Test
    public void testIncorrectValuesGenerator()
    {
        ValuesGeneratorObjectConfigurationProperty property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, Object.class.getName(), NotEnabledCondition.class.getName());
        assertNull(property.get("one"));
    }

    static class MyValues implements ValuesGenerator
    {
        public Map getValues(Map userParams)
        {
            return EasyMap.build("one", "1", "two", "2");
        }
    }
}
