package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.model.admin.Screen;

/**
 * Represents the Screens administration page. The page contains a table with all screens
 * available in JIRA, each of the rows containing links for configuring, editing and
 * copying the screen, 
 *
 * @since v4.2
 */
public interface ViewScreens extends AdminPage
{
    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    /**
     * Locator of the main table on this page containing list of all screens.
     *
     * @return screens table locator
     */
    Locator screenTableLocator();

    /**
     * Locator of the 'Configure screen' link for a given screen.
     *
     * @param screen target screen
     * @return locator of the screen's 'Configure' link on this page
     */
    Locator configureScreenLinkLocatorFor(Screen screen);
    

    /* ------------------------------------------------ TRANSITIONS ------------------------------------------------- */

    /**
     * Go to the 'Configure Screen' page for given <tt>screen</tt>.
     * 
     * @param screen screen to go to
     * @return {@link ConfigureScreen} instance representing the screen configuration page
     */
    ConfigureScreen goToConfigureScreen(Screen screen);

}
