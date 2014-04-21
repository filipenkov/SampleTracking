package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.SchemePermissions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A common base class for Workflow Plugin Factories that are concerned with Permissions.
 *
 * @since v3.13
 */
public abstract class AbstractWorkflowPermissionPluginFactory extends AbstractWorkflowPluginFactory
{
    private final SchemePermissions schemePermissions;

    public AbstractWorkflowPermissionPluginFactory(final SchemePermissions schemePermissions)
    {
        this.schemePermissions = schemePermissions;
    }

    @Override
    protected void getVelocityParamsForInput(final Map<String, Object> velocityParams)
    {
        final Map<String, ?> permissions = getGroupedPermissions();
        velocityParams.put("permissions", Collections.unmodifiableMap(permissions));
    }

    /**
     * JRA-14306: building a map of maps, so we can use optgroups to display each group of permissions.
     *
     * Note: we have to catch any permissions that are in schemePermissions.getSchemePermissions() but not covered
     * by the specific group permissions
     *
     * @return a Map with keys as the i18n key of the group, and the value as the map of permissions for that group.
     */
    protected Map<String, Map<Integer, Permission>> getGroupedPermissions()
    {
        // add all the grouped permissions
        final Map<String, Map<Integer, Permission>> permissions = new LinkedHashMap<String, Map<Integer, Permission>>();
        permissions.put("admin.permission.group.project.permissions", schemePermissions.getProjectPermissions());
        permissions.put("admin.permission.group.issue.permissions", schemePermissions.getIssuePermissions());
        permissions.put("admin.permission.group.voters.and.watchers.permissions", schemePermissions.getVotersAndWatchersPermissions());
        permissions.put("admin.permission.group.comments.permissions", schemePermissions.getCommentsPermissions());
        permissions.put("admin.permission.group.attachments.permissions", schemePermissions.getAttachmentsPermissions());
        permissions.put("admin.permission.group.time.tracking.permissions", schemePermissions.getTimeTrackingPermissions());

        // check for permissions that weren't in the above groups
        final Set<Integer> otherPermissionKeys = new TreeSet<Integer>(schemePermissions.getSchemePermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getProjectPermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getIssuePermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getVotersAndWatchersPermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getCommentsPermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getAttachmentsPermissions().keySet());
        otherPermissionKeys.removeAll(schemePermissions.getTimeTrackingPermissions().keySet());

        // if we have others, add them to a separate "other" group
        if (!otherPermissionKeys.isEmpty())
        {
            final Map<Integer, Permission> otherPermissions = new LinkedHashMap<Integer, Permission>();
            for (final Integer key : otherPermissionKeys)
            {
                otherPermissions.put(key, schemePermissions.getSchemePermissions().get(key));
            }
            permissions.put("admin.permission.group.other.permissions", otherPermissions);
        }
        return permissions;
    }

}
