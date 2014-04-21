package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestUpgradeTask_Build446 extends MockControllerTestCase
{

    @Test
    public void testDoUpgrade() throws Exception
    {
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        final OfBizDelegator mockOfBizDelegator = mockController.getMock(OfBizDelegator.class);
        final OfBizListIterator mockOfBizListIterator = mockController.getMock(OfBizListIterator.class);
        expect(mockOfBizListIterator.next()).andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.singletonMap("id", 10000L)));
        expect(mockOfBizListIterator.next()).andReturn(new MockGenericValue("PortletConfiguration", MapBuilder.singletonMap("id", 10020L)));
        expect(mockOfBizListIterator.next()).andReturn(null);
        mockOfBizListIterator.close();

        expect(mockOfBizDelegator.findListIteratorByCondition("PortletConfiguration", new MockEntityExpr(OfbizPortletConfigurationStore.Columns.PORTLETKEY, EntityOperator.IN,
                CollectionBuilder.newBuilder("com.atlassian.jira.plugin.ext.bamboo:bambooStatus", "com.atlassian.jira.plugin.ext.bamboo:buildGraph").asList()))).andReturn(mockOfBizListIterator);

        final PropertySet mockPs = mockController.getMock(PropertySet.class);
        expect(mockPs.getKeys()).andReturn(CollectionBuilder.newBuilder("key1", "key2").asList());
        mockPs.remove("key1");
        mockPs.remove("key2");
        final PropertySet mockPs2 = mockController.getMock(PropertySet.class);
        expect(mockPs.getKeys()).andReturn(Collections.emptyList());

        final JiraPropertySetFactory mockJiraPropertySetFactory = mockController.getMock(JiraPropertySetFactory.class);
        expect(mockJiraPropertySetFactory.buildNoncachingPropertySet("PortletConfiguration", 10000L)).andReturn(mockPs);
        expect(mockJiraPropertySetFactory.buildNoncachingPropertySet("PortletConfiguration", 10020L)).andReturn(mockPs2);

        UpgradeTask_Build446 upgradeTask_build446 = new UpgradeTask_Build446(mockOfBizDelegator, mockJiraPropertySetFactory)
        {
            @Override
            void flushPortletConfigurationCache()
            {
                flushCalled.set(true);
            }
        };

        replay(mockJiraPropertySetFactory, mockOfBizDelegator, mockOfBizListIterator, mockPs, mockPs2);
        upgradeTask_build446.doUpgrade(false);

        assertTrue(flushCalled.get());
        verify(mockJiraPropertySetFactory, mockOfBizDelegator, mockOfBizListIterator, mockPs, mockPs2);
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
            final boolean equals = that.getLhs().equals(getLhs()) && that.getOperator().equals(getOperator());
            if (that.getRhs() == null)
            {
                return equals && (getRhs() == null);
            }
            else
            {
                return equals && that.getRhs().equals(getRhs());
            }
        }
    }
}
