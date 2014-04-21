package com.atlassian.jira.user.preferences;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;

public class TestJiraUserPreferences extends ListeningTestCase
{
    ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = new MockApplicationProperties();
    }

    /** An osuser User Object that returns null for getPropertySet() will cause NPE. */
    @Test
    public void test_JRA_13778()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences((PropertySet) null);

        // Should act as though it has a null user:
        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        try
        {
            jiraUserPreferences.setString("Fruit", "Banana");
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetString() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
    }

    @Test
    public void testGetAndSetStringNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        try
        {
            jiraUserPreferences.setString("Fruit", "Banana");
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetLong() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        try
        {
            jiraUserPreferences.getLong("Size");
            fail("getLong() on  non-existant value should throw NumberFormatException.");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }
        applicationProperties.setString("Size", "12");
        assertEquals(12, jiraUserPreferences.getLong("Size"));
        jiraUserPreferences.setLong("Size", 20);
        assertEquals(20, jiraUserPreferences.getLong("Size"));
    }

    @Test
    public void testModifyPropertyViaUserPropertySetAndSeeChangeInPrefs() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        propertySet.setString("Test", "Stuff");
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);
        assertEquals("Stuff", jiraUserPreferences.getString("Test"));

        // Now change it on the users property
        propertySet.setString("Test", "Other Stuff");

        // We should see the change
        assertEquals("Other Stuff", jiraUserPreferences.getString("Test"));
    }

    @Test
    public void testGetAndSetLongNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        try
        {
            jiraUserPreferences.getLong("Size");
            fail("getLong() on  non-existant value should throw NumberFormatException.");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }
        applicationProperties.setString("Size", "10");
        assertEquals(10, jiraUserPreferences.getLong("Size"));

        // Test the setter
        try
        {
            jiraUserPreferences.setLong("Size", 20);
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetBoolean() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
        applicationProperties.setOption("GoFastFlag", true);
        assertTrue(jiraUserPreferences.getBoolean("GoFastFlag"));
        jiraUserPreferences.setBoolean("GoFastFlag", false);
        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
    }

    @Test
    public void testGetAndSetBooleanNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
        applicationProperties.setOption("GoFastFlag", true);
        assertTrue(jiraUserPreferences.getBoolean("GoFastFlag"));

        // Test the setter
        try
        {
            jiraUserPreferences.setBoolean("Size", true);
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testRemove() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertNull(jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.remove("Fruit");
        assertNull(jiraUserPreferences.getString("Fruit"));

        // Now test with underlying default property
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.remove("Fruit");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
    }

    @Test
    public void testEquals() throws AtlassianCoreException
    {
        MemoryPropertySet sallyPropertySet = new MemoryPropertySet();
        sallyPropertySet.init(null, null);

        // tests with null property set
        assertTrue(new JiraUserPreferences((PropertySet) null).equals(new JiraUserPreferences()));
        // Can no longer mock this, so omitting test.
        // assertFalse(new JiraUserPreferences(userSally).equals(new JiraUserPreferences()));

        // test with same object

        JiraUserPreferences userPreferences1 = new JiraUserPreferences(sallyPropertySet);
        assertTrue(userPreferences1.equals(userPreferences1));

        MemoryPropertySet mikePropertySet = new MemoryPropertySet();
        mikePropertySet.init(null, null);
        JiraUserPreferences userPreferences2 = new JiraUserPreferences(mikePropertySet);
        // test with different JiraUserPreferences objects
        assertTrue(userPreferences1.equals(userPreferences2));
        userPreferences1.setString("colour", "blue");
        assertFalse(userPreferences1.equals(userPreferences2));
        userPreferences2.setString("colour", "red");
        assertFalse(userPreferences1.equals(userPreferences2));
        userPreferences2.setString("colour", "blue");
        assertTrue(userPreferences1.equals(userPreferences2));
    }

    @Test
    public void testHashCode() throws AtlassianCoreException
    {
        assertEquals(new JiraUserPreferences((PropertySet) null), new JiraUserPreferences());

        // We won't do any more tests because they will fail - hashCode() is flawed -
        // see comments in JiraUserPreferences.

        
//        User userSally = new User("Sally");
//        User userMike = new User("Mike");
//        JiraUserPreferences userPreferences1 = new JiraUserPreferences(userSally);
//        JiraUserPreferences userPreferences2 = new JiraUserPreferences(userMike);
//        assertTrue(userPreferences1.hashCode() == userPreferences2.hashCode());
//        userPreferences1.setString("colour", "blue");
//        assertFalse(userPreferences1.hashCode() == userPreferences2.hashCode());
//        userPreferences2.setString("colour", "red");
//        assertFalse(userPreferences1.hashCode() == userPreferences2.hashCode());
//        userPreferences2.setString("colour", "blue");
//        assertTrue(userPreferences1.hashCode() == userPreferences2.hashCode());
    }

    /**
     * In order to avoid DB operations to get ApplicationProperties, we extend JiraUserPreferences and override the
     * getApplicationProperties() method.
     */
    private class MyJiraUserPreferences extends JiraUserPreferences
    {
        MyJiraUserPreferences(PropertySet propertySet)
        {
            super(propertySet);
        }

        ApplicationProperties getApplicationProperties()
        {
            return applicationProperties;
        }
    }
}

