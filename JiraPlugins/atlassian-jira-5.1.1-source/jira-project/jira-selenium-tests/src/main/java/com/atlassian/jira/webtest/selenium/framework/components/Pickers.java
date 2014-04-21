package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.SystemField;

/**
 * Factory for convenient creation of various JIRA pickers.
 *
 * @since v4.2
 */
public final class Pickers
{

    private Pickers() {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * Factory method for 'Affected Versions' system field picker.
     *
     * @param contextLocator context of the picker
     * @param ctx Selenium context
     * @return new 'Affected Versions' picker
     */
    public static GenericMultiSelect newAffectedVersionPicker(String contextLocator, SeleniumContext ctx)
    {
        return new GenericMultiSelect(contextLocator, SystemField.AFFECTED_VERSIONS.id(), ctx);
    }

    /**
     * Factory method for 'Fix Version' system field picker.
     *
     * @param contextLocator context of the picker
     * @param ctx Selenium context
     * @return new 'Fix Version' picker
     */
    public static GenericMultiSelect newFixVersionPicker(String contextLocator, SeleniumContext ctx)
    {
        return new GenericMultiSelect(contextLocator, SystemField.FIX_VERSIONS.id(), ctx);
    }

    /**
     * Factory method for a version picker associated with a custom field.
     *
     * @param contextLocator context of the picker
     * @param customFieldId ID of the custom field associated with the picker
     * @param ctx Selenium context
     * @return new custom field version picker
     */
    public static GenericMultiSelect newCustomFieldVersionPicker(String contextLocator, int customFieldId, SeleniumContext ctx)
    {
        return new GenericMultiSelect(contextLocator, customFieldId, ctx);
    }

    /**
     * Factory method for a component system field picker.
     *
     * @param contextLocator context of the picker
     * @param ctx Selenium context
     * @return new custom field version picker
     */
    public static GenericMultiSelect newComponentPicker(String contextLocator, SeleniumContext ctx)
    {
        return new GenericMultiSelect(contextLocator, SystemField.COMPONENTS.id(), ctx);
    }
}
