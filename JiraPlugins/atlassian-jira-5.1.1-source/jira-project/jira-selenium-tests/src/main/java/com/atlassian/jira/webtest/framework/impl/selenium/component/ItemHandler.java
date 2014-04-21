package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Queries and parses a single item from the drop-down list.
 *
 * @since v4.3
 */
public final class ItemHandler<P extends PageObject> extends SeleniumContextAware
{
    private static final String SCRIPT_RESOURCE = "getsingleitem.js";

    private final AbstractSeleniumDropdown<P> dropDown;
    private final String itemLocator;
    private final JqueryExecutor executor;
    private final ItemParser itemParser;
    private final String results;
    private final AjsDropdown.Item<P> parsed;

    ItemHandler(AbstractSeleniumDropdown<P> drop, String locator, SeleniumContext ctx)
    {
        super(ctx);
        this.dropDown = notNull("dropDown", drop);
        this.executor = new JqueryExecutor(context);
        this.itemParser = new ItemParser(context);
        this.itemLocator = notNull("itemLocator", locator);
        this.results = retrieve(locator);
        this.parsed = parse();
    }

    public AjsDropdown.Item<P> item()
    {
        return parsed;
    }

    private String retrieve(String locator)
    {
        return loadByDefaultTimeout(locator);
    }

    private String loadByDefaultTimeout(String locator)
    {
        return executor.execute(String.format(loadScript(), locator)).byDefaultTimeout();
    }

    private String loadScript()
    {
        try
        {
            return IOUtils.toString(getClass().getResourceAsStream(SCRIPT_RESOURCE));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not load script resource <" + SCRIPT_RESOURCE + ">", e);
        }
    }

    private AjsDropdown.Item<P> parse()
    {
        if (StringUtils.isBlank(results))
        {
            return null;
        }
        return itemParser.parseItemAndSection(results, dropDown).second();
    }

}
