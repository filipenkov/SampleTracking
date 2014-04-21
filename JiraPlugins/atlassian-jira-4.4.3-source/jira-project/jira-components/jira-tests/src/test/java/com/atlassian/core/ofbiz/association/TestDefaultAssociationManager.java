package com.atlassian.core.ofbiz.association;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.collection.EasyList;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.ofbiz.core.entity.GenericEntityException;

public class TestDefaultAssociationManager extends ListeningTestCase
{
    private static class LocalBoolean
    {
        private boolean value;

        private LocalBoolean(final boolean value)
        {
            this.value = value;
        }

        public boolean get()
        {
            return value;
        }

        public void set(final boolean value)
        {
            this.value = value;
        }

    }


    @Test
    public void testGetUsernamesFromSinkNullSink() throws Exception
    {
        try
        {
            new DefaultAssociationManager(CoreFactory.getGenericDelegator()).getUsernamesFromSink(null, null, false, false);
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testGetUsernamesFromSinkNoResults() throws Exception
    {
        final LocalBoolean methodCalled = new LocalBoolean(false);

        final MockGenericValue sink = new MockGenericValue("name");
        sink.set("id", new Long(123));

        final String associationType = "type";
        final boolean useCache = true;
        final boolean useSequence = false;

        DefaultAssociationManager manager = new DefaultAssociationManager(CoreFactory.getGenericDelegator())
        {
            List getAssociations(String associationName, Map fields, boolean uc, boolean us)
                    throws GenericEntityException
            {
                methodCalled.set(true);
                assertEquals("UserAssociation", associationName);
                assertNotNull(fields);
                assertEquals(3, fields.size());
                assertTrue(fields.containsKey("sinkNodeId"));
                assertTrue(fields.containsKey("sinkNodeEntity"));
                assertTrue(fields.containsKey("associationType"));
                assertEquals(new Long(123), fields.get("sinkNodeId"));
                assertEquals("name", fields.get("sinkNodeEntity"));
                assertEquals(associationType, fields.get("associationType"));
                assertEquals(useCache, uc);
                assertEquals(useSequence, us);
                return Collections.EMPTY_LIST;
            }
        };
        final List usernames = manager.getUsernamesFromSink(sink, associationType, useCache, useSequence);
        assertNotNull(usernames);
        assertTrue(usernames.isEmpty());
        assertTrue(methodCalled.get());
    }

    @Test
    public void testGetUsernamesFromSink() throws Exception
    {
        final String username = "fred";

        final LocalBoolean methodCalled = new LocalBoolean(false);

        final MockGenericValue sink = new MockGenericValue("name");
        sink.set("id", new Long(123));

        final String associationType = "type";
        final boolean useCache = true;
        final boolean useSequence = false;

        DefaultAssociationManager manager = new DefaultAssociationManager(CoreFactory.getGenericDelegator())
        {
            List getAssociations(String associationName, Map fields, boolean uc, boolean us)
                    throws GenericEntityException
            {
                methodCalled.set(true);
                assertEquals("UserAssociation", associationName);
                assertNotNull(fields);
                assertEquals(3, fields.size());
                assertTrue(fields.containsKey("sinkNodeId"));
                assertTrue(fields.containsKey("sinkNodeEntity"));
                assertTrue(fields.containsKey("associationType"));
                assertEquals(new Long(123), fields.get("sinkNodeId"));
                assertEquals("name", fields.get("sinkNodeEntity"));
                assertEquals(associationType, fields.get("associationType"));
                assertEquals(useCache, uc);
                assertEquals(useSequence, us);

                final MockGenericValue association = new MockGenericValue("assoc");
                association.setString("sourceName", username);
                return EasyList.build(association);
            }
        };
        final List usernames = manager.getUsernamesFromSink(sink, associationType, useCache, useSequence);
        assertNotNull(usernames);
        assertFalse(usernames.isEmpty());
        assertEquals(username, usernames.get(0));
        assertTrue(methodCalled.get());
    }


}
