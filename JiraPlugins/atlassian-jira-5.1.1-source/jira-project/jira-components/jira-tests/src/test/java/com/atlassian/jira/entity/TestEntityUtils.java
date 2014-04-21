package com.atlassian.jira.entity;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import org.ofbiz.core.util.UtilMisc;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEntityUtils extends AbstractOFBizTestCase
{
    @Test
    public void testCreateValue() throws GenericEntityException
    {
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, new DefaultOfBizDelegator(OfBizFactory.getGenericDelegator()));
        ComponentAccessor.initialiseWorker(componentAccessorWorker);

        final GenericValue gv2 = EntityUtils.createValue("Component", UtilMisc.toMap("id", new Long(20), "name", "baz"));
        assertEquals(new Long(20), gv2.getLong("id"));
        assertEquals("Component", gv2.getEntityName());
        assertEquals("baz", gv2.getString("name"));

        final GenericValue gv = EntityUtils.createValue("Component", UtilMisc.toMap("name", "bar"));
        assertEquals(new Long(1), gv.getLong("id"));
        assertEquals("Component", gv.getEntityName());
        assertEquals("bar", gv.getString("name"));
    }

    // always test identicals both ways around just to make sure
    @Test
    public void testIdentical()
    {
        assertTrue(EntityUtils.identical(null, null));

        final GenericValue entity1 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1)));
        assertTrue(!EntityUtils.identical(entity1, null));
        assertTrue(!EntityUtils.identical(null, entity1));
        assertTrue(EntityUtils.identical(entity1, entity1));

        final GenericValue entity2 = new MockGenericValue("TestEntity2", UtilMisc.toMap("id", new Long(1)));
        assertTrue(!EntityUtils.identical(entity1, entity2));
        assertTrue(!EntityUtils.identical(entity2, entity1));

        final GenericValue entity3 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(2)));
        assertTrue(!EntityUtils.identical(entity1, entity3));
        assertTrue(!EntityUtils.identical(entity3, entity1));

        final GenericValue issue4 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "numvalue", new Long(4)));
        assertTrue(!EntityUtils.identical(entity1, issue4));
        assertTrue(!EntityUtils.identical(issue4, entity1));

        final GenericValue issue5 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "numvalue", null));
        assertTrue(EntityUtils.identical(entity1, issue5));
        assertTrue(EntityUtils.identical(issue5, entity1));

        final Timestamp ts = UtilDateTime.nowTimestamp();

        final GenericValue tsIssue = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts));
        final GenericValue tsIssue2 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts));
        assertTrue(EntityUtils.identical(tsIssue, tsIssue2));
        assertTrue(EntityUtils.identical(tsIssue2, tsIssue));

        final Timestamp ts2 = new Timestamp(ts.getTime() - 100000);
        final GenericValue tsIssue3 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts2));
        assertTrue(!EntityUtils.identical(tsIssue, tsIssue3));
        assertTrue(!EntityUtils.identical(tsIssue3, tsIssue));
    }
}
