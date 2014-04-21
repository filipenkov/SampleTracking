package com.atlassian.jira.plugin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tests the OfBizPluginVersionStore.
 *
 * @since v3.13
 */
public class TestOfBizPluginVersionStore extends LegacyJiraMockTestCase
{
    private static final String TEST_KEY = "test.key";
    private static final String TEST_NAME = "Test Name";
    private static final String TEST_VERSION = "0.1.1";


    protected void setUp() throws Exception
    {
        super.setUp();
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        ofBizDelegator.removeByAnd(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build());
    }

    public void testCreate() throws GenericEntityException
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        List all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertTrue(all.isEmpty());

        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        final Date created = new Date();
        PluginVersion newPluginVersion = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, created);
        final PluginVersion pluginVersion = ofBizPluginVersionStore.create(newPluginVersion);

        all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertEquals(1, all.size());
        GenericValue pluginVersionGV = (GenericValue) all.get(0);
        assertEquals(pluginVersion.getId(), pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertEquals(pluginVersion.getKey(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_KEY));
        assertEquals(pluginVersion.getName(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_NAME));
        assertEquals(pluginVersion.getVersion(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION));
        assertEquals(pluginVersion.getCreated().getTime(), pluginVersionGV.getTimestamp(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED).getTime());
    }

    public void testCreateNullPluginVersion()
    {
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);

        try
        {
            ofBizPluginVersionStore.create(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // we want this
        }
    }

    public void testUpdate()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        List all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertTrue(all.isEmpty());
        GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION));

        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        PluginVersion pluginVersion = new PluginVersionImpl(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID),
                "new.key", "New Name", "0.2.2", new Date());
        final PluginVersion updatedPluginVersion = ofBizPluginVersionStore.update(pluginVersion);

        pluginVersionGV = ofBizDelegator.findByPrimaryKey(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_ID, pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID)));
        assertNotNull(pluginVersionGV);
        assertEquals(updatedPluginVersion.getId(), pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertEquals(updatedPluginVersion.getKey(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_KEY));
        assertEquals(updatedPluginVersion.getName(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_NAME));
        assertEquals(updatedPluginVersion.getVersion(), pluginVersionGV.getString(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION));
        assertEquals(updatedPluginVersion.getCreated().getTime(), pluginVersionGV.getTimestamp(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED).getTime());
    }

    public void testUpdateNullId()
    {
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);

        try
        {
            PluginVersion pluginVersion = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, new Date());
            ofBizPluginVersionStore.update(pluginVersion);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            // This should happen
        }
    }

    public void testUpdateNullPluginVersion()
    {
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);

        try
        {
            ofBizPluginVersionStore.update(null);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            // This should happen
        }
    }

    public void testUpdateNoRecordForId()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        try
        {
            PluginVersion pluginVersion = new PluginVersionImpl(new Long(54231), TEST_KEY, TEST_NAME, TEST_VERSION, new Date());
            ofBizPluginVersionStore.update(pluginVersion);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            // This should happen
        }
    }

    public void testDelete()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION));
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        
        assertTrue(ofBizPluginVersionStore.delete(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID)));

        List all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertTrue(all.isEmpty());
    }
    
    public void testDeleteNoId()
    {
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);

        try
        {
            ofBizPluginVersionStore.delete(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // We want this
        }
    }

    public void testDeleteNoRecordToDelete()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        assertFalse(ofBizPluginVersionStore.delete(new Long(10000)));

        List all = ofBizDelegator.findAll(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME);
        assertTrue(all.isEmpty());
    }

    public void testGetById()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        final long time = System.currentTimeMillis();
        GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(time)));
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);

        final PluginVersion pluginVersion = ofBizPluginVersionStore.getById(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertNotNull(pluginVersion);
        assertEquals(TEST_KEY, pluginVersion.getKey());
        assertEquals(TEST_NAME, pluginVersion.getName());
        assertEquals(TEST_VERSION, pluginVersion.getVersion());
        assertEquals(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID), pluginVersion.getId());
        assertEquals(time, pluginVersion.getCreated().getTime());
    }

    public void testGetByIdNoRecord()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        assertNull(ofBizPluginVersionStore.getById(new Long(123)));
    }

    public void testGetByIdNullId()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        assertNull(ofBizPluginVersionStore.getById(null));
    }

    public void testGetAll()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(ofBizDelegator);
        // Test where there are none
        assertTrue(ofBizPluginVersionStore.getAll().isEmpty());

        // Add 2
        final long time = System.currentTimeMillis();
        ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(time)));
        ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, new Timestamp(System.currentTimeMillis())));

        final List all = ofBizPluginVersionStore.getAll();
        assertEquals(2, all.size());
        PluginVersion pluginVersion = (PluginVersion) all.get(0);
        assertEquals(TEST_KEY, pluginVersion.getKey());
        assertEquals(TEST_NAME, pluginVersion.getName());
        assertEquals(TEST_VERSION, pluginVersion.getVersion());
        assertEquals(time, pluginVersion.getCreated().getTime());
    }

    public void testConvertToParams()
    {
        final Long id = new Long(54231);
        final Date created = new Date();
        PluginVersion pluginVersion = new PluginVersionImpl(id, TEST_KEY, TEST_NAME, TEST_VERSION, created);
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        final Map map = ofBizPluginVersionStore.convertToParams(pluginVersion);
        assertEquals(id, map.get(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertEquals(TEST_KEY, map.get(OfBizPluginVersionStore.PLUGIN_VERSION_KEY));
        assertEquals(TEST_NAME, map.get(OfBizPluginVersionStore.PLUGIN_VERSION_NAME));
        assertEquals(TEST_VERSION, map.get(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION));
        assertEquals(created.getTime(), ((Date) map.get(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED)).getTime());

        PluginVersion pluginVersionNoId = new PluginVersionImpl(TEST_KEY, TEST_NAME, TEST_VERSION, created);
        final Map mapNoId = ofBizPluginVersionStore.convertToParams(pluginVersionNoId);
        assertNull(mapNoId.get(OfBizPluginVersionStore.PLUGIN_VERSION_ID));
        assertEquals(TEST_KEY, mapNoId.get(OfBizPluginVersionStore.PLUGIN_VERSION_KEY));
        assertEquals(TEST_NAME, mapNoId.get(OfBizPluginVersionStore.PLUGIN_VERSION_NAME));
        assertEquals(TEST_VERSION, mapNoId.get(OfBizPluginVersionStore.PLUGIN_VERSION_VERSION));
        assertEquals(created.getTime(), ((Date) mapNoId.get(OfBizPluginVersionStore.PLUGIN_VERSION_CREATED)).getTime());
    }

    public void testConvertFromGV()
    {
        OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        final Timestamp time = new Timestamp(System.currentTimeMillis());
        GenericValue pluginVersionGV = ofBizDelegator.createValue(OfBizPluginVersionStore.PLUGIN_VERSION_ENTITY_NAME, EasyMap.build(OfBizPluginVersionStore.PLUGIN_VERSION_KEY, TEST_KEY,
                OfBizPluginVersionStore.PLUGIN_VERSION_NAME, TEST_NAME, OfBizPluginVersionStore.PLUGIN_VERSION_VERSION, TEST_VERSION, OfBizPluginVersionStore.PLUGIN_VERSION_CREATED, time));
        OfBizPluginVersionStore ofBizPluginVersionStore = new OfBizPluginVersionStore(null);
        final PluginVersion pluginVersion = ofBizPluginVersionStore.convertFromGV(pluginVersionGV);
        assertEquals(TEST_KEY, pluginVersion.getKey());
        assertEquals(TEST_NAME, pluginVersion.getName());
        assertEquals(TEST_VERSION, pluginVersion.getVersion());
        assertEquals(pluginVersionGV.getLong(OfBizPluginVersionStore.PLUGIN_VERSION_ID), pluginVersion.getId());
        assertEquals(time.getTime(), pluginVersion.getCreated().getTime());
    }
}
