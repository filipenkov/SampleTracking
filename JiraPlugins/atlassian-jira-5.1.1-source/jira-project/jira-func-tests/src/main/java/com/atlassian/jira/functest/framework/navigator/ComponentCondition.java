package com.atlassian.jira.functest.framework.navigator;

import net.sourceforge.jwebunit.WebTester;

/**
 * Navigator condition that can be used to specify components to search for.
 * 
 * @since v4.0
 */
public class ComponentCondition extends MultiSelectCondition
{
    public ComponentCondition()
    {
        super("component");
    }

    public ComponentCondition(final ComponentCondition componentCondition)
    {
        super(componentCondition);
    }

    public NavigatorCondition copyCondition()
    {
        return new ComponentCondition(this);
    }

    public NavigatorCondition copyConditionForParse()
    {
        return new ComponentCondition();
    }

    @Override
    public void setForm(final WebTester tester)
    {
        // submit the form so that the context is up-to-date
        // note: this will fail if no project condition has been processed yet
        tester.clickButton("issue-filter-submit");
        tester.setWorkingForm("issue-filter");
        super.setForm(tester);
    }
}
