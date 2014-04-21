/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@WebSudoRequired
public class GlobalPermissions extends ProjectActionSupport
{

    private static final class Actions
    {
        private static final String VIEW = "view";
        private static final String ADD = "add";
        private static final String DEL = "del";
        private static final String DELETE = "delete";
        private static final String CONFIRM = "confirm";
    }

    private Map permTypes;
    private String groupName;
    private int permType = -1;
    private String action = Actions.VIEW;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;

    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    public GlobalPermissions(GlobalPermissionManager globalPermissionManager, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil, UserUtil userUtil, GroupManager groupManager)
    {
        super();
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        return SUCCESS;
    }

    public void doValidation()
    {
        if (permType >= 0)
        {
            if (!Permissions.isGlobalPermission(permType))
            {
                addError("permType", getText("admin.errors.permissions.inexistent.permission"));
            }

            if (groupName != null)
            {
                //check if the group exists unless we're trying to remove a group from a global permission!
                if (!Actions.DEL.equals(action) && !Actions.CONFIRM.equals(action))
                {
                    Group group = groupManager.getGroup(groupName);
                    if (group == null)
                    {
                        addError("groupName", getText("admin.errors.permissions.inexistent.group", "'" + groupName + "'"));
                    }
                    // JRA-22984 Prevent admin groups from being added to JIRA USE permisson
                    if (permType == Permissions.USE && getAdministrativeGroups().contains(group))
                    {
                        addError("groupName", getText("admin.errors.permissions.group.notallowed.for.permission", groupName, getPermTypeDisplayName(permType)));
                    }
                }
                else
                {
                    //check that the group we're trying to remove is part of one of the permissions
                    final Collection groupNames = globalPermissionManager.getGroupNames(permType);
                    if(!groupNames.contains(groupName))
                    {
                        addErrorMessage(getText("admin.errors.permissions.delete.group.not.in.permission", groupName, getPermTypeDisplayName(permType)));
                    }
                }
            }
            else
            {
                // we dont allow them to add the Anyone group to JIRA USE permission as it make no sense and harms things in the long run
                //JRA-26627 don't allow them to be added to sysadmin and admin groups either
                if ((permType == Permissions.USE || Permissions.isAdministrativePermission(permType)) && Actions.ADD.equals(action))
                {
                    addError("groupName", getText("admin.errors.permissions.group.notallowed.for.permission", getText("admin.common.words.anyone"), getPermTypeDisplayName(permType)));
                }
            }

            // If the user is trying to add a hidden perm they should not be allowed to do so
            validateAdd();

            // If the user is deleting then we need to make sure they do not remove themselves from admin groups
            validateDelete();
        }
        else
        {
            if (Actions.ADD.equals(action))
            {
                addError("permType", getText("admin.errors.permissions.must.select.permission"));
            }
        }

        super.doValidation();
    }

    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        if (permType >= 0)
        {
            if (Actions.DEL.equals(action))
            {
                removePermission(permType, groupName);
                action = Actions.VIEW;
                return getPermissionRedirect();
            }
            else if (Actions.CONFIRM.equals(action))
            {
                return "confirm";
            }
            else if (Actions.ADD.equals(action))
            {
                final Group group = (groupName == null ? null : groupManager.getGroup(groupName));
                createPermission(permType, group);
                return getPermissionRedirect();
            }
        }

