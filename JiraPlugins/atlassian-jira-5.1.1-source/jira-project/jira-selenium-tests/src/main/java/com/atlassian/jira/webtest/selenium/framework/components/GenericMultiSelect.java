package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Generic MultiSelect representation.
 *
 * @since v4.2
 */
public final class GenericMultiSelect extends MultiSelect<GenericMultiSelect>
{

    /**
     * Create new MultiSelect with custom field name.
     *
     * @param contextLocator context locator (in case there is more than one picker on the page). If empty,
     * the picker locator will be considered global within the page (discouraged)
     * @param fieldName name of the field associated with this MultiSelect
     * @param ctx Selenium context
     */
    public GenericMultiSelect(String contextLocator, String fieldName, SeleniumContext ctx)
    {
        this(MultiSelectLocatorData.forFieldNameInContext(fieldName, contextLocator), ctx);
    }

    /**
     * Create new MultiSelect for a picker associated with a JIRA custom field.
     *
     * @param contextLocator context locator (in case there is more than one picker on the page). If empty,
     * the picker locator will be considered global within the page (discouraged)
     * @param customFieldId custom field ID
     * @param ctx Selenium context
     */
    public GenericMultiSelect(String contextLocator, int customFieldId, SeleniumContext ctx)
    {
        this(MultiSelectLocatorData.forCustomField(customFieldId, contextLocator), ctx);
    }

    private GenericMultiSelect(MultiSelectLocatorData locators, SeleniumContext ctx)
    {
        super(GenericMultiSelect.class, locators, ctx);
    }

}
