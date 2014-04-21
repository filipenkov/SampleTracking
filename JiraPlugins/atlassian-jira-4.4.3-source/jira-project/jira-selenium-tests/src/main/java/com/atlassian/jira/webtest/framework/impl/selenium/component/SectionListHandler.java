package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Queries and parses the section list from the JavaScript query.
 *
 * @since v4.3
 */
public final class SectionListHandler<P extends PageObject> extends SeleniumContextAware
{
    private static final String SCRIPT_RESOURCE = "getsections.js";

    private final AbstractSeleniumDropdown<P> parent;
    private final JqueryExecutor executor;
    private final ItemParser itemParser; 

    private final String results;
    private final List<AjsDropdown.Section<P>> parsed;

    SectionListHandler(AbstractSeleniumDropdown<P> parent, SeleniumContext ctx)
    {
        super(ctx);
        this.executor = new JqueryExecutor(context);
        this.itemParser = new ItemParser(context);
        this.parent = notNull("parent", parent);
        this.results = retrieve(parent.detector());
        this.parsed = parse();
    }

    public List<AjsDropdown.Section<P>> sections()
    {
        return parsed;
    }

    private String retrieve(SeleniumLocator suggestionsLocator)
    {
        return executor.execute(loadScript(suggestionsLocator)).byDefaultTimeout();
    }

    private String loadScript(SeleniumLocator suggestionsLocator)
    {
        try
        {
            return String.format(IOUtils.toString(getClass().getResourceAsStream(SCRIPT_RESOURCE)), suggestionsLocator.bareLocator());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not load script resource <" + SCRIPT_RESOURCE + ">", e);
        }
    }

    private List<AjsDropdown.Section<P>> parse()
    {
        final List<AjsDropdown.Section<P>> result = new ArrayList<AjsDropdown.Section<P>>();
        if (StringUtils.isBlank(results))
        {
            return result;
        }
        return parseJson(result);
    }

    private List<AjsDropdown.Section<P>> parseJson(List<AjsDropdown.Section<P>> result)
    {
        try
        {
            JSONArray jsonArray = new JSONArray(results);
            for (int i=0; i<jsonArray.length(); i++)
            {
                result.add(parseSingleObject(jsonArray.getJSONObject(i)));
            }
            return result;
        }
        catch (JSONException e)
        {
            throw new IllegalStateException("Error parsing JS section array <" + results + ">", e);
        }
    }

    private AjsDropdown.Section<P> parseSingleObject(JSONObject object)
    {
        return itemParser.parseSection(object, parent);
    }

}