        return getResult();
    }

    protected void validateDelete()
    {
        if (Actions.DEL.equals(action) || Actions.CONFIRM.equals(action))
        {
            // if we're deleting an admin permission, check that another group exists
            if (permType == Permissions.ADMINISTER)
            {
                boolean removingAllPerms = globalPermissionGroupAssociationUtil.isRemovingAllMyAdminGroups(EasyList.build(groupName), getLoggedInUser())
                                           && !globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getLoggedInUser());

                if (removingAllPerms)
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission"));
                }
            }
            else if (permType == Permissions.SYSTEM_ADMIN)
            {
                if (!globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getLoggedInUser()))
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission.sys.admin.only"));
                }
                else if (globalPermissionGroupAssociationUtil.isRemovingAllMySysAdminGroups(EasyList.build(groupName), getLoggedInUser()))
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission.sys.admin"));
                }
            }
        }
    }

    protected void validateAdd()
    {
        if (Actions.ADD.equals(action))
        {
            if (!getPermTypes().containsKey(new Integer(permType)))
            {
                addErrorMessage(getText("admin.errors.permissions.not.have.permission.to.add"));
            }
        }
    }

    protected String getPermissionRedirect() throws Exception
    {
        return getRedirect("GlobalPermissions!default.jspa");
    }

    protected void createPermission(int permType, Group group) throws CreateException
    {
        String groupName = (group == null ? null : group.getName());
        // We need to get all the groupNames for the permission type and see if the list contains this groupName
        if (!globalPermissionManager.getGroupNames(permType).contains(groupName))
        {
            globalPermissionManager.addPermission(permType, groupName);
        }

    }

    private void removePermission(int permType, String groupName) throws RemoveException
    {
        final Group group = (groupName == null ? null : groupManager.getGroup(groupName));

        String groupToDelete = null;
        if(group != null)
        {
            groupToDelete = group.getName();
        }
        else if(groupName != null)
        {
            //JRA-15911: Check if by chance the group returned was null but we did have a groupName
            groupToDelete = groupName;
        }
        globalPermissionManager.removePermission(permType, groupToDelete);
    }

    public Collection getPermissionGroups(Integer permType)
    {
        return globalPermissionManager.getPermissions(permType.intValue());
    }

    public Collection getGroups()
    {
        return groupManager.getAllGroups();
    }

    public int getPermType()
    {
        return permType;
    }

    public String getPermTypeName()
    {
        return getPermTypeDisplayName(permType);
    }

    public void setPermType(int permType)
    {
        this.permType = permType;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        if (TextUtils.stringSet(groupName))
        {
            this.groupName = groupName;
        }
        else
        {
            this.groupName = null;
        }
    }

    public void setAction(String action)
    {
        if (Actions.DEL.equalsIgnoreCase(action) || Actions.DELETE.equalsIgnoreCase(action))
        {
            this.action = Actions.DEL;
        }
        else if (Actions.CONFIRM.equalsIgnoreCase(action))
        {
            this.action = Actions.CONFIRM;
        }
        else
        {
            this.action = Actions.ADD;
        }
    }

    public boolean isConfirm()
    {
        return Actions.CONFIRM.equalsIgnoreCase(action);
    }

    public String getPermTypeDisplayName(int permType)
    {
        String name;
        if (permType == Permissions.SYSTEM_ADMIN)
        {
            name = "admin.global.permissions.system.administer";
        }
        else if (permType == Permissions.ADMINISTER)
        {
            name = "admin.global.permissions.administer";
        }
        else if (permType == Permissions.USE)
        {
            name = "admin.global.permissions.use";
        }
        else if (permType == Permissions.USER_PICKER)
        {
            name = "admin.global.permissions.user.picker";
        }
        else if (permType == Permissions.CREATE_SHARED_OBJECTS)
        {
            name = "admin.global.permissions.create.shared.filter";
        }
        else if (permType == Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS)
        {
            name = "admin.global.permissions.manage.group.filter.subscriptions";
        }
        else if (permType == Permissions.BULK_CHANGE)
        {
            name = "admin.global.permissions.bulk.change";
        }
        else
        {
            name = "common.words.unknown";
        }
        return getText(name);
    }

    public Map getPermTypes()
    {
        if (permTypes == null)
        {
            permTypes = new ListOrderedMap();

            // Only show the SYS_ADMIN perm to those who have it
            if (globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getLoggedInUser()))
            {
                permTypes.put(new Integer(Permissions.SYSTEM_ADMIN), getPermTypeDisplayName(Permissions.SYSTEM_ADMIN));
            }
            permTypes.put(new Integer(Permissions.ADMINISTER), getPermTypeDisplayName(Permissions.ADMINISTER));
            permTypes.put(new Integer(Permissions.USE), getPermTypeDisplayName(Permissions.USE));
            permTypes.put(new Integer(Permissions.USER_PICKER), getPermTypeDisplayName(Permissions.USER_PICKER));
            permTypes.put(new Integer(Permissions.CREATE_SHARED_OBJECTS), getPermTypeDisplayName(Permissions.CREATE_SHARED_OBJECTS));
            permTypes.put(new Integer(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS), getPermTypeDisplayName(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS));
            permTypes.put(new Integer(Permissions.BULK_CHANGE), getPermTypeDisplayName(Permissions.BULK_CHANGE));
        }

        return permTypes;
    }

    public String getDescription(Integer permType)
    {
        return Permissions.getDescription(permType.intValue());
    }

    public boolean hasExceededUserLimit()
    {
        return userUtil.hasExceededUserLimit();
    }

    private Collection<Group> getAdministrativeGroups()
    {
        final Collection<Group> groups = new ArrayList<Group>(globalPermissionManager.getGroupsWithPermission(Permissions.ADMINISTER));
        groups.addAll(globalPermissionManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN));
        return Collections.unmodifiableCollection(groups);
    }
}
