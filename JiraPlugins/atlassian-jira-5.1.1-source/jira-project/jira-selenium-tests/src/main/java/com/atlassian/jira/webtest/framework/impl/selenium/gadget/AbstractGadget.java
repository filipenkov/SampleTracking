package com.atlassian.jira.webtest.framework.impl.selenium.gadget;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.gadget.Gadget;
import com.atlassian.jira.webtest.framework.gadget.GadgetTimedCondition;
import com.atlassian.jira.webtest.framework.gadget.GadgetTimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * Abstract implementation of {@link com.atlassian.jira.webtest.framework.gadget.Gadget}.
 *
 * @since v4.3
 */
public abstract class AbstractGadget extends AbstractSeleniumComponent<Dashboard> implements Gadget
{
    private static final String TITLE_ID_REGEXP = "gadget-(\\d+)-title";
    private static final Pattern TITLE_ID_PATTERN = Pattern.compile(TITLE_ID_REGEXP);

    private int id;
    private final String name;
    private final Dashboard dashboard;

    protected AbstractGadget(int id, String name, Dashboard dashboard, SeleniumContext context)
    {
        super(dashboard, context);
        this.dashboard = notNull("dashboard", dashboard);
        this.name = notNull("name", name);
        this.id = greaterThan("id", id, -1);
    }

    protected AbstractGadget(String name, Dashboard dashboard, SeleniumContext context)
    {
        this(0, name, dashboard, context);
    }

    @Override
    protected Locator detector()
    {
        return id(iframeId());
    }

    @Override
    public TimedCondition isReady()
    {
        return super.isReady();
    }

    private String iframeId()
    {
        return String.format("gadget-%d", id());
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public Locator locator()
    {
        return detector();
    }

    @Override
    public int id()
    {
        if (id == 0)
        {
            id = retrieveIdForSingleGadget();
        }
        return id;
    }

    @Override
    public Locator frameLocator()
    {
        return id(iframeId());
    }

    protected final Locator titleLocator()
    {
        return jQuery(".dashboard-item-title:contains(" + name() + ")");
    }

    protected final int retrieveIdForSingleGadget()
    {
        String titleLocId = titleLocator().element().attribute("id").now();
        Matcher matcher = TITLE_ID_PATTERN.matcher(titleLocId);
        if (matcher.matches())
        {
            return Integer.parseInt(matcher.group(1));
        }
        else
        {
            throw new IllegalStateException("Title element id <" + titleLocId + "> does not match expected pattern <"
                    + TITLE_ID_REGEXP + ">");
        }
    }

    protected final TimedCondition inGadget(TimedCondition condition)
    {
        return new GadgetTimedCondition(context, condition, this);
    }

    protected final <T> TimedQuery<T> inGadget(TimedQuery<T> query)
    {
        return new GadgetTimedQuery<T>(context, query, this);
    }

}
