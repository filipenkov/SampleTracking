package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

@WebTest({Category.SELENIUM_TEST })
public class TestHideShowToggleOnViewIssueScreen extends JiraSeleniumTest
{
    private static final String JQUERY_VERBOSE_SELECTOR = "jquery=#%s .verbose";
    private static final String JQUERY_CONCISE_SELECTOR = "jquery=#%s .concise";
    private static final String JQUERY_NOT_COLLAPSED_SELECTOR = "jquery=#%s:not(.collapsed)";
    private static final String JQUERY_COLLAPSED_SELECTOR = "jquery=#%s.collapsed";

    /**
     * Tests that the toggle hide/show link on Issue screen works.
     * See JRA-16843
     */
    public void testHideShowToggle()
    {
        restoreData("TestEnvironmentField.xml");
        getNavigator().gotoIssue("HSP-1");

        // note: environment field needs to have excessive text for twixi to be displayed
        checkToggleForModule("descriptionmodule");
        checkInverseToggleForField("field-environment");
        checkToggleForField("comment-10000");
        checkToggleForField("comment-10001");
        checkToggleForField("comment-10002");
        checkToggleForField("comment-10003");

        // JRADEV-2360: collapsing comments then collapsing the Activity section should not change visibility of
        // comments upon expanding
        hideField("comment-10000");
        hideField("comment-10002");
        hideModule("activitymodule");
        showModule("activitymodule");
        assertFieldHidden("comment-10000");
        assertFieldShown("comment-10001");
        assertFieldHidden("comment-10002");
        assertFieldShown("comment-10003");

        // similar for Environment and Details section
        hideModule("details-module");
        showModule("details-module");
        assertFieldHidden("field-environment");
        
        showField("field-environment");
        hideModule("details-module");
        showModule("details-module");
        assertFieldShown("field-environment");
    }

    private void checkToggleForField(String fieldName)
    {
        assertFieldShown(fieldName);

        hideField(fieldName);
        assertFieldHidden(fieldName);

        showField(fieldName);
        assertFieldShown(fieldName);
    }

    private void checkInverseToggleForField(String fieldName)
    {
        assertFieldHidden(fieldName);

        showField(fieldName);
        assertFieldShown(fieldName);

        hideField(fieldName);
        assertFieldHidden(fieldName);
    }

    private void checkToggleForModule(String moduleName)
    {
        assertModuleShown(moduleName);

        hideModule(moduleName);
        assertModuleHidden(moduleName);

        showModule(moduleName);
        assertModuleShown(moduleName);
    }

    private void assertModuleShown(final String moduleName)
    {
        assertThat.elementVisible(notCollapsedSelector(moduleName));
        assertThat.elementNotVisible(collapsedSelector(moduleName));
    }

    private void assertModuleHidden(final String moduleName)
    {
        assertThat.elementNotVisible(notCollapsedSelector(moduleName));
        assertThat.elementVisible(collapsedSelector(moduleName));
    }

    private void assertFieldShown(final String fieldName)
    {
        assertThat.elementVisible(verboseSelector(fieldName));
        assertThat.elementNotVisible(conciseSelector(fieldName));
    }

    private void assertFieldHidden(final String fieldName)
    {
        assertThat.elementVisible(conciseSelector(fieldName));
        assertThat.elementNotVisible(verboseSelector(fieldName));
    }

    private void showModule(final String fieldName)
    {
        client.click(collapsedSelector(fieldName) + " .toggle-title");
    }

    private void hideModule(final String moduleName)
    {
        client.click(notCollapsedSelector(moduleName) + " .toggle-title");
    }

    private void showField(final String fieldName)
    {
        client.click(conciseSelector(fieldName) + " .twixi");
    }

    private void hideField(final String fieldName)
    {
        client.click(verboseSelector(fieldName) + " .twixi");
    }

    private static String notCollapsedSelector(final String moduleName)
    {
        return String.format(JQUERY_NOT_COLLAPSED_SELECTOR, moduleName);
    }

    private static String collapsedSelector(final String moduleName)
    {
        return String.format(JQUERY_COLLAPSED_SELECTOR, moduleName);
    }

    private static String verboseSelector(final String fieldName)
    {
        return String.format(JQUERY_VERBOSE_SELECTOR, fieldName);
    }

    private static String conciseSelector(final String fieldName)
    {
        return String.format(JQUERY_CONCISE_SELECTOR, fieldName);
    }
}
