package com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.model.SimpleIssueData;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.AbstractReloadablePluginsSeleniumTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertFalseByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.assertTrueByDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isFalse;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.isTrue;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.id;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.ID;

/**
 * Responsible for verifying that a custom menu item or section on the issue operations bar can be enabled.
 * <br/>
 * The test cases assume the reference plugin is installed and disabled. This is what we call the ZERO TO ON scenario.
 *
 * @since v4.3
 */
@Ignore ("Disabling till we resolve memory leak")
@WebTest({Category.SELENIUM_TEST })
public class TestWebSectionsAndItemsEnablingOnTheIssueOperationsBar extends AbstractReloadablePluginsSeleniumTest
{
    private static final long ISSUE_ID = 10000L;
    private static final String ISSUE_KEY = "HSP-1";
    private static final String REFERENCE_TOP_LEVEL_OPERATION_LINK_ID = "reference-top-level-operation";
    private static final String REFERENCE_SECTION_ON_OPS_BAR_JQUERY_LOCATOR =
            "#opsbar-operations_more_drop .aui-list-section #reference-operation";
    private static final String MORE_OPERATIONS_MENU_LINK_ID = "opsbar-operations_more";
    private static final String REFERENCE_MENU_ITEM_TRANSITIONS_LINK_ID = "reference-transition-item";
    private static final String MORE_ACTIONS_MENU_ID = "opsbar-operations_more_drop";

    public void testIssueOperationsBarWebSectionsAndItemsAreNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled()
    {
        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));

        _testReferenceTopLevelOperationIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled();
        _testReferenceSectionOnTheMoreActionsMenuIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled();
        _testReferenceMenuItemOnTheTransitionsSectionIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled();
    }

    public void testIssueOperationsBarWebSectionsAndItemsArePresentAndVisibleWhenTheReferencePluginIsEnabled()
    {
        enableReferencePlugin();

        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));

        _testReferenceTopLevelOperationIsPresentAndVisibleWhenTheReferencePluginIsEnabled();
        _testReferenceSectionOnTheMoreActionsMenuIsPresentAndVisibleWhenTheReferencePluginIsEnabled();
        _testReferenceMenuItemOnTheTransitionsSectionIsPresentAndVisibleWhenTheReferencePluginIsEnabled();
    }

    public void _testReferenceTopLevelOperationIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled()
    {
        assertThatReferenceTopLevelOperationIsNeitherPresentNorVisible();
    }

    public void _testReferenceSectionOnTheMoreActionsMenuIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled()
    {
        displayMoreActionsMenu();

        assertThatReferenceSectionOnTheMoreActionsMenuIsNeitherPresentNotVisible();

        closeMoreActionsMenu();
    }

    public void _testReferenceMenuItemOnTheTransitionsSectionIsNeitherPresentNorVisibleWhenTheReferencePluginIsDisabled()
    {
        assertThatReferenceMenuItemOnTheTransitionsSectionIsNeitherPresentNorVisible();
    }

    public void _testReferenceTopLevelOperationIsPresentAndVisibleWhenTheReferencePluginIsEnabled()
    {
        assertThatReferenceTopLevelOperationIsPresentAndVisible();
    }

    public void _testReferenceSectionOnTheMoreActionsMenuIsPresentAndVisibleWhenTheReferencePluginIsEnabled()
    {
        displayMoreActionsMenu();

        assertThatReferenceSectionOnTheMoreActionsMenuIsPresentAndVisible();

        closeMoreActionsMenu();
    }

    public void _testReferenceMenuItemOnTheTransitionsSectionIsPresentAndVisibleWhenTheReferencePluginIsEnabled()
    {
        enableReferencePlugin();
        globalPages().goToViewIssueFor(new SimpleIssueData(ISSUE_ID, ISSUE_KEY));

        assertThatReferenceMenuItemOnTheTransitionsSectionIsPresentAndVisible();
    }

    private void assertThatReferenceTopLevelOperationIsNeitherPresentNorVisible()
    {
        assertThat("The Reference Top Level Operation menu was found when the reference plugin was disabled",
                and(
                        referenceTopLevelOperationLocator().element().isPresent(),
                        referenceTopLevelOperationLocator().element().isVisible()
                ), isFalse().now()
        );
    }

    private void assertThatReferenceSectionOnTheMoreActionsMenuIsNeitherPresentNotVisible()
    {
        assertFalseByDefaultTimeout("The Reference Operation Section on the Operations Bar was found when the "
                + "reference plugin was disabled",
                and(
                        referenceSectionOnTheOperationsBarLocator().element().isPresent(),
                        referenceSectionOnTheOperationsBarLocator().element().isVisible()
                )
        );
    }

    private void assertThatReferenceMenuItemOnTheTransitionsSectionIsNeitherPresentNorVisible()
    {
        assertThat("The Reference Menu Item on the Transitions Section was found when the reference plugin was disabled",
                and(
                        referenceMenuItemOnTheTransitionSectionLocator().element().isPresent(),
                        referenceMenuItemOnTheTransitionSectionLocator().element().isVisible()
                ), isFalse().now()
        );
    }

    private void assertThatReferenceTopLevelOperationIsPresentAndVisible()
    {
        assertThat("The Reference Top Level Operation menu was not found when the reference plugin was enabled",
                and(
                        referenceTopLevelOperationLocator().element().isPresent(),
                        referenceTopLevelOperationLocator().element().isVisible()
                ), isTrue().now()
        );
    }

    private void assertThatReferenceSectionOnTheMoreActionsMenuIsPresentAndVisible()
    {
        assertTrueByDefaultTimeout("The Reference Operation Section on the Operations Bar was not found "
                + "when the reference plugin was enabled",
                and(
                        referenceSectionOnTheOperationsBarLocator().element().isPresent(),
                        referenceSectionOnTheOperationsBarLocator().element().isVisible()
                )
        );
    }

    private void assertThatReferenceMenuItemOnTheTransitionsSectionIsPresentAndVisible()
    {
        assertThat("The Reference Menu Item on the Transitions Section was not found when the reference plugin was enabled",
                and(
                        referenceMenuItemOnTheTransitionSectionLocator().element().isPresent(),
                        referenceMenuItemOnTheTransitionSectionLocator().element().isVisible()
                ), isTrue().now()
        );
    }

    private SeleniumLocator referenceMenuItemOnTheTransitionSectionLocator()
    {
        return id(REFERENCE_MENU_ITEM_TRANSITIONS_LINK_ID, context());
    }

    private SeleniumLocator referenceSectionOnTheOperationsBarLocator()
    {
        return jQuery(REFERENCE_SECTION_ON_OPS_BAR_JQUERY_LOCATOR, context());
    }

    private SeleniumLocator referenceTopLevelOperationLocator()
    {
        return id(REFERENCE_TOP_LEVEL_OPERATION_LINK_ID, context());
    }

    private SeleniumLocator moreActionsMenuLocator()
    {
        return id(MORE_ACTIONS_MENU_ID, context());
    }

    private void displayMoreActionsMenu()
    {
        assertThat("More actions menu was already open", moreActionsMenuLocator().element().isVisible(), isFalse().now());
        toggleMoreActionsMenu();
    }

    private void closeMoreActionsMenu()
    {
        assertThat("More actions menu was already closed", moreActionsMenuLocator().element().isVisible(), isTrue().now());
        toggleMoreActionsMenu();
    }

    private void toggleMoreActionsMenu()
    {
        client.click(ID.create(MORE_OPERATIONS_MENU_LINK_ID));
    }

}
