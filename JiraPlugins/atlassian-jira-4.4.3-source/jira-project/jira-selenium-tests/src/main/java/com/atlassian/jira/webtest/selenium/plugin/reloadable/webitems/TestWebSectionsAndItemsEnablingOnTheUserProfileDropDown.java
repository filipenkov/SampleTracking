package com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.AbstractReloadablePluginsSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.id;

/**
 * Responsible for testing that web sections and items in the user profile drop-down can be enabled.
 * <br/>
 * The test cases assumes the reference plugin is installed and disabled. This is what we call the ZERO TO ON scenario.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebSectionsAndItemsEnablingOnTheUserProfileDropDown extends AbstractReloadablePluginsSeleniumTest
{
    private static final String USER_PROFILE_DROPDOWN_CSS_LOCATOR = "#header-details-user span a.drop.aui-dd-link";
    private static final String USER_OPTIONS_REFERENCE_SECTION_ID = "reference-section-user-options";
    private static final String USER_OPTIONS_REFERENCE_SECTION_REFERENCE_ITEM_ID =
            "reference-item-user-options-reference-section";
    private static final String USER_OPTIONS_PERSONAL_SECTION_REFERENCE_ITEM_ID = "reference-item-user-options-personal-section";

    public void testDropDownItemsAndSectionsShouldNotBeVisibleWhenTheReferencePluginIsDisabled() throws Exception
    {
        clickOnTheUserProfileDropDown();

        assertThatTheReferenceSectionIsNeitherVisibleNorPresent();
        assertThatTheReferenceSectionItemIsNeitherVisibleNorPresent();
        assertThatTheReferenceItemOnThePersonalSectionIsNeitherVisibleNorPresent();
    }

    private void clickOnTheUserProfileDropDown()
    {
        client.click(Locators.CSS.create(USER_PROFILE_DROPDOWN_CSS_LOCATOR));
    }

    private void assertThatTheReferenceSectionIsNeitherVisibleNorPresent()
    {
        assertTrueByDefaultTimeout("The reference section is visible on the user profile drop-down, and the reference "
                + "plugin has not been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_ID, context()).element().isNotVisible());


        assertThat("The reference section was found in the user profile drop-down, and the reference plugin has not "
                + "been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_ID, context()).element().isNotPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionItemIsNeitherVisibleNorPresent()
    {
        assertTrueByDefaultTimeout("The reference section item is visible on the user profile drop-down, and the "
                + "reference plugin has not been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_REFERENCE_ITEM_ID, context()).element().isNotVisible());

        assertThat("The reference section item was found in the user profile drop-down, and the reference plugin has "
                + "not been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_REFERENCE_ITEM_ID, context()).element().isNotPresent(), isTrue().now());
    }

    private void assertThatTheReferenceItemOnThePersonalSectionIsNeitherVisibleNorPresent()
    {
        assertTrueByDefaultTimeout("The reference item on the personal section is visible on the user profile "
                + "drop-down, and the reference plugin has not been enabled",
                id(USER_OPTIONS_PERSONAL_SECTION_REFERENCE_ITEM_ID, context()).element().isNotVisible());

        assertThat("The reference section item on the personal section was found in the user profile drop-down, and "
                + "the reference plugin has not been enabled",
                id(USER_OPTIONS_PERSONAL_SECTION_REFERENCE_ITEM_ID, context()).element().isNotPresent(), isTrue().now());
    }

    public void testDropDownItemsAndSectionsShouldBeVisibleWhenTheReferencePluginIsEnabled() throws Exception
    {
        enableReferencePlugin();

        // go to the dashboard to reload the user profile drop-down.
        globalPages().goToDashboard();
        clickOnTheUserProfileDropDown();

        assertThatTheReferenceSectionIsVisibleAndPresent();
        assertThatTheReferenceSectionItemIsVisibleAndPresent();
        assertThatTheReferenceItemOnThePersonalSectionIsVisibleAndPresent();
    }

    private void assertThatTheReferenceSectionIsVisibleAndPresent()
    {
        assertTrueByDefaultTimeout("The reference section is not visible on the user profile drop-down, and the reference "
                + "plugin has been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_ID, context()).element().isVisible());


        assertThat("The reference section was not found in the user profile drop-down, and the reference plugin has "
                + "been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_ID, context()).element().isPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionItemIsVisibleAndPresent()
    {
        assertTrueByDefaultTimeout("The reference section item is not visible on the user profile drop-down, and the "
                + "reference plugin has been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_REFERENCE_ITEM_ID, context()).element().isVisible());

        assertThat("The reference section item was not found in the user profile drop-down, and the reference plugin has "
                + "been enabled",
                id(USER_OPTIONS_REFERENCE_SECTION_REFERENCE_ITEM_ID, context()).element().isPresent(), isTrue().now());
    }

    private void assertThatTheReferenceItemOnThePersonalSectionIsVisibleAndPresent()
    {
        assertTrueByDefaultTimeout("The reference item on the personal section is not visible on the user profile "
                + "drop-down, and the reference plugin has been enabled",
                id(USER_OPTIONS_PERSONAL_SECTION_REFERENCE_ITEM_ID, context()).element().isVisible());

        assertThat("The reference section item on the personal section was not found in the user profile drop-down, and "
                + "the reference plugin has been enabled",
                id(USER_OPTIONS_PERSONAL_SECTION_REFERENCE_ITEM_ID, context()).element().isPresent(), isTrue().now());
    }
}
