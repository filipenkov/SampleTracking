/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.OnDemand;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ViewProjects extends JiraWebActionSupport
{
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    /*
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     * CHANGING THIS CONSTRUCTOR WILL BREAK ON DEMAND.
     *
     * Please consider if you really have to do that (unless you're trying to improve Studio integration).
     *
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     *
     */
    @OnDemand ("ON DEMAND extends this action and thus changing this constructor will cause compilation errors")
    public ViewProjects(final UserUtil userUtil, final UserManager userManager,
            VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    protected String doExecute() throws Exception
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT);
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB);

        return super.doExecute();
    }

    public boolean isAdmin() throws GenericEntityException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser());
    }

    public boolean isProjectAdmin(GenericValue project) throws GenericEntityException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, project, getLoggedInUser());
    }

    public boolean hasAdminPermission(GenericValue project) throws GenericEntityException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, project, getLoggedInUser());
    }

    public List getProjects()
    {
        List returns = new ArrayList();
        Collection projects = ComponentAccessor.getProjectManager().getProjects();
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            if (ComponentAccessor.getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, project, getLoggedInUser())
                    || ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
            {
                returns.add(project);
            }
        }
        return returns;
    }

    public boolean isDefaultAssigneeAssignable(GenericValue project) throws GenericEntityException
    {
        Long assigneeType = project.getLong("assigneetype");
        if (assigneeType == null)
        {
            return true;
        }
        
        if (ProjectAssigneeTypes.PROJECT_LEAD == assigneeType.longValue())
        {
            return ComponentUtils.isProjectLeadAssignable(project);
        }
        else
        {
            return true;
        }
    }

    public String abbreviateString(String str, int length)
    {
        return StringUtils.abbreviate(str, length);
    }


    public User getUser(GenericValue project)
    {
        return userUtil.getUser(project.getString("lead"));
    }

    public boolean getStringSet(GenericValue gv, String value)
    {
        return TextUtils.stringSet(gv.getString(value));
    }

    public String getPrettyAssigneeType(Long assigneeType)
    {
        return ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType);
    }

    public boolean isAllowSignUp()
    {
        return userManager.hasPasswordWritableDirectory() && JiraUtils.isPublicMode();
    }
}
