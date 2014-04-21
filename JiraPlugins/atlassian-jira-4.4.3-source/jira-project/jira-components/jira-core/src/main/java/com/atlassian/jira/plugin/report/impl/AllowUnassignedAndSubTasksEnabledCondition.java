package com.atlassian.jira.plugin.report.impl;

/**
 * Composite of the AllowUnassigned and SubTasks EnabledConditions
 *
 * @since v3.11
 */
public class AllowUnassignedAndSubTasksEnabledCondition extends AndEnabledCondition
{
    public AllowUnassignedAndSubTasksEnabledCondition()
    {
        super(new AllowUnassignedIssuesEnabledCondition(), new SubTasksEnabledCondition());
    }
}
