/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class ViewUser extends IssueActionSupport
{
    protected String name;
    protected User user;
    private boolean showPasswordUpdateMsg;
    private Map<String, String> userProperties;

    protected final CrowdService crowdService;
    protected final CrowdDirectoryService crowdDirectoryService;
    protected final UserPropertyManager userPropertyManager;
    protected final UserManager userManager;

    public ViewUser(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, UserPropertyManager userPropertyManager, UserManager userManager)
    {
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.userPropertyManager = userPropertyManager;
        this.userManager = userManager;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public User getUser()
    {
        if (user == null)
        {
            user = crowdService.getUser(name);
        }

        return user;
    }

    public Collection<String> getUserGroups()
    {
        final com.atlassian.crowd.search.query.membership.MembershipQuery<String> membershipQuery =
                QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(name).returningAtMost(EntityQuery.ALL_RESULTS);

        List<String> groupNames = new ArrayList<String>();
        Iterable<String> iterable = crowdService.search(membershipQuery);
        for (Iterator<String> it = iterable.iterator(); it.hasNext(); )
        {
            groupNames.add(it.next());
        }
        return groupNames;
    }

    public String getDirectoryName()
    {
        User user = getUser();
        if (user == null)
        {
            return "???";
        }
        return crowdDirectoryService.findDirectoryById(user.getDirectoryId()).getName();
    }

    protected String doExecute() throws Exception
    {
        retrieveUserMetaProperties();
        return super.doExecute();
    }

    protected void doValidation()
    {
        if (getUser() == null)
        {
            addErrorMessage(getText("admin.errors.users.user.does.not.exist"));
        }
    }
    /**
     * This method retrieves a user's meta properties
     */
    protected void retrieveUserMetaProperties()
    {
        userProperties = new HashMap<String, String>();

        User user = getUser();
        if (user != null)
        {
            PropertySet userPropertySet = userPropertyManager.getPropertySet(user);

            Collection keys = userPropertySet.getKeys(PropertySet.STRING);

            if (keys != null)
            {
                for (Iterator iterator = keys.iterator(); iterator.hasNext();)
                {
                    String key = (String) iterator.next();
                    if (key.startsWith(UserUtil.META_PROPERTY_PREFIX))
                    {
                        userProperties.put(key.substring(UserUtil.META_PROPERTY_PREFIX.length()), userPropertySet.getString(key));
                    }
                }
            }

        }
    }

    public boolean isShowPasswordUpdateMsg()
    {
        return showPasswordUpdateMsg;
    }

    public void setShowPasswordUpdateMsg(boolean showPasswordUpdateMsg)
    {
        this.showPasswordUpdateMsg = showPasswordUpdateMsg;
    }

    public Map<String, String> getUserProperties()
    {
        return userProperties;
    }

    public boolean isRemoteUserPermittedToEditSelectedUser()
    {
        return getUser() != null && (isSystemAdministrator() || !getGlobalPermissionManager().hasPermission(Permissions.SYSTEM_ADMIN, getUser()));
    }

    public boolean isSelectedUserEditable()
    {
        if (userManager.canUpdateUser(getUser()))
        {
            return isRemoteUserPermittedToEditSelectedUser();
        }
        return false;
    }

    public boolean isSelectedUsersGroupsEditable()
    {
        return userManager.canUpdateGroupMembershipForUser(getUser());
    }

    public boolean canUpdateUserPassword()
    {
        return isSelectedUserEditable() && userManager.canUpdateUserPassword(getUser());
    }
}
