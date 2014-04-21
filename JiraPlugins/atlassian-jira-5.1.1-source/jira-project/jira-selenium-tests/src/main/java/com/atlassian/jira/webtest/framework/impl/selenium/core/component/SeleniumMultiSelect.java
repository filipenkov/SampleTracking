package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.component.MultiSelect;
import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Options;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.core.component.MultiSelect} component.
 *
 * @since v4.3
 */
public class SeleniumMultiSelect extends AbstractLocatorBasedPageObject implements MultiSelect
{

    private final SeleniumLocator main;

    public SeleniumMultiSelect(SeleniumLocator mainLocator, SeleniumContext context)
    {
        super(context);
        this.main = mainLocator;
    }


    @Override
    public Locator locator()
    {
        return main;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return main;
    }

    @Override
    public List<Option> all()
    {
        throw new UnsupportedOperationException("Implement as needed");
    }

    @Override
    public List<Option> selected()
    {
        String[] selectedIds = client.getSelectedIds(main.fullLocator());
        String[] selectedValues = client.getSelectedValues(main.fullLocator());
        String[] selectedLabels = client.getSelectedLabels(main.fullLocator());
        return createOptions(selectedIds, selectedValues, selectedLabels);
    }

    @Override
    public MultiSelect select(Option... options)
    {
        for (Option option : options)
        {
            selectOption(option);
        }
        return this;
    }

    @Override
    public MultiSelect unselect(Option... options)
    {
        for (Option option : options)
        {
            unselectOption(option);
        }
        return this;
    }


    @Override
    public MultiSelect selectAll()
    {
        for (Option option : all())
        {
            selectOption(option);
        }
        return this;
    }

    @Override
    public MultiSelect unselectAll()
    {
        client.removeAllSelections(main.fullLocator());
        return this;
    }

    private List<Option> createOptions(String[] ids, String[] values, String[] labels)
    {
        List<Option> answer = new ArrayList<Option>();
        int min = Math.min(ids.length, Math.min(values.length, labels.length));
        for(int i=0; i<min; i++)
        {
            answer.add(Options.full(ids[i], values[i], labels[i]));
        }
        return answer;
    }

    private void selectOption(Option option)
    {
        client.addSelection(main.fullLocator(), SeleniumOptions.create(option));
    }

    private void unselectOption(Option option)
    {
        client.removeSelection(main.fullLocator(), SeleniumOptions.create(option));
    }

}
