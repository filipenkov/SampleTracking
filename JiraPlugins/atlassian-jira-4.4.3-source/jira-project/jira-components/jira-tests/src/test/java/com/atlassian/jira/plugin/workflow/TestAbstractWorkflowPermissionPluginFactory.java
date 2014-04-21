package com.atlassian.jira.plugin.workflow;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.permission.PermissionImpl;
import com.atlassian.jira.permission.SchemePermissions;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Map;
import java.util.Set;

public class TestAbstractWorkflowPermissionPluginFactory extends ListeningTestCase
{
    @Test
    public void testGetGroupedPermissionsWithNoOther()
    {
        SchemePermissions sp = new MockSchemePermissions(false);
        AbstractWorkflowPermissionPluginFactory pf = createWorkflowPluginFactory(sp);
        Set i18nKeys = pf.getGroupedPermissions().keySet();
        assertFalse(i18nKeys.contains("admin.permission.group.other.permissions"));
    }

    @Test
    public void testGetGroupedPermissionsWithOthers()
    {
        SchemePermissions sp = new MockSchemePermissions(true);
        AbstractWorkflowPermissionPluginFactory pf = createWorkflowPluginFactory(sp);
        Set i18nKeys = pf.getGroupedPermissions().keySet();
        assertTrue(i18nKeys.contains("admin.permission.group.other.permissions"));
    }

    private AbstractWorkflowPermissionPluginFactory createWorkflowPluginFactory(final SchemePermissions sp)
    {
        return new AbstractWorkflowPermissionPluginFactory(sp)
        {
            protected void getVelocityParamsForEdit(final Map velocityParams, final AbstractDescriptor descriptor)
            {
            }

            protected void getVelocityParamsForView(final Map velocityParams, final AbstractDescriptor descriptor)
            {
            }

            public Map getDescriptorParams(final Map formParams)
            {
                return null;
            }
        };
    }

    private static class MockSchemePermissions extends SchemePermissions
    {
        private boolean hasOthers = false;

        private MockSchemePermissions(final boolean hasOthers)
        {
            this.hasOthers = hasOthers;
        }

        public synchronized Map getProjectPermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(1), new PermissionImpl("1", "1", "1", "1", "1"));
            return pp;
        }

        public synchronized Map getIssuePermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(2), new PermissionImpl("2", "2", "2", "2", "2"));
            return pp;
        }

        public synchronized Map getVotersAndWatchersPermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(3), new PermissionImpl("3", "3", "3", "3", "3"));
            return pp;
        }

        public synchronized Map getCommentsPermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(4), new PermissionImpl("4", "4", "4", "4", "4"));
            return pp;
        }

        public synchronized Map getAttachmentsPermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(5), new PermissionImpl("5", "5", "5", "5", "5"));
            return pp;
        }

        public synchronized Map getTimeTrackingPermissions()
        {
            Map pp = new ListOrderedMap();
            pp.put(new Integer(6), new PermissionImpl("6", "6", "6", "6", "6"));
            return pp;
        }

        public synchronized Map getSchemePermissions()
        {
            Map pp = new ListOrderedMap();
            pp.putAll(getProjectPermissions());
            pp.putAll(getIssuePermissions());
            pp.putAll(getVotersAndWatchersPermissions());
            pp.putAll(getCommentsPermissions());
            pp.putAll(getAttachmentsPermissions());
            pp.putAll(getTimeTrackingPermissions());
            if (hasOthers)
            {
                pp.put(new Integer(7), new PermissionImpl("7", "7", "7", "7", "7"));
            }
            return pp;
        }
    }
}
