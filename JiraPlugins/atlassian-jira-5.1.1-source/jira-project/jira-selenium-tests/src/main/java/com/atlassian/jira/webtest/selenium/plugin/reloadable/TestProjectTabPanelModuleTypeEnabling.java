package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.ID;

/**
 * <p/>
 * Test that the 'web-resource' plugin module type becomes present when going from 'never enabled' to enabled state.
 * Also referred to as 'ZERO to ON scenario'.
 *
 * @since v4.3
 */

@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestProjectTabPanelModuleTypeEnabling extends AbstractReloadablePluginsSeleniumTest
{
    private static final String REFERENCE_PROJECT_TAB_PANEL_CONTENT_JQUERY_LOCATOR = ".module img[src$=\"JIRAMaster.png\"]";
    private static final String REFERENCE_PROJECT_TAB_PANEL_LINK_ID = "reference-project-tab-panel-panel";

    // assert the structure of the panel is present
    private static final String REFERENCE_PROJECT_TAB_PANEL_JQUERY_LOCATOR = ".vertical.tabs #" + REFERENCE_PROJECT_TAB_PANEL_LINK_ID + ".browse-tab";
    private static final String PROJECT_KEY = "HSP";

    public void testProjectTabPanelIsNotPresentWhenPluginIsDisabled()
    {
        getNavigator().browseProject(PROJECT_KEY);
        assertThatReferencePluginProjectTabPanelIsNotPresent();
    }

    public void testProjectTabPanelIsPresentAndHasContentWhenPluginIsEnabled()
    {
        enableReferencePlugin();

        getNavigator().browseProject(PROJECT_KEY);
        assertThatReferencePluginProjectTabPanelIsPresent();

        clickOnReferencePluginProjectTabPanel();
        assertThatReferencePluginProjectTabPanelHasContent();

    }

    private void assertThatReferencePluginProjectTabPanelIsNotPresent()
    {
        assertThat("Reference Project Tab Panel was present when plugin was disabled",
                and(
                        projectTabLocator().element().isPresent(),
                        projectTabLocator().element().isVisible()
                ),
                isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatReferencePluginProjectTabPanelIsPresent()
    {
        assertThat("Reference Project Tab Panel was not present when plugin was enabled",
                and(
                        projectTabLocator().element().isPresent(),
                        projectTabLocator().element().isVisible()
                ),
                isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void clickOnReferencePluginProjectTabPanel()
    {
        client.click(ID.create(REFERENCE_PROJECT_TAB_PANEL_LINK_ID));
    }

    private void assertThatReferencePluginProjectTabPanelHasContent()
    {
        assertThat("Reference Project Tab Panel contents were not present when plugin was enabled",
                and(
                        projectTabContentLocator().element().isPresent(),
                        projectTabContentLocator().element().isVisible()
                ),
                isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private Locator projectTabContentLocator()
    {
        return jQuery(REFERENCE_PROJECT_TAB_PANEL_CONTENT_JQUERY_LOCATOR, context());
    }

    private Locator projectTabLocator()
    {
        return jQuery(REFERENCE_PROJECT_TAB_PANEL_JQUERY_LOCATOR, context());
    }

}