/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;

import java.util.Map;

public class PermissionCondition extends AbstractJiraCondition
{
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        String permName = (String) args.get("permission");

        Issue issue = getIssue(transientVars);

        PermissionManager permMan = ManagerFactory.getPermissionManager();

        User caller = getCaller(transientVars, args);
        return permMan.hasPermission(Permissions.getType(permName), issue.getGenericValue(), caller);
    }
}
