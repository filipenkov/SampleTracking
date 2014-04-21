package com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.TimedAssertions;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.page.admin.CustomFields;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.AbstractReloadablePluginsSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;

/**
 * Responsible for testing that web sections and items in the administration page panel can be enabled.
 * <br/>
 * The test cases assumes the reference plugin is installed and disabled. This is what we call the ZERO TO ON scenario.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebSectionsAndItemsOnTheAdministrationPagePanelEnabling extends AbstractReloadablePluginsSeleniumTest
{
    private static final String ADMIN_REFERENCE_SECTION_ID = "admin.reference-section-admin-page-links";
    private static final String REFERENCE_SECTION_JIRA_KB_LINK_ID = "reference-jira-kb-link";
    private static final String REFERENCE_SECTION_JIRA_PLUGIN_GUIDE_LINK_ID = "reference-jira-plugin-guide-link";
    private static final String USERS_SECTION_REFERENCE_OPENID_LINK_ID = "reference-openid-link";

    public void testMenuItemsAndSectionsShouldNotBeVisibleWhenTheReferencePluginIsDisabled() throws Exception
    {
        globalPages().administration().goTo();

        assertThatTheReferenceSectionIsNotPresent();
        assertThatTheReferenceSectionItemsAreNotPresent();
        assertThatTheReferenceItemOnTheUsersSectionIsNotPresent();
    }

    public void testMenuItemsAndSectionsShouldBeVisibleWhenTheReferencePluginIsEnabled() throws Exception
    {
        enableReferencePlugin();

        // We need to go to another screen so that the admin page gets reloaded. This sucks I know, but the framework
        // assumes that if I tell it to go to a page and I am already there I don't want the page to be reloaded. As a
        // matter of fact, in this case I do want a reload !!! ...
        globalPages().goToAdministration().goToPage(CustomFields.class);

        assertThatTheReferenceSectionIsPresent();
        assertThatTheReferenceSectionItemsArePresent();
        assertThatTheReferenceItemOnTheUsersSectionIsPresent();
    }

    private void assertThatTheReferenceItemOnTheUsersSectionIsPresent()
    {
        TimedAssertions.assertThat("The reference openid item on the users section section was not found, "
                + "and the reference plugin has been enabled",
                SeleniumLocators.id(USERS_SECTION_REFERENCE_OPENID_LINK_ID,
                        context()).element().isPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionItemsArePresent()
    {
        TimedAssertions.assertThat("The reference jira kb item on the reference admin section was not found, "
                + "and the reference plugin has been enabled",
                SeleniumLocators.id(REFERENCE_SECTION_JIRA_KB_LINK_ID,
                        context()).element().isPresent(), isTrue().now());

        TimedAssertions.assertThat("The reference jira plugin guide item on the reference admin section was not found, "
                + "and the reference plugin has been enabled",
                SeleniumLocators.id(REFERENCE_SECTION_JIRA_PLUGIN_GUIDE_LINK_ID,
                        context()).element().isPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionIsPresent()
    {
        TimedAssertions.assertThat("The reference admin section was not found, and the reference plugin has been enabled",
                SeleniumLocators.id(ADMIN_REFERENCE_SECTION_ID, context()).element().isPresent(), isTrue().now());        
    }

    private void assertThatTheReferenceItemOnTheUsersSectionIsNotPresent()
    {
        TimedAssertions.assertThat("The reference openid item on the users section section was found, "
                + "and the reference plugin has not been enabled",
                SeleniumLocators.id(USERS_SECTION_REFERENCE_OPENID_LINK_ID,
                        context()).element().isNotPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionItemsAreNotPresent()
    {
        TimedAssertions.assertThat("The reference jira kb item on the reference admin section was found, "
                + "and the reference plugin has not been enabled",
                SeleniumLocators.id(REFERENCE_SECTION_JIRA_KB_LINK_ID,
                        context()).element().isNotPresent(), isTrue().now());

        TimedAssertions.assertThat("The reference jira plugin guide item on the reference admin section was found, "
                + "and the reference plugin has not been enabled",
                SeleniumLocators.id(REFERENCE_SECTION_JIRA_PLUGIN_GUIDE_LINK_ID,
                        context()).element().isNotPresent(), isTrue().now());
    }

    private void assertThatTheReferenceSectionIsNotPresent()
    {
        TimedAssertions.assertThat("The reference admin section was found, and the reference plugin has not been enabled",
                SeleniumLocators.id(ADMIN_REFERENCE_SECTION_ID, context()).element().isNotPresent(), isTrue().now());
    }
}
