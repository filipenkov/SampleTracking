package com.atlassian.crowd.embedded.directory;

import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CrowdDirectoryAttributesTest
{
    @Test
    public void testToAttributesMap() throws Exception
    {
        CrowdDirectoryAttributes crowdDirectoryAttributes = new CrowdDirectoryAttributes();
        crowdDirectoryAttributes.setApplicationName("Johnny");
        crowdDirectoryAttributes.setApplicationPassword("secret");
        crowdDirectoryAttributes.setCrowdServerUrl("http://cwd:999/crowd");
        crowdDirectoryAttributes.setNestedGroupsEnabled(false);
        crowdDirectoryAttributes.setIncrementalSyncEnabled(true);
        crowdDirectoryAttributes.setCrowdServerSynchroniseIntervalInSeconds("60");
        Map<String,String> map = crowdDirectoryAttributes.toAttributesMap();

        assertEquals(6, map.size());
        assertEquals("Johnny", map.get("application.name"));
        assertEquals("secret", map.get("application.password"));
        assertEquals("http://cwd:999/crowd", map.get("crowd.server.url"));
        assertEquals("false", map.get(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS));
        assertEquals("true", map.get(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED));
        assertEquals("60", map.get(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL));
    }
    
    @Test
    public void testFromAttributesMap() throws Exception
    {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("application.name", "xxx");
        map.put("application.password", "ppp");
        map.put("crowd.server.url", "http://cwd:999/crowd");
        map.put(DirectoryImpl.ATTRIBUTE_KEY_USE_NESTED_GROUPS, "true");
        map.put(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED, "true");
        map.put(SynchronisableDirectoryProperties.CACHE_SYNCHRONISE_INTERVAL, "60");
        CrowdDirectoryAttributes crowdDirectoryAttributes = CrowdDirectoryAttributes.fromAttributesMap(map);

        assertEquals("xxx", crowdDirectoryAttributes.getApplicationName());
        assertEquals("ppp", crowdDirectoryAttributes.getApplicationPassword());
        assertEquals("http://cwd:999/crowd", crowdDirectoryAttributes.getCrowdServerUrl());
        assertTrue(crowdDirectoryAttributes.isNestedGroupsEnabled());
        assertTrue(crowdDirectoryAttributes.isIncrementalSyncEnabled());
        assertEquals("60", crowdDirectoryAttributes.getCrowdServerSynchroniseIntervalInSeconds());
    }
}
