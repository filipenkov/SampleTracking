package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;

/**
 * Utility to parse items & sections from JSON objects.
 *
 * @since v4.3
 */
final class ItemParser extends SeleniumContextAware
{
    private static final String HEADER_KEY = "header";
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";

    private static final String ITEM_KEY = "item";
    private static final String SECTION_KEY = "section";

    ItemParser(SeleniumContext ctx)
    {
        super(ctx);
    }

    <P extends  PageObject> SeleniumDDSection<P> parseSection(JSONObject object, AbstractSeleniumDropdown<P> dropDown)
    {
        try
        {
            if (!object.has(ID_KEY))
            {
                throw new IllegalStateException("No 'id' found in the JS section object <" + object.toString() + ">");
            }
            return new SeleniumDDSection<P>(dropDown, context, object.getString(ID_KEY), getHeader(object));
        }
        catch (JSONException e)
        {
            throw new IllegalStateException("Error parsing JS section object <" + object.toString() + ">", e);
        }
    }

    <P extends PageObject> SeleniumDDItem<P> parseItem(JSONObject object, SeleniumDDSection<P> section)
    {
        try
        {
            if (!object.has(NAME_KEY))
            {
                throw new IllegalStateException("No 'name' found in the JS position object <" + object.toString() + ">");
            }
            return new SeleniumDDItem<P>(context, section, object.getString(NAME_KEY));
        }
        catch (JSONException e)
        {
            throw new IllegalStateException("Error parsing JS item object <" + object.toString() + ">", e);
        }
    }

    <P extends PageObject> Pair<SeleniumDDSection<P>, SeleniumDDItem<P>> parseItemAndSection(String itemAndSection,
            AbstractSeleniumDropdown<P> dropDown)
    {
        try
        {
            JSONObject itemObj = new JSONObject(itemAndSection);
            // TODO handle case where there's no section?
            SeleniumDDSection<P> section = parseSection(itemObj.getJSONObject(SECTION_KEY), dropDown);
            SeleniumDDItem<P> item = parseItem(itemObj.getJSONObject(ITEM_KEY), section);
            return Pair.of(section, item);
        }
        catch (JSONException e)
        {
            throw new IllegalStateException("Error parsing JS section and item object <" + itemAndSection + ">", e);
        }
    }

    private String getHeader(JSONObject obj) throws JSONException
    {
        return obj.has(HEADER_KEY) ? obj.getString(HEADER_KEY) : null;
    }

}
