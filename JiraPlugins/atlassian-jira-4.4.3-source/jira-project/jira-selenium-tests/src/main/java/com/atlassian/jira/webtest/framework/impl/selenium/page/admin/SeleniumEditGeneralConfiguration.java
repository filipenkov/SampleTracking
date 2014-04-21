package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumSubmittableChildPage;
import com.atlassian.jira.webtest.framework.model.admin.GeneralConfigurationProperty;
import com.atlassian.jira.webtest.framework.page.admin.EditGeneralConfiguration;
import com.atlassian.jira.webtest.framework.page.admin.ViewGeneralConfiguration;

import static com.atlassian.webtest.ui.keys.Sequences.chars;


/**
 * Represents the 'Global configuration' administration page in edit mode.
 *
 * @since v4.2
 */
public class SeleniumEditGeneralConfiguration extends AbstractSeleniumSubmittableChildPage<ViewGeneralConfiguration>
        implements EditGeneralConfiguration
{
    private static final String SUBMIT_LOCATOR = "input#edit_property";
    private static final String CANCEL_LOCATOR = "cancelButton";

    private final SeleniumLocator submit;
    private final SeleniumLocator cancel;

    public SeleniumEditGeneralConfiguration(SeleniumContext ctx, ViewGeneralConfiguration parent)
    {
        super(parent, ctx);
        this.submit = css(SUBMIT_LOCATOR);
        this.cancel = id(CANCEL_LOCATOR);
    }

    /* ------------------------------------------------ LOCATORS ---------------------------------------------------- */

    @Override
    protected SeleniumLocator detector()
    {
        return submit;
    }

    @Override
    protected Locator backLocator()
    {
        return cancel;
    }

    @Override
    protected SeleniumLocator submitLocator()
    {
        return submit;
    }

    private Locator propertyLocator(GeneralConfigurationProperty property)
    {
        return locatorFor(GeneralPropertyMappings.editLocator(property));
    }

    /* -------------------------------------------------- ACTIONS --------------------------------------------------- */

    @Override
    public SeleniumEditGeneralConfiguration setPropertyValue(GeneralConfigurationProperty property, String newValue)
    {
        propertyLocator(property).element().type(chars(newValue));
        return this;
    }

    @Override
    public SeleniumEditGeneralConfiguration setMode(String mode)
    {
        client.select("mode_select", mode);
        return this;
    }

    @Override
    public SeleniumEditGeneralConfiguration setExternalUserManagement(boolean enable)
    {
        css("input[name='externalUM'][value='" + Boolean.toString(enable) + "']").element().click();
        return this;
    }
}
