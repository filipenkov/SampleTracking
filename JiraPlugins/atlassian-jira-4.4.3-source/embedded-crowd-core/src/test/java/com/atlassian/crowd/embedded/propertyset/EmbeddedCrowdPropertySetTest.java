package com.atlassian.crowd.embedded.propertyset;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.google.common.collect.Sets;
import com.opensymphony.module.propertyset.IllegalPropertyException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public class EmbeddedCrowdPropertySetTest extends TestCase
{
    @Mock
    private CrowdService crowdService;

    private EmbeddedCrowdPropertySet propertySet;
    private UserTemplateWithAttributes user;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        user = new UserTemplateWithAttributes("fred", 100L);
        user.setAttribute("hair", "brown");
        user.setAttribute("friends", Sets.newHashSet("Mary", "John"));

        propertySet = new EmbeddedCrowdPropertySet(user, crowdService);
    }

    public void testInitialProperties() throws Exception
    {
        assertEquals("brown", propertySet.getString("hair"));
        assertEquals("Mary", propertySet.getString("friends")); // returns the first attribute in the List
        verifyZeroInteractions(crowdService); // only used for update operations
    }

    public void testSetStringToNull() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.STRING));

        propertySet.setString("key", null);

        verify(crowdService).removeUserAttribute(user, "key");
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), anySet()); // don't attempt to store nulls
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), any(String.class)); // don't attempt to store nulls
        assertNull(propertySet.getString("key"));
    }

    public void testSetStringToBlank() throws Exception
    {
        propertySet.setString("key", "");
        verify(crowdService).setUserAttribute(user, "key", "");
        assertEquals("", propertySet.getString("key"));
    }

    public void testSetString() throws Exception
    {
        propertySet.setString("key", "A String");
        verify(crowdService).setUserAttribute(user, "key", "A String");
        assertEquals("A String", propertySet.getString("key"));
    }

    public void testSetStringTooLong() throws Exception
    {
        try
        {
            propertySet.setString("key", StringUtils.repeat("value", 80)); // larger than 255 characters
            fail("Expected exception not thrown");
        }
        catch (IllegalPropertyException expected)
        {
        }
    }

    public void testGetStringDefaultValue() throws Exception
    {
        assertNull(propertySet.getString("key"));
    }

    public void testSetTextToNull() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.TEXT));
        propertySet.setText("key", null);

        verify(crowdService).removeUserAttribute(user, "key");
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), anySet()); // don't attempt to store nulls
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), any(String.class)); // don't attempt to store nulls

        assertNull(propertySet.getText("key"));
        assertNull(propertySet.getString("key"));
    }

    public void testSetTextToBlank() throws Exception
    {
        propertySet.setText("key", "");

        verify(crowdService).setUserAttribute(user, "key", "");

        assertEquals("", propertySet.getText("key"));
        assertEquals("", propertySet.getString("key"));
    }

    public void testSetText() throws Exception
    {
        propertySet.setText("key", "A Text");

        verify(crowdService).setUserAttribute(user, "key", "A Text");

        assertEquals("A Text", propertySet.getText("key"));
        assertEquals("A Text", propertySet.getString("key"));
    }

    public void testSetTextTooLong() throws Exception
    {
        try
        {
            propertySet.setText("text", StringUtils.repeat("value", 80)); // larger than 255 characters
            fail("Expected exception not thrown");
        }
        catch (IllegalPropertyException expected)
        {
        }
        verifyZeroInteractions(crowdService);
    }

    public void testGetText() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertNull(propertySet.getText("key"));
    }

    public void testSetBoolean() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.BOOLEAN));

        propertySet.setBoolean("key", true);
        propertySet.setBoolean("clever", false);
        assertTrue(propertySet.getBoolean("key"));
        assertEquals("true", propertySet.getString("key"));
        assertFalse(propertySet.getBoolean("clever"));
        assertEquals("false", propertySet.getString("clever"));
    }

    public void testGetBoolean() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertFalse(propertySet.getBoolean("key"));
        propertySet.setString("key", null);
        assertFalse(propertySet.getBoolean("key"));

        // These probably failed in previous implementations, but we don't care.
        propertySet.setString("key", "");
        assertFalse(propertySet.getBoolean("key"));

        propertySet.setString("key", "gobbledy");
        assertFalse(propertySet.getBoolean("key"));
    }

    public void testSetInt() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.INT));

        propertySet.setInt("key", 10);
        assertEquals(10, propertySet.getInt("key"));
        assertEquals("10", propertySet.getString("key"));

        propertySet.setInt("key", -25);
        assertEquals(-25, propertySet.getInt("key"));
        assertEquals("-25", propertySet.getString("key"));

        propertySet.setInt("key", 0);
        assertEquals(0, propertySet.getInt("key"));
        assertEquals("0", propertySet.getString("key"));
    }

    public void testGetInt() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertEquals(0, propertySet.getInt("key"));
        propertySet.setString("key", null);
        assertEquals(0, propertySet.getInt("key"));

        // These should fail
        propertySet.setString("key", "");
        try
        {
            propertySet.getInt("key");
            fail("Should throw horrible exception");
        }
        catch (Exception e)
        {
            // OK
        }

        propertySet.setString("key", "gobbledy");
        try
        {
            propertySet.getInt("key");
            fail("Should throw horrible exception");
        }
        catch (Exception e)
        {
            // OK
        }
    }

    public void testSetLong() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.LONG));

        propertySet.setLong("key", 100000000000L);
        assertEquals(100000000000L, propertySet.getLong("key"));
        assertEquals("100000000000", propertySet.getString("key"));

        propertySet.setLong("key", -25);
        assertEquals(-25, propertySet.getLong("key"));
        assertEquals("-25", propertySet.getString("key"));

        propertySet.setLong("key", 0);
        assertEquals(0, propertySet.getLong("key"));
        assertEquals("0", propertySet.getString("key"));
    }

    public void testGetLong() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertEquals(0, propertySet.getLong("key"));
        propertySet.setString("key", null);
        assertEquals(0, propertySet.getLong("key"));

        // These should fail
        propertySet.setString("key", "");
        try
        {
            propertySet.getLong("key");
            fail("Should throw horrible exception");
        }
        catch (Exception e)
        {
            // OK
        }

        propertySet.setString("key", "gobbledy");
        try
        {
            propertySet.getLong("key");
            fail("Should throw horrible exception");
        }
        catch (Exception e)
        {
            // OK
        }
    }

    public void testSetDouble() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.DOUBLE));

        propertySet.setDouble("key", 123.45D);
        assertEquals(123.45D, propertySet.getDouble("key"));
        assertEquals("123.45", propertySet.getString("key"));

        propertySet.setDouble("key", -25);
        assertEquals(-25D, propertySet.getDouble("key"));
        assertEquals("-25.0", propertySet.getString("key"));

        propertySet.setDouble("key", 0);
        assertEquals(0D, propertySet.getDouble("key"));
        assertEquals("0.0", propertySet.getString("key"));
    }

    public void testGetDouble() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertEquals(0D, propertySet.getDouble("key"));
        propertySet.setString("key", null);
        assertEquals(0D, propertySet.getDouble("key"));

        // These should fail
        propertySet.setString("key", "");
        try
        {
            propertySet.getDouble("key");
            fail("Should throw horrible exception");
        }
        catch (NumberFormatException e)
        {
            // OK
        }

        propertySet.setString("key", "gobbledy");
        try
        {
            propertySet.getDouble("key");
            fail("Should throw horrible exception");
        }
        catch (NumberFormatException e)
        {
            // OK
        }
    }

    public void testSetDate() throws Exception
    {
        assertTrue(propertySet.supportsType(PropertySet.DATE));

        propertySet.setDate("key", null);
        assertNull(propertySet.getDate("key"));
        assertNull(propertySet.getString("key"));

        Date jan1_1970 = new Date(0);
        propertySet.setDate("key", jan1_1970);
        assertEquals(jan1_1970, propertySet.getDate("key"));
        assertEquals("1970-01-01T00:00:00.000+0000", propertySet.getString("key"));

        Date aboutNow = new Date(1260331200000L);
        propertySet.setDate("key", aboutNow);
        assertEquals(new Date(1260331200000L), propertySet.getDate("key"));
        assertEquals("2009-12-09T04:00:00.000+0000", propertySet.getString("key"));

        Date birthday = new Date(-11973600000L);
        propertySet.setDate("key", birthday);
        assertEquals(new Date(-11973600000L), propertySet.getDate("key"));
        assertEquals("1969-08-15T10:00:00.000+0000", propertySet.getString("key"));
    }

    public void testSetDateToNull() throws Exception
    {
        propertySet.setDate("key", null);

        verify(crowdService).removeUserAttribute(user, "key");
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), anySet()); // don't attempt to store nulls
        verify(crowdService, never()).setUserAttribute(eq(user), any(String.class), any(String.class)); // don't attempt to store nulls
        assertNull(propertySet.getDate("key"));
    }

    public void testGetDate() throws Exception
    {
        // Check default behaviour, when value is missing or null
        assertNull(propertySet.getDate("key"));
        propertySet.setString("key", null);
        assertNull(propertySet.getDate("key"));

        // These should fail
        propertySet.setString("key", "");
        try
        {
            propertySet.getDate("key");
            fail("Should throw horrible exception");
        }
        catch (PropertyException e)
        {
            // OK
        }

        propertySet.setString("key", "gobbledy");
        try
        {
            propertySet.getDate("key");
            fail("Should throw horrible exception");
        }
        catch (PropertyException e)
        {
            // OK
        }
    }

    public void testGetData() throws Exception
    {
        assertFalse(propertySet.supportsType(PropertySet.DATA));

        try
        {
            propertySet.getData("aaa");
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        byte[] data = new byte[2];
        try
        {
            propertySet.setData("aaa", data);
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    public void testGetObject() throws Exception
    {
        assertFalse(propertySet.supportsType(PropertySet.OBJECT));

        try
        {
            propertySet.getObject("aaa");
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            propertySet.setObject("aaa", new Object());
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    public void testGetProperties() throws Exception
    {
        assertFalse(propertySet.supportsType(PropertySet.PROPERTIES));

        try
        {
            propertySet.getProperties("aaa");
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
        try
        {
            propertySet.setProperties("aaa", new Properties());
            fail();
        }
        catch (UnsupportedOperationException e)
        {
            // expected
        }
    }

    public void testRemove() throws Exception
    {
        propertySet.remove("test");

        verify(crowdService).removeUserAttribute(user, "test");
    }

    public void testUpdateExistingProperty() throws Exception
    {
        assertEquals("brown", propertySet.getString("hair"));
        propertySet.setString("hair", "red");

        verify(crowdService).removeUserAttribute(user, "hair"); // should remove old attributes first
        verify(crowdService).setUserAttribute(user, "hair", "red");
        assertEquals("red", propertySet.getString("hair"));
    }
}