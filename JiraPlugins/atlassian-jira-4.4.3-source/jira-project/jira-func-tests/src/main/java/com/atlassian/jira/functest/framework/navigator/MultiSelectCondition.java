package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a navigator condition in a multi-select box.
 *
 * @since v3.13
 */
public abstract class MultiSelectCondition implements NavigatorCondition
{
    private Set<String> options = new HashSet<String>();
    private final String elementName;

    protected MultiSelectCondition(String elementName)
    {
        this.elementName = elementName;
    }

    protected MultiSelectCondition(String elementName, Collection<String> options)
    {
        this(elementName);
        addOptions(options);
    }

    protected MultiSelectCondition(MultiSelectCondition condition)
    {
        this(condition.elementName, condition.options);
    }

    public void setOptions(Collection<String> options)
    {
        this.options = options == null ? new HashSet<String>() : new HashSet<String>(options);
    }

    public boolean addOption(String option)
    {
        return option != null && options.add(option);
    }

    public boolean removeOption(String option)
    {
        return option != null && options.remove(option);
    }

    public void clearOptions()
    {
        options.clear();
    }

    public void addOptions(Collection<String> options)
    {
        if (options != null)
        {
            this.options.addAll(options);
        }
    }

    public void removeOptions(Collection<String> options)
    {
        if (options != null)
        {
            this.options.removeAll(options);
        }
    }

    public Set<String> getOptions()
    {
        return Collections.unmodifiableSet(options);
    }

    public String getElementName()
    {
        return elementName;
    }

    public void setForm(WebTester tester)
    {
        final HttpUnitDialog dialog = tester.getDialog();
        final WebForm form = dialog.getForm();
        if (!options.isEmpty())
        {
            //This is hack to get around the fact that the API does not have a way
            //to set multiple option values. This is fixed in a newer version of JWebUnit.

            final String[] values = new String[options.size()];

            int position = 0;
            for (String type : options)
            {
                values[position] = dialog.getValueForOption(elementName, type);
                position++;
            }

            form.setParameter(elementName, values);
        }
        else
        {
            form.setParameter(elementName, new String[]{});
        }
    }

    public void parseCondition(WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        setOptions(getSetOptions(form));
    }

    private Set<String> getSetOptions(WebForm form)
    {
        final String[] options = form.getOptions(elementName);
        final String[] optionValues = form.getOptionValues(elementName);
        final String[] values = form.getParameterValues(elementName);

        Set<String> actualOptions = new HashSet<String>();
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                for (int j = 0; j < optionValues.length; j++)
                {
                    if (optionValues[j].equals(value))
                    {
                        actualOptions.add(options[j]);
                    }
                }
            }
        }
        return actualOptions;
    }

    public void assertSettings(WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();

        Assert.assertEquals("Options not set correctly for element: " + elementName, options, getSetOptions(form));
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MultiSelectCondition that = (MultiSelectCondition) o;

        if (elementName != null ? !elementName.equals(that.elementName) : that.elementName != null)
        {
            return false;
        }
        if (options != null ? !options.equals(that.options) : that.options != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (options != null ? options.hashCode() : 0);
        result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "MultiSelectCondition{" +
                "elementName='" + elementName + '\'' +
                ", options=" + options +
                '}';
    }
}
