package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.query.AbstractSeleniumConditionBasedQuery;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
* Selenium {@link com.atlassian.jira.webtest.framework.component.AjsDropdown.Section} implementation.
*
* @since v4.3
*/
public class SeleniumDDSection<P extends PageObject> extends AbstractSeleniumComponent<AjsDropdown<P>> implements AjsDropdown.Section<P>
{
    private final String id;
    private final String header;
    private final SeleniumLocator locator;


    protected SeleniumDDSection(AbstractSeleniumDropdown<P> parent, SeleniumContext context, String id, String header)
    {
        super(parent, context);
        this.id = notNull("id", id);
        this.header = header;
        this.locator = parent.detector().combine(id(id));
    }


    @Override
    public String id()
    {
        return id;
    }

    @Override
    public String header()
    {
        return header;
    }

    @Override
    public boolean hasHeader()
    {
        return header != null;
    }

    @Override
    public SeleniumLocator locator()
    {
        return locator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return locator;
    }

    @Override
    public TimedQuery<List<AjsDropdown.Item<P>>> items()
    {
        return new AbstractSeleniumConditionBasedQuery<List<AjsDropdown.Item<P>>>(locator().element().isPresent(),
                context, ExpirationHandler.RETURN_CURRENT)
        {
            @Override
            protected List<AjsDropdown.Item<P>> evaluateNow()
            {
                return new ItemListHandler<P>(SeleniumDDSection.this, context).items();
            }
            @Override
            protected List<AjsDropdown.Item<P>> substituteValue()
            {
                return Collections.emptyList();
            }
        };
    }
}
