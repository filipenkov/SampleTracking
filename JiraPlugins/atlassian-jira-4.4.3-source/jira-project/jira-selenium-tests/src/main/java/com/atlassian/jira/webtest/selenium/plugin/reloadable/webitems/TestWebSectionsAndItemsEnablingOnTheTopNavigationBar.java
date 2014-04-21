package com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.AbstractReloadablePluginsSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.id;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.ID;

/**
 * Responsible for verifying that a custom menu item or section on the top navigation bar can be enabled.
 * <br/>
 * The test cases assume the reference plugin is installed and disabled. This is what we call the ZERO TO ON scenario.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebSectionsAndItemsEnablingOnTheTopNavigationBar extends AbstractReloadablePluginsSeleniumTest
{
    private static final String REFERENCE_MENU_TOP_NAVIGATION_BAR_LINK_ID = "reference-menu-top-level-section";
    private static final String REFERENCE_MENU_DROP_DOWN_LINK_ID = "reference-menu-top-level-section_drop";
    private static final String REFERENCE_CAC_MENU_ITEM_JQUERY_LOCATOR = "#reference-menu-top-level-section_drop_drop .aui-list-section #reference-cac-link";
    private static final String REFERENCE_FORUMS_MENU_ITEM_JQUERY_LOCATOR = "#reference-menu-top-level-section_drop_drop .aui-list-section #reference-forums-link";

    public void testReferenceMenuOnTheTopNavigationBarIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled()
    {
        assertThatReferenceMenuOnTheTopNavigationBarIsNeitherPresentNorVisible();
        assertThatReferenceMenuDropDownArrowOnTheTopNavigationBarIsNeitherPresentNorVisible();
    }

    public void testReferenceMenuOnTheTopNavigationBarIsPresentAndVisibleWhenTheReferencePluginIsEnabled()
    {
        enableReferencePlugin();

        reallyRefreshThePage();

        assertThatReferenceMenuOnTheTopNavigationBarIsPresentAndVisible();
        assertThatReferenceMenuDropDownArrowOnTheTopNavigationBarIsPresentAndVisible();

        displayReferenceMenu();

        assertThatTheCACMenuItemOnTopNavigationBarIsPresentAndVisible();
        assertThatTheForumsMenuItemOnTopNavigationBarIsPresentAndVisible();
    }

    private void assertThatReferenceMenuDropDownArrowOnTheTopNavigationBarIsNeitherPresentNorVisible()
    {
        assertThat("The Reference Menu Drop Down Arrow on the Top Navigation Bar was found when the reference plugin was disabled",
                and(
                        referenceMenuDropDownArrowOnTheTopNavigationBarLocator().element().isPresent(),
                        referenceMenuDropDownArrowOnTheTopNavigationBarLocator().element().isVisible()
                ), isFalse().now()
        );
    }

    private void assertThatReferenceMenuOnTheTopNavigationBarIsNeitherPresentNorVisible()
    {
        assertThat("The Reference Menu on the Top Navigation Bar was found when the reference plugin was disabled",
                and(
                        referenceMenuOnTheTopNavigationBarLocator().element().isPresent(),
                        referenceMenuOnTheTopNavigationBarLocator().element().isVisible()
                ), isFalse().now()
        );
    }

    private void assertThatReferenceMenuOnTheTopNavigationBarIsPresentAndVisible()
    {
        assertThat("The Reference Menu on the Top Navigation Bar was not found when the reference plugin was enabled",
                and(
                        referenceMenuOnTheTopNavigationBarLocator().element().isPresent(),
                        referenceMenuOnTheTopNavigationBarLocator().element().isVisible()
                ), isTrue().now()
        );
    }

    private void assertThatReferenceMenuDropDownArrowOnTheTopNavigationBarIsPresentAndVisible()
    {
        assertThat("The Reference Menu Drop Down Arrow on the Top Navigation Bar was not found when the reference plugin was enabled",
                and(
                        referenceMenuDropDownArrowOnTheTopNavigationBarLocator().element().isPresent(),
                        referenceMenuDropDownArrowOnTheTopNavigationBarLocator().element().isVisible()
                ), isTrue().now()
        );
    }

    private void assertThatTheForumsMenuItemOnTopNavigationBarIsPresentAndVisible()
    {
        assertThat("The Forums Menu Item on the Reference Menu was found when the reference plugin was enabled",
                and(
                        forumsMenuItemOnTopNavigationBarLocator().element().isPresent(),
                        forumsMenuItemOnTopNavigationBarLocator().element().isVisible()
                ), isTrue().by(context().timeoutFor(Timeouts.AJAX_ACTION))
        );
    }

    private void assertThatTheCACMenuItemOnTopNavigationBarIsPresentAndVisible()
    {
        assertThat("The CAC Menu Item on the Reference Menu was not found when the reference plugin was enabled",
                and(
                        cacMenuItemOnTopNavigationBarLocator().element().isPresent(),
                        cacMenuItemOnTopNavigationBarLocator().element().isVisible()
                ), isTrue().by(context().timeoutFor(Timeouts.AJAX_ACTION))
        );
    }

    private void reallyRefreshThePage()
    {
        client.refresh();
        client.waitForPageToLoad();
    }

    private void displayReferenceMenu()
    {
        client.click(ID.create(REFERENCE_MENU_DROP_DOWN_LINK_ID));
    }

    private SeleniumLocator referenceMenuDropDownArrowOnTheTopNavigationBarLocator()
    {
        return id(REFERENCE_MENU_DROP_DOWN_LINK_ID, context());
    }

    private SeleniumLocator cacMenuItemOnTopNavigationBarLocator()
    {
        return jQuery(REFERENCE_CAC_MENU_ITEM_JQUERY_LOCATOR, context());
    }

    private SeleniumLocator forumsMenuItemOnTopNavigationBarLocator()
    {
        return jQuery(REFERENCE_FORUMS_MENU_ITEM_JQUERY_LOCATOR, context());
    }

    private SeleniumLocator referenceMenuOnTheTopNavigationBarLocator()
    {
        return id(REFERENCE_MENU_TOP_NAVIGATION_BAR_LINK_ID, context());
    }

}
