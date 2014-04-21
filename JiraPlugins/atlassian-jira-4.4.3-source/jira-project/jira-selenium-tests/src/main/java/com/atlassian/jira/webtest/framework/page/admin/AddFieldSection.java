package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.core.component.MultiSelect;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.PageSection;

/**
 * Represents the section of the 'Configure screens' page that is used to add a new field. 
 *
 * @see com.atlassian.jira.webtest.framework.page.admin.ConfigureScreen
 * @since v4.3
 */
public interface AddFieldSection extends PageSection<ConfigureScreen>
{
    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    /**
     * Locator of the multi-select of fields.
     *
     * @return field multi-select locator
     */
    SeleniumLocator addFieldSelectLocator();

    /**
     * Locator of the 'Add Fields' submit button within the section.
     *
     * @return 'Add Fields' submit locator
     */
    SeleniumLocator addFieldSubmitLocator();

    /* --------------------------------------------- COMPONENTS ----------------------------------------------------- */

    /**
     * Multi-select object representing the fields multi-select.
     *
     * @return fields multi-select
     */
    MultiSelect selectFields();

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    /**
     * Submit selected fields and add them to the screen
     *
     * @return this AddFieldSection instance
     */
    AddFieldSection submitAdd();
}
