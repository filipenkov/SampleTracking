package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LegacyBambooServerImplTest
{
    private LegacyBambooServerImpl bambooServer;

    @Before
    public void setUp()
    {
        bambooServer = new LegacyBambooServerImpl();
    }

    @Test
    public void testCreation()
    {
        assertEquals("Uninitialized object shall have id equal to 0", bambooServer.getId(), 0);

        assertNotNull("Uninitialized object shall have not null name", bambooServer.getName());
        assertTrue("Uninitialized object shall have empty name", StringUtils.isEmpty(bambooServer.getName()));

        assertNotNull("Uninitialized object shall have not null description", bambooServer.getDescription());
        assertTrue("Uninitialized object shall have empty description", StringUtils.isEmpty(bambooServer.getDescription()));

        assertNotNull("Uninitialized object shall have not null host", bambooServer.getHost());
        assertTrue("Uninitialized object shall have empty host", StringUtils.isEmpty(bambooServer.getHost()));

        assertNotNull("Uninitialized object shall have not null project keys collection", bambooServer.getAssociatedProjectKeys());
        assertTrue("Uninitialized object shall have empty project keys collection", bambooServer.getAssociatedProjectKeys().isEmpty());
    }

    @Test
    public void testGetSetId()
    {
        final int testId = new Random().nextInt();
        bambooServer.setId(testId);
        
        assertEquals(bambooServer.getId(), testId);
    }

    @Test
    public void testGetSetName()
    {
        final String testName = "abcdefghijklmnopqrstuvwxyz";
        bambooServer.setName(testName);

        assertEquals(bambooServer.getName(), testName);
    }

    @Test
    public void testGetSetDescription()
    {
        final String testDescription = "abcdefghijklmnopqrstuvwxyz\nABCDEFGHIJKLMNOPQRSTUVWXYZ";
        bambooServer.setDescription(testDescription);

        assertEquals(bambooServer.getDescription(), testDescription);
    }

    @Test
    public void testGetSetHost()
    {
        final String testHost = "http://www.host.internet.com";
        bambooServer.setHost(testHost);

        assertEquals(bambooServer.getHost(), testHost);
    }

    @Test
    public void testGetSetAssociatedProjectKeys()
    {
        final String testProjectKeysArray[] = new String[] {"TST", "ABC", "CDE"};
        bambooServer.setAssociatedProjectKeys(new HashSet(Arrays.asList(testProjectKeysArray)));

        assertEquals("There shall be 3 associated project keys", bambooServer.getAssociatedProjectKeys().size(), 3);
        for (String projectKey : testProjectKeysArray)
        {
            assertTrue(bambooServer.getAssociatedProjectKeys().contains(projectKey));
        }
    }

    @Test
    public void testAddAssociatedProjectKey()
    {
        final String testProjectKey = "TESTPROJECTKEY";
        bambooServer.addAssociatedProjectKey(testProjectKey);

        assertEquals(bambooServer.getAssociatedProjectKeys().size(), 1);
        assertTrue(bambooServer.getAssociatedProjectKeys().contains(testProjectKey));

        bambooServer.addAssociatedProjectKey(testProjectKey);
        assertEquals("Duplicate project key shall be eliminated", bambooServer.getAssociatedProjectKeys().size(), 1);
        assertTrue(bambooServer.getAssociatedProjectKeys().contains(testProjectKey));
    }

    @Test
    public void testRemoveAssociatedProjectKey()
    {
        final String testProjectKeysArray[] = new String[] {"TST", "ABC", "CDE"};
        bambooServer.setAssociatedProjectKeys(new HashSet(Arrays.asList(testProjectKeysArray)));
        assertEquals("There shall be 3 associated project keys", bambooServer.getAssociatedProjectKeys().size(), 3);

        bambooServer.removeAssociatedProjectKey(testProjectKeysArray[0]);
        assertEquals("There shall be 2 associated project keys", bambooServer.getAssociatedProjectKeys().size(), 2);
        assertFalse(bambooServer.getAssociatedProjectKeys().contains(testProjectKeysArray[0]));
        assertTrue(bambooServer.getAssociatedProjectKeys().contains(testProjectKeysArray[1]));
        assertTrue(bambooServer.getAssociatedProjectKeys().contains(testProjectKeysArray[2]));
    }
}