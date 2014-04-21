package com.atlassian.jira.ofbiz;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericModelException;

import java.util.Collections;

public class TestDefaultOfBizDelegator extends LegacyJiraMockTestCase
{
    private static final String ENTITY_NAME = "Project";
    private static final String ENTITY_ID = "id";
    private static final Long PROJECT_ID_1 = new Long(1);
    private static final Long PROJECT_ID_2 = new Long(2);
    private static final Long PROJECT_ID_3 = new Long(3);

    private OfBizDelegator ofBizDelegator = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());

        // Setup of entities for the tests.
        UtilsForTests.getTestEntity(ENTITY_NAME, EasyMap.build(ENTITY_ID, PROJECT_ID_1, "name", "test project 1"));
        UtilsForTests.getTestEntity(ENTITY_NAME, EasyMap.build(ENTITY_ID, PROJECT_ID_2, "name", "test project 2"));
        UtilsForTests.getTestEntity(ENTITY_NAME, EasyMap.build(ENTITY_ID, PROJECT_ID_3, "name", "test project 3"));
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testRemoveByOr() throws GenericModelException
    {
        // remove one entity and leave 2
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, EasyList.build(PROJECT_ID_1));
        assertEquals(1, totalRemoved);

        // Assert that two are still present
        long entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(2L, entitiesPresent);

        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "2");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, EasyList.build(PROJECT_ID_2, PROJECT_ID_3));
        assertEquals(2, totalRemoved);

        // Assert that no entities are present
        entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(0L, entitiesPresent);
    }

    public void testRemoveByOrWithEmptyList() throws GenericModelException
    {
        int results = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, Collections.EMPTY_LIST);
        assertEquals(0, results);

        // Assert that we still have 3 entities, and non were deleted
        long entitiesPresent = ofBizDelegator.getCount(ENTITY_NAME);
        assertEquals(3L, entitiesPresent);
    }

    public void testRemoveByOrWithInvalidParam()
    {
        try
        {
            ofBizDelegator.removeByOr(ENTITY_NAME, "notandentityvalue", Collections.EMPTY_LIST);
            fail("We should have thrown an GenericModelException");
        }
        catch (GenericModelException e)
        {
            // this is expected
        }

        try
        {
            ofBizDelegator.removeByOr("notanentity", ENTITY_ID, Collections.EMPTY_LIST);
            fail("We should have thrown an GenericModelException");
        }
        catch (GenericModelException e)
        {
            // this is expected
        }

    }

    public void testRemoveByOrWithLargeBatchSize() throws GenericModelException
    {
        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "5");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, EasyList.build(PROJECT_ID_1, PROJECT_ID_2, PROJECT_ID_3));
        assertEquals(3, totalRemoved);
    }

    public void testRemoveByOrWithInvalidIDs() throws GenericModelException
    {
        // Set the batch size to one.
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "5");

        // Remove both entities with a batch size set to 1
        // This will make sure that we actually batch our queries
        int totalRemoved = ofBizDelegator.removeByOr(ENTITY_NAME, ENTITY_ID, EasyList.build(PROJECT_ID_1, new Long(2000000), PROJECT_ID_3));
        assertEquals(2, totalRemoved);
    }

}