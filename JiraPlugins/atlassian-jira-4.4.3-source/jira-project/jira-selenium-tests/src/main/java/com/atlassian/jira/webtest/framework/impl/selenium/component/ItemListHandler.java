package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
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
public final class ItemListHandler<P extends PageObject> extends SeleniumContextAware
{
    private static final String SCRIPT_RESOURCE = "getpositions.js";

    private final SeleniumDDSection<P> section;
    private final JqueryExecutor executor;
    private final String results;
    private final List<AjsDropdown.Item<P>> parsed;
    private final ItemParser itemParser;

    ItemListHandler(SeleniumDDSection<P> parent, SeleniumContext ctx)
    {
        super(ctx);
        this.section = notNull("section", parent);
        this.executor = new JqueryExecutor(context);
        this.itemParser = new ItemParser(context);
        this.results = retrieve(section.detector().fullLocator());
        this.parsed = parse();
    }

    public List<AjsDropdown.Item<P>> items()
    {
        return parsed;
    }

    private String retrieve(String sectionLocator)
    {
        return loadByDefaultTimeout(sectionLocator);
    }

    private String loadByDefaultTimeout(String sectionLocator)
    {
        return executor.execute(String.format(loadScript(), sectionLocator)).byDefaultTimeout();
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

    private List<AjsDropdown.Item<P>> parse()
    {
        final List<AjsDropdown.Item<P>> result = new ArrayList<AjsDropdown.Item<P>>();
        if (StringUtils.isBlank(results))
        {
            return result;
        }
        return parseJson(result);
    }

    private List<AjsDropdown.Item<P>> parseJson(List<AjsDropdown.Item<P>> result)
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
            throw new IllegalStateException("Error parsing JS position array <" + results + ">", e);
        }
    }

    private AjsDropdown.Item<P> parseSingleObject(JSONObject object)
    {
        return itemParser.parseItem(object, section);
    }

}
