package com.atlassian.jira.functest.framework.navigator;

/**
 * Condition that can be used to interact with the "reporter" navigator UI.
 *
 * @since v4.0
 */
public class ReporterCondition extends UserGroupPicker
{
    private final String OPTION_NO_REPORTER = "issue_no_reporter";

    public ReporterCondition()
    {
        super("reporter");
    }

    public ReporterCondition setNoReporter()
    {
        setPickerOption(new PickerOption(OPTION_NO_REPORTER));
        return this;
    }

    public boolean isNoReporter()
    {
        return isOptionSet(OPTION_NO_REPORTER);
    }
}
