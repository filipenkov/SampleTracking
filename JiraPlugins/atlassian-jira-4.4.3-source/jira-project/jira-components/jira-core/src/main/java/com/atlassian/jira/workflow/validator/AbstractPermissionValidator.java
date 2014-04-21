/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.User;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public abstract class AbstractPermissionValidator implements Validator
{
    protected void hasUserPermission(Map args, Map transientVars, User user) throws InvalidInputException
    {
        String permName = (String) args.get("permission");
        Issue issue = (Issue) transientVars.get("issue");
        PermissionManager permMan = ManagerFactory.getPermissionManager();

        GenericValue genericValueToCheckPermissionsOn;

        //Check to see if we have an existing issue in the transient vars, if we do use that, otherwise use the project.
        if (issue.getGenericValue() != null)
            genericValueToCheckPermissionsOn = issue.getGenericValue();
        else if (issue.getProject() != null)
            genericValueToCheckPermissionsOn = issue.getProject();
        else
            throw new InvalidInputException("Invalid project specified.");

        if (permMan.hasPermission(Permissions.getType(permName), genericValueToCheckPermissionsOn, user))
        {
            return;
        }

        throw new InvalidInputException("User '" + user + "' doesn't have the '"+permName+"' permission");
    }
}
