package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.model.admin.Screen;
import com.atlassian.jira.webtest.framework.page.ChildPage;

/**
 * Represents configure screen administration page.
 *
 *
 * @see com.atlassian.jira.webtest.framework.page.admin.AdminPage
 * @since v4.3
 */
public interface ConfigureScreen extends ChildPage<ViewScreens>
{
    /**
     * Screen to be configured by this page (as passed to
     * {@link ViewScreens#goToConfigureScreen(com.atlassian.jira.webtest.framework.model.admin.Screen)}).
     *
     * @return scrren of this configuration page
     */
    Screen screen();

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    /**
     * Locator of the field table on the page
     *
     * @return field table locator
     */
    public Locator fieldTableLocator();

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    /**
     * Gets the representation of add field section on the page. 
     *
     * @return add field section component instance
     */
    public AddFieldSection addFieldSection();

     /* ---------------------------------------------- ACTIONS ------------------------------------------------------ */

    // TODO
}
