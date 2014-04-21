package com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.model.SimpleIssueData;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.AbstractReloadablePluginsSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.id;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.ID;

/**
 * Responsible for verifying that a custom menu item on the user avatar menu can be enabled.
 * <br/>
 * The test cases assumes the reference plugin is installed and disabled. This is what we call the ZERO TO ON scenario.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebSectionsAndItemsEnablingOnTheUserAvatarHoverMenu extends AbstractReloadablePluginsSeleniumTest
{
    private static final long ISSUE_ID = 10000L;
    private static final String ISSUE_KEY = "HSP-1";
    private static final String ISSUE_REPORTER_LINK_ID = "issue_summary_reporter_admin";
    private static final String ISSUE_REPORTER_USER_HOVER_CONTAINER_ID = "admin_user_hover";
    private static final String ISSUE_REPORTER_USER_HOVER_REFERENCE_ITEM_JQUERY_LOCATOR =
            "#admin_user_hover div.user-hover-buttons a.user-hover-more:contains('Reference Item')";

    public void testReferenceMenuItemIsNeitherVisibleNorPresentWhenTheReferencePluginIsDisabled()
    {
        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));
        displayTheUserAvatarHoverMenuForTheIssueReporter();

        assertThatTheReferenceMenuItemIsNeitherVisibleNorPresent();
    }

    private void assertThatTheReferenceMenuItemIsNeitherVisibleNorPresent()
    {
        assertTrueByDefaultTimeout("The reference item on the user avatar hover menu is visible but the reference "
                + "plugin has not been enabled", issueReporterUserHoverReferenceItemLocator().element().isNotVisible());

        assertTrueByDefaultTimeout("The reference item on the user avatar hover menu is present but the reference "
                + "plugin has not been enabled", issueReporterUserHoverReferenceItemLocator().element().isNotPresent());
    }

    public void testReferenceMenuItemIsVisibleAndPresentWhenTheReferencePluginIsEnabled()
    {
        enableReferencePlugin();

        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));
        displayTheUserAvatarHoverMenuForTheIssueReporter();

        assertThatTheReferenceMenuItemIsVisibleAndPresent();
    }

    private void assertThatTheReferenceMenuItemIsVisibleAndPresent()
    {
        assertTrueByDefaultTimeout("The reference item on the user avatar hover menu was not found and the reference "
                + "plugin has been enabled",
                and(
                        issueReporterUserHoverReferenceItemLocator().element().isPresent(),
                        issueReporterUserHoverReferenceItemLocator().element().isVisible()
                )
        );
    }

    private void displayTheUserAvatarHoverMenuForTheIssueReporter()
    {
        Mouse.mouseover(client, ID.create(ISSUE_REPORTER_LINK_ID));

        assertThat("The user hover menu is not open after mousing over the issue reporter",
                and(
                        issueReporterUserHoverLocator().element().isPresent(),
                        issueReporterUserHoverLocator().element().isVisible()
                ), isTrue().by(context().timeoutFor(Timeouts.DIALOG_LOAD))
        );
    }

    private SeleniumLocator issueReporterUserHoverLocator()
    {
        return id(ISSUE_REPORTER_USER_HOVER_CONTAINER_ID, context());
    }

    private SeleniumLocator issueReporterUserHoverReferenceItemLocator()
    {
        return jQuery(ISSUE_REPORTER_USER_HOVER_REFERENCE_ITEM_JQUERY_LOCATOR, context());
    }
}
