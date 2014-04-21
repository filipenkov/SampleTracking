package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.util.UserUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

public class DefaultGlobalPermissionManager implements GlobalPermissionManager, Startable
{
    private final GlobalPermissionsCache cache;
    private final EventPublisher eventPublisher;
    private final CrowdService crowdService;

    public DefaultGlobalPermissionManager(final EventPublisher eventPublisher, final CrowdService crowdService)
    {
        this.eventPublisher = eventPublisher;
        this.crowdService = crowdService;
        cache = new GlobalPermissionsCache();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.refresh();
    }

    /**
     * Adds a global permission
     *
     * @param permissionId must be a global permission type
     * @param group        can be null if it is anyone permission
     * @return True if the permission was added
     * @throws CreateException
     */
    public boolean addPermission(final int permissionId, final String group) throws CreateException
    {
        if (!Permissions.isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed must be a global permissions " + permissionId + " is not");
        }
        // as a final check we dont allow Permissions.USE to be added to the group Anyone (null).  It should be protected by the UI
        // so as a last resort we check it here.
        if ((permissionId == Permissions.USE) && (group == null))
        {
            throw new IllegalArgumentException("The group Anyone cannot be added to the global permission JIRA Users");
        }

        try
        {
            EntityUtils.createValue("SchemePermissions", EasyMap.build("permission", new Long(permissionId), "type", GroupDropdown.DESC, "parameter",
                group));
            cache.refresh();
            clearActiveUserCountIfNecessary(permissionId);
            return true;
        }
        catch (final GenericEntityException e)
        {
            throw new CreateException(e);
        }
    }

    public Collection<JiraPermission> getPermissions(final int permissionType)
    {
        return cache.getPermissions(permissionType);
    }

    /**
     * Removes a global permission
     *
     * @param permissionId must be a global permission type
     * @param group        can be null if it is anyone permission
     * @return True if the permission was removed, false if not (usually it didn't exist)
     * @throws RemoveException
     */
    public boolean removePermission(final int permissionId, final String group) throws RemoveException
    {
        if (!Permissions.isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must be a global permission, " + permissionId + " is not");
        }

        final JiraPermission jiraPermission = new JiraPermission(permissionId, group, GroupDropdown.DESC);
        if (hasPermission(jiraPermission))
        {
            final GenericValue permGV = cache.getPermission(jiraPermission);
            try
            {
                CoreFactory.getGenericDelegator().removeAll(EasyList.build(permGV));
                cache.refresh();
                clearActiveUserCountIfNecessary(permissionId);
                return true;
            }
            catch (final GenericEntityException e)
            {
                throw new RemoveException(e);
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Remove a global permissions that the group passed in
     *
     * @param group must NOT be null and the group must exist
     * @return True all the permissions are removed
     */
    public boolean removePermissions(final String group) throws RemoveException
    {
        if (group == null)
        {
            throw new IllegalArgumentException("Group passed must NOT be null");
        }
        if (crowdService.getGroup(group) == null)
        {
            throw new IllegalArgumentException("Group passed must exist");
        }

        final Set<JiraPermission> permissions = cache.getPermissions();
        for (final JiraPermission permission : permissions)
        {
            if (group.equals(permission.getGroup()))
            {
                try
                {
                    cache.getPermission(permission).remove();
                    clearActiveUserCountIfNecessary(permission.getType());
                }
                catch (final GenericEntityException e)
                {
                    throw new RemoveException(e);
                }
            }
        }
        cache.refresh();
        return true;
    }

    /**
     * Check if a global anonymous permission exists
     *
     * @param permissionId must be global permission
     */
    public boolean hasPermission(final int permissionId)
    {
        //Ensure the permission Type is global
        if (!Permissions.isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must a global permission, " + permissionId + " is not");
        }

        // Check Global
        return hasPermission(new JiraPermission(permissionId));
    }

    /**
     * Check if a global permission for one of the users groups exists
     *
     * @param permissionId must be a global permission
     * @param u            must not be null
     */
    public boolean hasPermission(final int permissionId, final User u)
    {
        //Ensure the permission Type is global
        if (!Permissions.isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must a global permission, " + permissionId + " is not");
        }

        //Ensure the user isn't null
        if (u == null)
        {
            throw new IllegalArgumentException("User passed to this function cannot be null");
        }

        //Check the global permission first
        if (hasPermission(permissionId))
        {
            return true;
        }

        // Loop through the users groups and see if there is a global permission for one of them
        final com.atlassian.crowd.search.query.membership.MembershipQuery<String> membershipQuery =
                QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(u.getName()).returningAtMost(EntityQuery.ALL_RESULTS);

        Iterable<String> groups = crowdService.search(membershipQuery);

        for (String groupName : groups)
        {
            if (hasPermission(new JiraPermission(permissionId, groupName, GroupDropdown.DESC)))
            {
                return true;
            }
        }

        //Doesn't have permission
        return false;
    }

    /**
     * Retrieve all the groups with this permission
     *
     * @param permissionId must be a global permission
     * @return a collection of Group objects, never null
     */
    public Collection<com.opensymphony.user.Group> getGroups(final int permissionId)
    {
        final Collection<com.opensymphony.user.Group> groups = new ArrayList<com.opensymphony.user.Group>();
        final Collection<String> groupNames = getGroupNames(permissionId);
        for (final String groupName : groupNames)
        {
            com.atlassian.crowd.embedded.api.Group group = crowdService.getGroup(groupName);
            if (group != null)
            {
                groups.add(new com.opensymphony.user.Group(group));
            }
        }
        return unmodifiableCollection(groups);
    }

    @Override
    public Collection<Group> getGroupsWithPermission(int permissionId)
    {
        final Collection<Group> groups = new ArrayList<Group>();
        final Collection<String> groupNames = getGroupNames(permissionId);
        for (final String groupName : groupNames)
        {
            com.atlassian.crowd.embedded.api.Group group = crowdService.getGroup(groupName);
            if (group != null)
            {
                groups.add(group);
            }
        }
        return unmodifiableCollection(groups);
    }

    public Collection<String> getGroupNames(final int permissionId)
    {
        if (!Permissions.isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must a global permission, " + permissionId + " is not");
        }

        final Set<String> groupNames = new HashSet<String>();
        final Collection<JiraPermission> permissions = cache.getPermissions(permissionId);
        for (final JiraPermission jiraPermission : permissions)
        {
            groupNames.add(jiraPermission.getGroup());
        }
        return unmodifiableCollection(groupNames);
    }

    /////////////// Cache Access methods ////////////////////////////////////////////////
    protected boolean hasPermission(final JiraPermission jiraPermission)
    {
        // HACK - Since the JIRA System Administer permission implies the JIRA Administrator permission we
        // need to check if a user has the Sys perm if we are asking about the Admin perm
        if (Permissions.ADMINISTER == jiraPermission.getType())
        {
            // Do a check where if they have the current "Admin" permission we will short circuit, otherwise do
            // a second check with the sam group and PermType against the "SYS" permission.
            return cache.hasPermission(jiraPermission) || cache.hasPermission(new JiraPermission(Permissions.SYSTEM_ADMIN, jiraPermission.getGroup(),
                jiraPermission.getPermType()));
        }
        else
        {
            return cache.hasPermission(jiraPermission);
        }
    }

    private void clearActiveUserCountIfNecessary(final int permissionId)
    {
        final Set<Integer> usePermissions = Permissions.getUsePermissions();
        if (usePermissions.contains(permissionId))
        {
            getUserUtil().clearActiveUserCount();
        }
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }

    public boolean hasPermission(final int permissionType, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionType, (User) u);
    }
}
