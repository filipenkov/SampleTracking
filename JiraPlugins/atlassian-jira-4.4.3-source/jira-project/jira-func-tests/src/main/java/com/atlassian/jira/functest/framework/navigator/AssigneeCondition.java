package com.atlassian.jira.functest.framework.navigator;

/**
 * Condition that can be used to interact with the "assignee" navigator UI.
 *
 * @since v4.0
 */
public class AssigneeCondition extends UserGroupPicker
{
    private final String OPTION_NO_ASSIGNEE = "unassigned";

    public AssigneeCondition()
    {
        super("assignee");
    }

    public AssigneeCondition setNoReporter()
    {
        setPickerOption(new PickerOption(OPTION_NO_ASSIGNEE));
        return this;
    }

    public boolean isNoReporter()
    {
        return isOptionSet(OPTION_NO_ASSIGNEE);
    }
}
