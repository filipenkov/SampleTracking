package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.0
 */
public class GenericQueryCondition implements NavigatorCondition
{
    private final String elementName;
    private String query;

    public GenericQueryCondition(final String elementName)
    {
        this.elementName = elementName;
    }

    public void setForm(final WebTester tester)
    {
        final HttpUnitDialog dialog = tester.getDialog();
        final WebForm form = dialog.getForm();
        form.setParameter(elementName, query);
    }

    public GenericQueryCondition setQuery(final String q)
    {
        this.query = q;
        return this;
    }

    public void parseCondition(final WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        query = form.getParameterValue(elementName);
    }

    public void assertSettings(final WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        Assert.assertEquals("Value not set correctly for element: " + elementName, query, form.getParameterValue(elementName));
    }

    @Override
    public String toString()
    {
        return String.format("[%s: %s]", elementName, query);
    }

    public NavigatorCondition copyCondition()
    {
        throw new UnsupportedOperationException();
    }

    public NavigatorCondition copyConditionForParse()
    {
        throw new UnsupportedOperationException();
    }
}
