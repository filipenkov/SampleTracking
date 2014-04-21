/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowContext;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class CloseCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(DisallowIfInStepCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        try
        {
            Boolean close = (Boolean) transientVars.get("close");
            if (!close.booleanValue())
            {
                return false;
            }

            GenericValue issueGV = getIssue(transientVars).getGenericValue();
            WorkflowContext context = (WorkflowContext) transientVars.get("context");

            String username = context.getCaller();
            User user = null;

            if (username != null)
                user = UserUtils.getUser(username);

            return ManagerFactory.getPermissionManager().hasPermission(Permissions.CLOSE_ISSUE, issueGV, user);
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
            return false;
        }
    }
}
