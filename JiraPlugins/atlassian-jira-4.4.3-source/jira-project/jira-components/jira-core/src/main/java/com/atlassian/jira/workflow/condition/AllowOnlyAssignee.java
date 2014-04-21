/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.exception.DataAccessException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;

public class AllowOnlyAssignee extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(AllowOnlyAssignee.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        String caller = getCallerName(transientVars, args);
        User assignee = null;
        try
        {
            assignee = getIssue(transientVars).getAssignee();
        }
        catch (DataAccessException e)
        {
            log.warn("Could not retrieve assignee with id '" + getIssue(transientVars).getAssigneeId() + "' of issue '" + getIssue(transientVars).getKey() + "'");
        }
        if (caller != null && assignee != null && caller.equals(assignee.getName()))
            return true;
        else
            return false;
    }

}
