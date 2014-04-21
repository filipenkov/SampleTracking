package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Options;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import java.util.List;

/**
 * An implementation of HTML select that triggers page reload on each selection change.
 *
 * @since v4.3
 */
public class ReloadingSelect extends AbstractSeleniumPageObject implements Select
{
    private final Select wrapped;

    public ReloadingSelect(SeleniumLocator mainLocator, SeleniumContext context)
    {
        super(context);
        this.wrapped = new SeleniumSelect(mainLocator, context);
    }

    @Override
    public Locator locator()
    {
        return wrapped.locator();
    }

    @Override
    public List<Option> all()
    {
        return wrapped.all();
    }

    @Override
    public Option selected()
    {
        return wrapped.selected();
    }

    @Override
    public Select select(Option option)
    {
        if (alreadySelected(option))
        {
            return this;
        }
        wrapped.select(option);
        waitFor().pageLoad();
        return this;
    }

    private boolean alreadySelected(Option option)
    {
        return wrapped.selected() != null && wrapped.selected().equals(option);
    }

    @Override
    public Select selectDefault()
    {
        return select(Options.defaultValue());
    }


    @Override
    public TimedCondition isReady()
    {
        return wrapped.isReady();
    }

}
