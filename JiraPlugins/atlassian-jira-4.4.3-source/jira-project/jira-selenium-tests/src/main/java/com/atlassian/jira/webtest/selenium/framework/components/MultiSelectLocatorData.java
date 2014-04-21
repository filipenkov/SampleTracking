package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.removeLocatorPrefix;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Encapsulates element locators for a single AUI MultiSelect component.
 *
 * @since v4.2
 */
public final class MultiSelectLocatorData
{

    public static MultiSelectLocatorData forFieldName(String fieldName)
    {
        return new MultiSelectLocatorData(null, fieldName, null);
    }

    public static MultiSelectLocatorData forFieldNameInContext(String fieldName, String contextLocator)
    {
        return new MultiSelectLocatorData(contextLocator, fieldName, null);
    }

    public static MultiSelectLocatorData forCustomField(int customFieldId, String contextLocator)
    {
        return new MultiSelectLocatorData(contextLocator, customFieldNameFor(customFieldId), null);
    }

    public static MultiSelectLocatorData withCustomLocator(String customFieldLocator, String fieldName, String contextLocator)
    {
        return new MultiSelectLocatorData(contextLocator, fieldName, customFieldLocator);
    }

    private static String customFieldNameFor(int id)
    {
        return "customfield_" + id;
    }

    private static final String MAIN_LOCATOR_TEMPLATE = "div#%s-multi-select";
    private static final String VISIBLE_SUGGESTIONS_LOCATOR_TEMPLATE = Locators.JQUERY.addPrefix("body > div.ajs-layer.active div#%s-suggestions");

    private final String context;
    private final String fieldName;
    private final String multiSelectLocator;
    private final String textAreaLocator;
    private final String visibleSuggestionsLocator;
    private final String selectModelLocator;
    private final String dropDownIconLocator;

    private MultiSelectLocatorData(final String context, final String fieldName, final String mainLocator)
    {
        this.context = context;
        this.fieldName = Assertions.notNull("fieldName", fieldName);
        this.multiSelectLocator = inContext(resolveLocator(mainLocator));
        this.textAreaLocator = initTextAreaLocator();
        this.visibleSuggestionsLocator = initVisibleSuggestionsLocator();
        this.selectModelLocator = initSelectModelLocator();
        this.dropDownIconLocator = initDropDownIconLocator();
    }

    private String resolveLocator(String locator)
    {
        return locator != null ? locator : defaultMainLocator();
    }

    private String defaultMainLocator()
    {
        return String.format(MAIN_LOCATOR_TEMPLATE, fieldName);
    }

    private String initTextAreaLocator()
    {
        return inMultiSelect("textarea#" + fieldName + "-textarea");
    }

    private String initVisibleSuggestionsLocator()
    {
        return String.format(VISIBLE_SUGGESTIONS_LOCATOR_TEMPLATE, fieldName);
    }

    private String initSelectModelLocator()
    {
        return mainLocator() + " ~ select#" + fieldName;
    }

    private String initDropDownIconLocator()
    {
        return inMultiSelect("span.icon.drop-menu");
    }


    
    public String mainLocator()
    {
        return multiSelectLocator;
    }

    public String contextLocator()
    {
        return context;
    }

    public String textAreaLocator()
    {
        return textAreaLocator;
    }

    public String visibleSuggestionsLocator()
    {
        return visibleSuggestionsLocator;
    }

    public String selectModelLocator()
    {
        return selectModelLocator;
    }

    public String dropDownIconLocator()
    {
        return dropDownIconLocator;
    }

    public boolean hasContext()
    {
        return isNotEmpty(context);
    }

    public String inMultiSelect(String elementLocator)
    {
        return mainLocator() + " " + elementLocator;
    }

    public String inContext(String elemLocator)
    {
        return hasContext() ? context + " " + removeLocatorPrefix(elemLocator) : Locators.JQUERY.addPrefixIfNecessary(elemLocator);
    }

    public String inSuggestions(String jQueryLocator)
    {
        return visibleSuggestionsLocator() + " " + removeLocatorPrefix(jQueryLocator);
    }

}
