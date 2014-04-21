package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Selenium representation of the AUI.LabelsPicker component, which is a specialized MultiSelect.
 *
 * @since v4.2
 */
public class LabelsPicker extends MultiSelect<LabelsPicker>
{
    public static final String LABELS_SYSTEM_FIELD_NAME = "labels";

    public static LabelsPicker newSystemLabelsPicker(String contextLocator, SeleniumContext ctx)
    {
        MultiSelectLocatorData loc = MultiSelectLocatorData.forFieldNameInContext(LABELS_SYSTEM_FIELD_NAME, contextLocator);
        return new LabelsPicker(loc, ctx);
    }

    public static LabelsPicker newCustomFieldLabelsPicker(String contextLocator, int fieldId, SeleniumContext ctx)
    {
        return new LabelsPicker(MultiSelectLocatorData.forCustomField(fieldId, contextLocator), ctx);
    }

    /**
     * Create new labels picker with custom picker locator.
     *
     * @param locators jquery locator collection of the represented MultiSelect control.
     * @param ctx Selenium client
     */
    private LabelsPicker(MultiSelectLocatorData locators, SeleniumContext ctx)
    {
        super(LabelsPicker.class, locators, ctx);
    }

}
