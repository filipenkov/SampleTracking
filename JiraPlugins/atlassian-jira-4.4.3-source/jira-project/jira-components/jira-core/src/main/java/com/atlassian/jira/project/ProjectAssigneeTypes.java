/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

public class ProjectAssigneeTypes extends AssigneeTypes
{
    private static final String NO_DEFAULT_MESSAGE = "Please select a Default Assignee";

    public static boolean isValidType(Long defaultAssigneeType)
    {
        return (defaultAssigneeType != null) && ((defaultAssigneeType.longValue() == PROJECT_LEAD) || (defaultAssigneeType.longValue() == UNASSIGNED && isAllowUnassigned()));
    }

    public static boolean isProjectLead(Long defaultAssigneeType)
    {
        if (defaultAssigneeType == null)
            return !isAllowUnassigned();
        else
            return defaultAssigneeType.longValue() == PROJECT_LEAD;
    }

    public static boolean isUnassigned(Long defaultAssigneeType)
    {
        if (defaultAssigneeType == null)
            return isAllowUnassigned();
        else
            return defaultAssigneeType.longValue() == UNASSIGNED;
    }

    public static Map getAssigneeTypes()
    {
        if (isAllowUnassigned())
            return EasyMap.build(String.valueOf(UNASSIGNED), PRETTY_UNASSIGNED, String.valueOf(PROJECT_LEAD), PRETTY_PROJECT_LEAD);
        else
            return EasyMap.build(String.valueOf(PROJECT_LEAD), PRETTY_PROJECT_LEAD);
    }

    public static String getPrettyAssigneeType(Long defaultAssigneeType)
    {
        if (isProjectLead(defaultAssigneeType))
            return PRETTY_PROJECT_LEAD;
        else if (isUnassigned(defaultAssigneeType))
            return PRETTY_UNASSIGNED;
        else
            return NO_DEFAULT_MESSAGE;
    }
}
