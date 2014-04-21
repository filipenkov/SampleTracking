package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestUpgradeTask_Build402 extends MockControllerTestCase
{
    @Test
    public void testDetails()
    {
        final UpgradeTask_Build402 upgradeTask = mockController.instantiate(UpgradeTask_Build402.class);
        assertEquals("402", upgradeTask.getBuildNumber());
        assertEquals("Charting plugin: Converting charting plugin portlets to system portlets.", upgradeTask.getShortDescription());
    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        JiraMockGenericValue mockPortletConfigGV = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10000), "portletId", "com.atlassian.jira.ext.charting:createdvsresolved"));
        JiraMockGenericValue mockPortletConfigGV1 = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10001), "portletId", "com.atlassian.jira.ext.charting:singlefieldpie"));
        JiraMockGenericValue mockPortletConfigGV2 = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10002), "portletId", "com.atlassian.jira.ext.charting:recentlycreated"));
        JiraMockGenericValue mockPortletConfigGV3 = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10003), "portletId", "com.atlassian.jira.ext.charting:resolutiontime"));
        JiraMockGenericValue mockPortletConfigGV4 = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10004), "portletId", "com.atlassian.jira.ext.charting:averageage"));
        JiraMockGenericValue mockPortletConfigGV5 = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("id", new Long(10005), "portletId", "com.atlassian.jira.ext.charting:timesince"));
        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV1);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV2);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV3);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV4);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV5);
        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        final Set<String> portletKeysToReplace = new HashSet<String>();
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:createdvsresolved");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:singlefieldpie");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:recentlycreated");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:timesince");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:averageage");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:resolutiontime");
        final EntityCondition portletKeysClause =
                new MockEntityExpr("portletId", EntityOperator.IN, portletKeysToReplace);
        ofBizDelegator.findListIteratorByCondition("PortletConfiguration", portletKeysClause);
        mockController.setReturnValue(mockIterator);

        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:createdvsresolved").toMap(),
                CollectionBuilder.newBuilder(10000L).asList());
        mockController.setReturnValue(1);
        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:pie").toMap(),
                CollectionBuilder.newBuilder(10001L).asList());
        mockController.setReturnValue(1);
        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:recentlycreated").toMap(),
                CollectionBuilder.newBuilder(10002L).asList());
        mockController.setReturnValue(1);
        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:resolutiontime").toMap(),
                CollectionBuilder.newBuilder(10003L).asList());
        mockController.setReturnValue(1);
        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:averageage").toMap(),
                CollectionBuilder.newBuilder(10004L).asList());
        mockController.setReturnValue(1);
        ofBizDelegator.bulkUpdateByPrimaryKey("PortletConfiguration",
                MapBuilder.<String, Object>newBuilder().add("portletId", "com.atlassian.jira.plugin.system.portlets:timesince").toMap(),
                CollectionBuilder.newBuilder(10005L).asList());
        mockController.setReturnValue(1);

        mockController.replay();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        final UpgradeTask_Build402 upgradeTask = new UpgradeTask_Build402(ofBizDelegator)
        {
            void flushPortletConfigurationCache()
            {
                flushCalled.set(true);
            }
        };

        upgradeTask.doUpgrade(false);
        assertTrue(flushCalled.get());

        mockController.verify();
    }

    //make sure we don't blow up if there's nothing to update
    @Test
    public void testDoUpgradeNothingToUpgrade() throws Exception
    {
        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        final Set<String> portletKeysToReplace = new HashSet<String>();
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:createdvsresolved");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:singlefieldpie");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:recentlycreated");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:timesince");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:averageage");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:resolutiontime");
        final EntityCondition portletKeysClause =
                new MockEntityExpr("portletId", EntityOperator.IN, portletKeysToReplace);
        ofBizDelegator.findListIteratorByCondition("PortletConfiguration", portletKeysClause);
        mockController.setReturnValue(mockIterator);

        mockController.replay();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        final UpgradeTask_Build402 upgradeTask = new UpgradeTask_Build402(ofBizDelegator)
        {
            void flushPortletConfigurationCache()
            {
                flushCalled.set(true);
            }
        };

        upgradeTask.doUpgrade(false);
        assertTrue(flushCalled.get());
    }

    @Test
    public void testDoUpgradeNothingToDo() throws Exception
    {
        //should never really happen since the condition prevents GVs with this portletKey from being returned
        JiraMockGenericValue mockPortletConfigGV = new JiraMockGenericValue("PortletConfiguration",
                EasyMap.build("portletId", "com.atlassian.jira.plugin.system.portlets:createdvsresolved"));
        final OfBizListIterator mockIterator = mockController.getMock(OfBizListIterator.class);
        mockIterator.next();
        mockController.setReturnValue(mockPortletConfigGV);
        mockIterator.next();
        mockController.setReturnValue(null);
        mockIterator.close();

        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        final Set<String> portletKeysToReplace = new HashSet<String>();
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:createdvsresolved");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:singlefieldpie");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:recentlycreated");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:timesince");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:averageage");
        portletKeysToReplace.add("com.atlassian.jira.ext.charting:resolutiontime");
        final EntityCondition portletKeysClause =
                new MockEntityExpr("portletId", EntityOperator.IN, portletKeysToReplace);
        ofBizDelegator.findListIteratorByCondition("PortletConfiguration", portletKeysClause);
        mockController.setReturnValue(mockIterator);

        mockController.replay();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        final UpgradeTask_Build402 upgradeTask = new UpgradeTask_Build402(ofBizDelegator)
        {
            void flushPortletConfigurationCache()
            {
                flushCalled.set(true);
            }
        };

        upgradeTask.doUpgrade(false);
        assertTrue(flushCalled.get());
        assertEquals("com.atlassian.jira.plugin.system.portlets:createdvsresolved", mockPortletConfigGV.getString("portletId"));
        assertFalse(mockPortletConfigGV.isStored());
    }


    class MockEntityExpr extends EntityExpr
    {
        MockEntityExpr(final String lhs, final EntityOperator operator, final Object rhs)
        {
            super(lhs, operator, rhs);
        }

        public boolean equals(final Object o)
        {
            if (!(o instanceof EntityExpr))
            {
                return false;
            }
            final EntityExpr that = (EntityExpr) o;
            boolean equals = that.getLhs().equals(getLhs()) && that.getOperator().equals(getOperator());
            if (that.getRhs() == null)
            {
                return equals && getRhs() == null;
            }
            else
            {
                return equals && that.getRhs().equals(getRhs());
            }
        }
    }

    class JiraMockGenericValue extends MockGenericValue
    {
        private boolean stored = false;

        public JiraMockGenericValue(String entityName, Map fields)
        {
            super(entityName, fields);
        }

        public void store() throws GenericEntityException
        {
            stored = true;
        }

        public boolean isStored()
        {
            return stored;
        }
    }
}
