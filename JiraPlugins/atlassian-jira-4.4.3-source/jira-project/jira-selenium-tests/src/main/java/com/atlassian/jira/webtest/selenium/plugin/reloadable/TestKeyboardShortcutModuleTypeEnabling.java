package com.atlassian.jira.webtest.selenium.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.model.SimpleIssueData;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;


/**
 * <p>
 * Test that the 'keyboard-shortcut' plugin module type behaves correctly when going from 'never enabled'
 * to enabled state. Also referred to as 'ZERO to ON scenario'.
 *
 * @since v4.3
 */

@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestKeyboardShortcutModuleTypeEnabling extends AbstractReloadablePluginsSeleniumTest
{
    private static final long ISSUE_ID = 10000L;
    private static final String ISSUE_KEY = "HSP-1";

    private final String GLOBAL_SHORTCUT_FIRED_INDICATOR = "globalshortcut";
    private final String ISSUE_NAVIGATION_SHORTCUT_FIRED_INDICATOR = "issuenavigationshortcut";
    private final String ISSUE_ACTION_SHORTCUT_FIRED_INDICATOR = "issueactionshortcut";

    public void testShortCutNotPresent()
    {
        assertThatShortcutIsNotActiveOnDashboard(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);

        assertThatShortcutIsNotActiveOnIssueNavigator(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsNotActiveOnIssueNavigator(Shortcuts.ISSUE_NAVIGATION_SHORTCUT, ISSUE_NAVIGATION_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsNotActiveOnIssueNavigator(Shortcuts.ISSUE_ACTION_SHORTCUT, ISSUE_ACTION_SHORTCUT_FIRED_INDICATOR);

        assertThatShortcutIsNotActiveOnIssue(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsNotActiveOnIssue(Shortcuts.ISSUE_NAVIGATION_SHORTCUT, ISSUE_NAVIGATION_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsNotActiveOnIssue(Shortcuts.ISSUE_ACTION_SHORTCUT, ISSUE_ACTION_SHORTCUT_FIRED_INDICATOR);
    }

    public void testShortCutPresent()
    {
        enableReferencePlugin();

        assertThatShortcutIsActiveOnDashboard(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);

        assertThatShortcutIsActiveOnIssueNavigator(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsActiveOnIssueNavigator(Shortcuts.ISSUE_NAVIGATION_SHORTCUT, ISSUE_NAVIGATION_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsActiveOnIssueNavigator(Shortcuts.ISSUE_ACTION_SHORTCUT, ISSUE_ACTION_SHORTCUT_FIRED_INDICATOR);

        assertThatShortcutIsActiveOnIssue(Shortcuts.GLOBAL_CONTEXT_SHORTCUT, GLOBAL_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsActiveOnIssue(Shortcuts.ISSUE_NAVIGATION_SHORTCUT, ISSUE_NAVIGATION_SHORTCUT_FIRED_INDICATOR);
        assertThatShortcutIsActiveOnIssue(Shortcuts.ISSUE_ACTION_SHORTCUT, ISSUE_ACTION_SHORTCUT_FIRED_INDICATOR);

    }

    private void assertThatShortcutIsNotActiveOnDashboard(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnDashboard(shortcut);

        assertThat("Expected shortcut " + shortcut + " to be fired on Dashboard, but was.",
                bodyLocator().element().containsText(indicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatShortcutIsNotActiveOnIssueNavigator(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnIssueNavigator(shortcut);

        assertThat("Expected shortcut " + shortcut + " not to be fired on Issue Navigator, but was.",
                bodyLocator().element().containsText(indicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatShortcutIsNotActiveOnIssue(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnIssue(shortcut);

        assertThat("Expected shortcut " + shortcut + " not to be fired on Issue, but was.",
                bodyLocator().element().containsText(indicator), isFalse().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatShortcutIsActiveOnDashboard(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnDashboard(shortcut);

        assertThat("Expected shortcut " + shortcut + " to be fired on Dashboard, but was not.",
                bodyLocator().element().containsText(indicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatShortcutIsActiveOnIssueNavigator(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnIssueNavigator(shortcut);

        assertThat("Expected shortcut " + shortcut + " to be fired on Issue Navigator, but was not.",
                bodyLocator().element().containsText(indicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void assertThatShortcutIsActiveOnIssue(final Shortcuts shortcut, final String indicator)
    {
        fireShortcutOnIssue(shortcut);

        assertThat("Expected shortcut " + shortcut + " to be fired on Issue, but was not.",
                bodyLocator().element().containsText(indicator), isTrue().by(timeouts().timeoutFor(Timeouts.AJAX_ACTION)));
    }

    private void fireShortcutOnDashboard(Shortcuts shortcut)
    {
        globalPages().goToDashboard();
        assertTrueByDefaultTimeout(globalPages().dashboard().isAt());

        context().ui().pressInBody(shortcut);
    }

    private void fireShortcutOnIssueNavigator(Shortcuts shortcut)
    {
        getNavigator().findAllIssues();
        globalPages().goToIssueNavigator();
        assertTrueByDefaultTimeout(globalPages().issueNavigator().isAt());

        context().ui().pressInBody(shortcut);
    }

    private void fireShortcutOnIssue(Shortcuts shortcut)
    {
        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));
        client.waitForPageToLoad();

        context().ui().pressInBody(shortcut);
    }

    private Locator bodyLocator()
    {
        return context().ui().body();
    }

}
