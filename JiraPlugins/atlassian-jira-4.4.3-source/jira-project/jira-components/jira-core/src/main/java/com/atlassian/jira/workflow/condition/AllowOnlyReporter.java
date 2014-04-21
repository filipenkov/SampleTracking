/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.exception.DataAccessException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * A Condition which passes when the user is the issue's reporter.
 */
public class AllowOnlyReporter extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(AllowOnlyReporter.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        String caller = getCallerName(transientVars, args);
        User reporter = null;
        try
        {
            reporter = getIssue(transientVars).getReporter();
        }
        catch (DataAccessException e)
        {
            log.warn("Could not retrieve reporter with id '" + getIssue(transientVars).getAssigneeId() + "' of issue '" + getIssue(transientVars).getKey() + "'");
        }
        if (caller != null && reporter != null && caller.equals(reporter.getName()))
            return true;
        else
            return false;
    }
}
