package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;


/**
 * <p>
 * Test that the 'web-resource' plugin module type becomes present when going from 'never enabled'
 * to enabled state. Also referred to as 'ZERO to ON scenario'.
 *
 * @since v4.3
 */

@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebResourceModuleTypeEnabling extends AbstractReloadablePluginsSeleniumTest
{
    private final String GENERAL_WEBRESOURCE_INDICATOR = "general webresource present";
    private final String ADMIN_WEBRESOURCE_INDICATOR = "admin webresource present";
    private final String USERPROFILE_WEBRESOURCE_INDICATOR = "userprofile webresource present";

    public void testWebResourceModuleNotPresentWhenReferencePluginIsDisabled()
    {
        assertThatWebResourcesAreNotActiveOnDashboard();
        assertThatWebResourcesAreNotActiveOnAdmin();
        assertThatWebResourcesAreNotActiveOnUserProfile();
    }

    public void testWebResourceModulePresentWhenReferencePluginIsEnabled()
    {
        enableReferencePlugin();

        assertThatOnlyGeneralWebResourcesAreActiveOnDashboard();
        assertThatOnlyAdminWebResourcesAreActiveOnAdminPage();
        assertThatGeneralAndUserProfileWebResourcesAreActiveOnUserProfilePage();
    }

    private void assertThatWebResourcesAreNotActiveOnAdmin()
    {
        assertThatResourceIsNotActiveOnAdmin(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnAdmin(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnAdmin(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatWebResourcesAreNotActiveOnDashboard()
    {
        assertThatWebResourceIsNotActiveOnDashboard(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatWebResourceIsNotActiveOnDashboard(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatWebResourceIsNotActiveOnDashboard(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatWebResourcesAreNotActiveOnUserProfile()
    {
        assertThatResourceIsNotActiveOnUserProfile(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnUserProfile(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnUserProfile(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatOnlyGeneralWebResourcesAreActiveOnDashboard()
    {
        assertThatWebResourceIsActiveOnDashboard(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatWebResourceIsNotActiveOnDashboard(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatWebResourceIsNotActiveOnDashboard(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatOnlyAdminWebResourcesAreActiveOnAdminPage()
    {
        assertThatResourceIsNotActiveOnAdmin(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatResourceIsActiveOnAdmin(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnAdmin(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatGeneralAndUserProfileWebResourcesAreActiveOnUserProfilePage()
    {
        assertThatResourceIsActiveOnUserProfile(GENERAL_WEBRESOURCE_INDICATOR);
        assertThatResourceIsNotActiveOnUserProfile(ADMIN_WEBRESOURCE_INDICATOR);
        assertThatResourceIsActiveOnUserProfile(USERPROFILE_WEBRESOURCE_INDICATOR);
    }

    private void assertThatWebResourceIsNotActiveOnDashboard(final String resourceIndicator)
    {
        globalPages().goToDashboard();
        assertThat("Expected " + resourceIndicator + " to not be present on Dashboard, but it was.",
                bodyLocator().element().containsText(resourceIndicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatResourceIsNotActiveOnAdmin(final String resourceIndicator)
    {
        globalPages().goToAdministration();
        assertThat("Expected " + resourceIndicator + " to not be present on Admin page, but it was.",
                bodyLocator().element().containsText(resourceIndicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatResourceIsNotActiveOnUserProfile(final String resourceIndicator)
    {
        getNavigator().gotoUserProfile();
        assertThat("Expected " + resourceIndicator + " to not be present on User Profile page, but it was.",
                bodyLocator().element().containsText(resourceIndicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatWebResourceIsActiveOnDashboard(final String resourceIndicator)
    {
        globalPages().goToDashboard();
        assertThat("Expected " + resourceIndicator + " to be present on Dashboard, but it was not.",
                bodyLocator().element().containsText(resourceIndicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatResourceIsActiveOnAdmin(final String resourceIndicator)
    {
        globalPages().goToAdministration();
        assertThat("Expected " + resourceIndicator + " to be present on Admin page, but it was not.",
                bodyLocator().element().containsText(resourceIndicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatResourceIsActiveOnUserProfile(final String resourceIndicator)
    {
        getNavigator().gotoUserProfile();
        assertThat("Expected " + resourceIndicator + " to be present on Admin page, but it was not.",
                bodyLocator().element().containsText(resourceIndicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private Locator bodyLocator()
    {
        return context().ui().body();
    }

}
