package com.atlassian.jira.propertyset;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Holds unit tests for {@link DefaultJiraPropertySetFactory}
 *
 * @since v3.12
 */
public class TestDefaultJiraPropertySetFactory extends ListeningTestCase
{
    private static final String ENTITY_NAME = "entityName";
    private static final String ENTITY_ID = "entityId";
    private static final Long DEFAULT_ID = 1L;

    @Before
    public void setUpTenantContext()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Test
    public void testGetPropertySetWithDefaultEntityId()
    {
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory()
        {
            PropertySet createPropertySet(String propertySetDelegator, HashMap ofbizArgs)
            {
                assertEquals("ofbiz", propertySetDelegator);
                assertNotNull(ofbizArgs);
                assertEquals("testEntity", ofbizArgs.get(ENTITY_NAME));
                assertEquals(DEFAULT_ID, ofbizArgs.get(ENTITY_ID));
                assertEquals("default", ofbizArgs.get("delegator.name"));
                return null;
            }
        };

        defaultJiraPropertySetManager.buildNoncachingPropertySet("testEntity");
    }

    @Test
    public void testGetPropertSetWithEntityId()
    {
        final Long id = 20L;
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory()
        {

            PropertySet createPropertySet(String propertySetDelegator, HashMap ofbizArgs)
            {
                assertEquals("ofbiz", propertySetDelegator);
                assertNotNull(ofbizArgs);
                assertEquals("testEntity", ofbizArgs.get(ENTITY_NAME));
                assertEquals(id, ofbizArgs.get(ENTITY_ID));
                assertEquals("default", ofbizArgs.get("delegator.name"));
                return null;
            }
        };

        defaultJiraPropertySetManager.buildNoncachingPropertySet("testEntity", id);
    }

    @Test
    public void testGetCachingPropertySet()
    {
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory()
        {
            int count = 0;

            PropertySet createPropertySet(String propertySetDelegator, HashMap ofbizArgs)
            {
                if (count == 0)
                {
                    assertEquals("ofbiz", propertySetDelegator);
                    assertNotNull(ofbizArgs);
                    assertEquals("testEntity", ofbizArgs.get(ENTITY_NAME));
                    assertEquals(DEFAULT_ID, ofbizArgs.get(ENTITY_ID));
                    assertEquals("default", ofbizArgs.get("delegator.name"));
                }
                else if (count == 1)
                {
                    assertEquals("cached", propertySetDelegator);
                    assertNotNull(ofbizArgs);
                    assertNotNull(ofbizArgs.get("PropertySet"));
                    assertTrue((Boolean) ofbizArgs.get("bulkload"));
                }
                count++;

                return new JiraCachingPropertySet();
            }
        };

        defaultJiraPropertySetManager.buildCachingDefaultPropertySet("testEntity", true);
    }

    @Test
    public void testGetCachingPropertySetWithEntityId()
    {
        final Long entityId = 20L;
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory()
        {
            int count = 0;

            PropertySet createPropertySet(String propertySetDelegator, HashMap ofbizArgs)
            {
                if (count == 0)
                {
                    assertEquals("ofbiz", propertySetDelegator);
                    assertNotNull(ofbizArgs);
                    assertEquals("testEntity", ofbizArgs.get(ENTITY_NAME));
                    assertEquals(entityId, ofbizArgs.get(ENTITY_ID));
                    assertEquals("default", ofbizArgs.get("delegator.name"));
                }
                else if (count == 1)
                {
                    assertEquals("cached", propertySetDelegator);
                    assertNotNull(ofbizArgs);
                    assertNotNull(ofbizArgs.get("PropertySet"));
                    assertTrue((Boolean) ofbizArgs.get("bulkload"));
                }
                count++;

                return new JiraCachingPropertySet();
            }
        };

        defaultJiraPropertySetManager.buildCachingPropertySet("testEntity", entityId, true);
    }

    @Test
    public void testCachingPropertySetWithNullParameter()
    {
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory();
        try
        {
            defaultJiraPropertySetManager.buildCachingPropertySet(null, false);
            fail("Should have thrown NullPointerException");
        }
        catch (NullPointerException e)
        {
            // yay
        }
    }
}
