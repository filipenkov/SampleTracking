package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;

import com.atlassian.jira.plugin.ext.bamboo.upgrade.AbstractPropertySetBasedTest;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.LegacyBambooServer;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util.LegacyBambooPropertyManager;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util.LegacyBambooServerIdGenerator;
import com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.util.LegacyBambooServerIdGeneratorImpl;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyBambooServerManagerTest extends AbstractPropertySetBasedTest
{
    @Mock private LegacyBambooPropertyManager propertyManager;
    @Mock private LegacyBambooServerManager bambooServerManager;

    private LegacyBambooServerIdGenerator bambooServerIdGenerator;
    
    @Before
    public void setUp()
    {
        setUpPropertySet();
        when(propertyManager.getPropertySet()).thenReturn(propertySet);
        bambooServerIdGenerator = new LegacyBambooServerIdGeneratorImpl(propertyManager);
    }

    @Override
    protected void init(String propertiesFileName) throws Exception
    {
        super.init(propertiesFileName);
        //initialize the server manager once the properties have been read in.
        bambooServerManager = new LegacyBambooServerManagerImpl(propertyManager, bambooServerIdGenerator);
    }

    @Test
    public void testPropertiesVersionPre22ProperlyConverted() throws Exception
    {
        init("BambooServerManagerImpl-pre22.properties");

        verifyPropertySetWithProperties("BambooServerManagerImpl-pre22-converted.properties");
    }

    @Test
    public void testPropertiesVersionPre22ProperlyRead() throws Exception
    {
        init("BambooServerManagerImpl-pre22.properties");

        Iterable<LegacyBambooServer> bambooServers = bambooServerManager.getServers();
        LegacyBambooServer serverWithId1 = getServerById(1);

        assertEquals("Number of servers shall be 1", 1, Iterables.size(bambooServers));
        assertEquals(1, serverWithId1.getId());
        assertEquals("TST", serverWithId1.getName());
        assertEquals(null, serverWithId1.getDescription());
        assertEquals("http://bamboo.atlassian.com", serverWithId1.getHost());
        assertEquals(0, serverWithId1.getAssociatedProjectKeys().size());
    }

    @Test
    public void testGetUsername() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");
        assertTrue(Iterables.get(bambooServerManager.getServers(), 0).getUsername().equals("test"));
    }

    @Test
    public void testGetEncryptedPassword() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");
        assertTrue(Iterables.get(bambooServerManager.getServers(), 0).getPassword().equals("test"));
    }

    @Test
    public void testHasServers() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");
        assertTrue(bambooServerManager.hasServers());
    }

    @Test
    public void testHasNoServers() throws Exception
    {
        init("BambooServerManagerImpl-v22-no-servers.properties");
        assertFalse(bambooServerManager.hasServers());
    }

    @Test
    public void testGetServersContainsExpectedServers() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");

        assertTrue(getServerByName("TST01") != null);
        assertTrue(getServerByName("TST02") != null);
        assertTrue(getServerByName("TST03") != null);
        assertTrue(getServerByName("TST-ABC") == null);
        assertTrue(getServerByName("TST-DEF") == null);
        assertTrue(getServerByName("TST-GHI") == null);
    }

    @Test
    public void testGetServersEmpty() throws Exception
    {
        init("BambooServerManagerImpl-v22-no-servers.properties");

        assertNotNull(bambooServerManager.getServers());
        assertTrue(Iterables.isEmpty(bambooServerManager.getServers()));
    }

    @Test
    public void testGetServersNotEmpty() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");

        assertNotNull(bambooServerManager.getServers());
        assertEquals(3, Iterables.size(bambooServerManager.getServers()));
    }

    @Test
    @Ignore
    public void testIsDefaultServer() throws Exception
    {
        init("BambooServerManagerImpl-v22.properties");

        assertTrue(bambooServerManager.isDefaultServer(getServerByName("TST01")));
        assertFalse(bambooServerManager.isDefaultServer(getServerByName("TST02")));
    }

    /**
     * Get the legacy bamboo server with the given id. Used to be part of the BambooServerManager before legacy-izing it.
     * But since it is no longer needed by the code, I removed the function and thus have to move it here as it is
     * only used for testing purposes.
     */
    private LegacyBambooServer getServerById(int id)
    {
        for (LegacyBambooServer server : bambooServerManager.getServers())
        {
            if (server.getId() == id)
            {
                return server;
            }
        }

        return null;
    }

    /**
     * Get the legacy bamboo server with the given id. Used to be part of the BambooServerManager before legacy-izing it.
     * But since it is no longer needed by the code, I removed the function and thus have to move it here as it is
     * only used for testing purposes.
     */
    private LegacyBambooServer getServerByName(String name)
    {
        for (LegacyBambooServer server : bambooServerManager.getServers())
        {
            if (server.getName().equals(name))
            {
                return server;
            }
        }

        return null;
    }
}
