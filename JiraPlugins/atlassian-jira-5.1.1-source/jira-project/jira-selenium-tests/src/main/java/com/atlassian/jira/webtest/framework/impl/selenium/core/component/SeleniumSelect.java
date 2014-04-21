package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Options;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import java.util.List;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.core.component.Select} component.
 *
 * @since v4.3
 */
public class SeleniumSelect extends AbstractLocatorBasedPageObject implements Select
{

    private final SeleniumLocator main;

    public SeleniumSelect(SeleniumLocator mainLocator, SeleniumContext context)
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
    public Option selected()
    {
        String selectedId = client.getSelectedId(main.fullLocator());
        String selectedValue = client.getSelectedValue(main.fullLocator());
        String selectedLabel = client.getSelectedLabel(main.fullLocator());
        return Options.full(selectedId, selectedValue, selectedLabel);
    }

    @Override
    public Select select(Option option)
    {
        client.select(main.fullLocator(), SeleniumOptions.create(option));
        return this;
    }

    @Override
    public Select selectDefault()
    {
        return select(Options.defaultValue());
    }

}
