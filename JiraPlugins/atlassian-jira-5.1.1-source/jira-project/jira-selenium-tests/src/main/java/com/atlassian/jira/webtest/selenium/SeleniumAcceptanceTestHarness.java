package com.atlassian.jira.webtest.selenium;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.functest.framework.TestSuiteBuilder;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.webtest.selenium.activity.TestActivityStream;
import com.atlassian.jira.webtest.selenium.admin.TestGadgetWhitelistUpgrade;
import com.atlassian.jira.webtest.selenium.admin.groupbrowser.TestBulkEditGroupMembers;
import com.atlassian.jira.webtest.selenium.admin.imports.TestXmlImport;
import com.atlassian.jira.webtest.selenium.admin.imports.project.TestProjectImportSelectProject;
import com.atlassian.jira.webtest.selenium.admin.lookandfeel.TestLookAndFeel;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestAdminSectionUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestEmailOptionsUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestIssueLevelSecurityUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestIssueUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestNavigatorUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestNotificationsUserPicker;
import com.atlassian.jira.webtest.selenium.ajaxuserpicker.TestPermissionsUserPicker;
import com.atlassian.jira.webtest.selenium.applinks.TestAppLinksAdministration;
import com.atlassian.jira.webtest.selenium.assignee.TestAssigneeFieldNoContext;
import com.atlassian.jira.webtest.selenium.auidialog.comment.TestCommentDialog;
import com.atlassian.jira.webtest.selenium.auidialog.common.TestCustomFieldsInDialogs;
import com.atlassian.jira.webtest.selenium.auidialog.labels.TestEditLabelsInIssueNavigator;
import com.atlassian.jira.webtest.selenium.auidialog.labels.TestEditLabelsInViewIssue;
import com.atlassian.jira.webtest.selenium.browseprojects.TestBrowseProjectControls;
import com.atlassian.jira.webtest.selenium.browseprojects.TestBrowseProjectNavigation;
import com.atlassian.jira.webtest.selenium.browseprojects.TestChangeLog;
import com.atlassian.jira.webtest.selenium.calendar.TestCalendarPopUp;
import com.atlassian.jira.webtest.selenium.customfields.TestMultiSelectCustomField;
import com.atlassian.jira.webtest.selenium.dashboard.TestAddPortalPage;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboard;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboardEditing;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboardGeneral;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboardMessages;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboardPermissions;
import com.atlassian.jira.webtest.selenium.dashboard.TestDashboardShared;
import com.atlassian.jira.webtest.selenium.dashboard.TestDeletePortalPage;
import com.atlassian.jira.webtest.selenium.dashboard.TestEditPortalPage;
import com.atlassian.jira.webtest.selenium.dashboard.TestGadgetPermissions;
import com.atlassian.jira.webtest.selenium.dashboard.TestManageDashboardsControls;
import com.atlassian.jira.webtest.selenium.dashboard.TestManageDashboardsPermissionsCollapsor;
import com.atlassian.jira.webtest.selenium.dashboard.TestShareDashboardFavourites;
import com.atlassian.jira.webtest.selenium.favourites.TestIssueNavigatorFavourites;
import com.atlassian.jira.webtest.selenium.favourites.TestManageFiltersFavourites;
import com.atlassian.jira.webtest.selenium.fields.TestComponentsField;
import com.atlassian.jira.webtest.selenium.fields.TestFrotherControlRenderers;
import com.atlassian.jira.webtest.selenium.fields.TestMultiSelectInteractions;
import com.atlassian.jira.webtest.selenium.fields.TestVersionsField;
import com.atlassian.jira.webtest.selenium.filters.TestDeleteFilterDialog;
import com.atlassian.jira.webtest.selenium.filters.TestEditFilter;
import com.atlassian.jira.webtest.selenium.filters.TestFilterBrowser;
import com.atlassian.jira.webtest.selenium.filters.TestFilterOrProjectPicker;
import com.atlassian.jira.webtest.selenium.filters.TestFilterPopUp;
import com.atlassian.jira.webtest.selenium.filters.TestManageFiltersControls;
import com.atlassian.jira.webtest.selenium.filters.TestManageShareBrowse;
import com.atlassian.jira.webtest.selenium.gadgets.Test2DStatsUpgrade;
import com.atlassian.jira.webtest.selenium.gadgets.TestAssignedToMeGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestAverageAgeGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestBugzillaGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestCreatedVsResolvedGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestFavouriteFiltersGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestFilterResultsGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestHeatMapGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestHeatMapGadgetIrrelevantIssues;
import com.atlassian.jira.webtest.selenium.gadgets.TestInProgressGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestIntroGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestLabelsGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestLoginGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestPieChartGadgetIrrelevantIssues;
import com.atlassian.jira.webtest.selenium.gadgets.TestProjectGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestQuicklinksGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestRecentlyCreatedGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestRoadmapGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestStatsGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestStatsGadgetIrrelevantIssues;
import com.atlassian.jira.webtest.selenium.gadgets.TestTextGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestTimeSinceGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestTwoDimensionalStatsGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestVotedIssuesGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestWatchedIssuesGadget;
import com.atlassian.jira.webtest.selenium.gadgets.TestWatchedIssuesGadgetResolvedIssues;
import com.atlassian.jira.webtest.selenium.harness.JiraSeleniumTestSuite;
import com.atlassian.jira.webtest.selenium.issue.TestCommentPermlink;
import com.atlassian.jira.webtest.selenium.issue.TestCommentSecurityLevel;
import com.atlassian.jira.webtest.selenium.issue.TestCommentToggling;
import com.atlassian.jira.webtest.selenium.issue.TestDeleteIssueLink;
import com.atlassian.jira.webtest.selenium.issue.TestEditIssueComponentPicker;
import com.atlassian.jira.webtest.selenium.issue.TestEditIssueVersionPickers;
import com.atlassian.jira.webtest.selenium.issue.TestEditLabelsAnonymous;
import com.atlassian.jira.webtest.selenium.issue.TestHideShowToggleOnViewIssueScreen;
import com.atlassian.jira.webtest.selenium.issue.TestIssuePickerInteractions;
import com.atlassian.jira.webtest.selenium.issue.TestIssuePickerPopup;
import com.atlassian.jira.webtest.selenium.issue.TestKeyboardShortcutHintsInIssueNavigator;
import com.atlassian.jira.webtest.selenium.issue.TestManageAttachments;
import com.atlassian.jira.webtest.selenium.issue.TestOpsBar;
import com.atlassian.jira.webtest.selenium.issue.TestViewIssue;
import com.atlassian.jira.webtest.selenium.issue.TestVotingAndWatching;
import com.atlassian.jira.webtest.selenium.issue.dialogs.TestAttachFile;
import com.atlassian.jira.webtest.selenium.issue.dialogs.TestDeleteIssue;
import com.atlassian.jira.webtest.selenium.issue.dialogs.TestDialogInteractions;
import com.atlassian.jira.webtest.selenium.issue.dialogs.TestWorkflowTransition;
import com.atlassian.jira.webtest.selenium.issue.subtask.TestSubTaskQuickCreation;
import com.atlassian.jira.webtest.selenium.issue.timetracking.TestIssueAggregateTimeTracking;
import com.atlassian.jira.webtest.selenium.issue.timetracking.TestWorklogAndTimeTrackingToggle;
import com.atlassian.jira.webtest.selenium.jql.TestJqlAutoComplete;
import com.atlassian.jira.webtest.selenium.jql.TestJqlAutoCompleteManipulatesJiraData;
import com.atlassian.jira.webtest.selenium.jql.TestJqlDecimalValuesComplete;
import com.atlassian.jira.webtest.selenium.jql.TestJqlQueryTextBox;
import com.atlassian.jira.webtest.selenium.jql.TestSearchProviderCorrectness;
import com.atlassian.jira.webtest.selenium.jquery.TestJQueryLocatorStrategyInSelenium;
import com.atlassian.jira.webtest.selenium.keyboardcommands.TestKeyboardUpCommandToViewIssue;
import com.atlassian.jira.webtest.selenium.mention.TestMentionAutocomplete;
import com.atlassian.jira.webtest.selenium.menu.TestIssuesMenu;
import com.atlassian.jira.webtest.selenium.menu.TestUserMenu;
import com.atlassian.jira.webtest.selenium.misc.Test500Page;
import com.atlassian.jira.webtest.selenium.navigator.TestBulkEdit;
import com.atlassian.jira.webtest.selenium.navigator.TestChartsView;
import com.atlassian.jira.webtest.selenium.navigator.TestIssueNavigatorCaretAndFocus;
import com.atlassian.jira.webtest.selenium.navigator.TestIssueNavigatorCollapsing;
import com.atlassian.jira.webtest.selenium.navigator.TestIssueNavigatorKeyboardNavigation;
import com.atlassian.jira.webtest.selenium.navigator.TestSearchFocus;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.TestGadgetModuleTypeEnabling;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.TestKeyboardShortcutModuleTypeEnabling;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.TestProjectTabPanelModuleTypeEnabling;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.TestWebResourceModuleTypeEnabling;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems.TestWebSectionsAndItemsEnablingOnTheIssueOperationsBar;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems.TestWebSectionsAndItemsEnablingOnTheTopNavigationBar;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems.TestWebSectionsAndItemsEnablingOnTheUserAvatarHoverMenu;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems.TestWebSectionsAndItemsEnablingOnTheUserProfileDropDown;
import com.atlassian.jira.webtest.selenium.plugin.reloadable.webitems.TestWebSectionsAndItemsOnTheAdministrationPagePanelEnabling;
import com.atlassian.jira.webtest.selenium.popupuserpicker.TestPopupGroupPicker;
import com.atlassian.jira.webtest.selenium.popupuserpicker.TestPopupUserPicker;
import com.atlassian.jira.webtest.selenium.project.TestBrowseProjects;
import com.atlassian.jira.webtest.selenium.project.TestIssuesProjectTabPanel;
import com.atlassian.jira.webtest.selenium.renderers.TestPluggableRendererComponents;
import com.atlassian.jira.webtest.selenium.renderers.TestPluggableRendererComponentsNoIE;
import com.atlassian.jira.webtest.selenium.setup.TestSetup;
import com.atlassian.jira.webtest.selenium.setup.TestSetupDatabase;
import com.atlassian.jira.webtest.selenium.user.TestUserProfileControls;
import com.atlassian.jira.webtest.selenium.visualregression.TestVisualRegressionSmoke;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SeleniumConfiguration;
import com.atlassian.selenium.SeleniumTest;
import com.atlassian.selenium.SkipInBrowserUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SeleniumAcceptanceTestHarness extends FuncTestSuite
{
    public static final Logger logger = Logger.getLogger(SeleniumAcceptanceTestHarness.class);

    public static final String SELENIUM_PROPERTY_LOCATION = "jira.functest.seleniumproperties";
    private static final String DEFAULT_SELENIUMTEST_PROPERTIES = "seleniumtest.properties";
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new SeleniumAcceptanceTestHarness();

    private static boolean runOnlyQuarantinedTests;

    public static Test suite()
    {
        final LocalTestEnvironmentData environmentData = getLocalTestEnvironmentData();
        final TestSuite suite = new JiraSeleniumTestSuite(environmentData);
        final Set<Class<? extends TestCase>> tests = SUITE.getTests(environmentData);

        for (final Class<? extends TestCase> test : tests)
        {
            suite.addTestSuite(test);
        }
        TestInformationKit.startTestSuite(suite.countTestCases());
        return suite;
    }

    /**
     * ------------- This is where we should put in all the unit test classes that need to be tested ---------------
     */
    private SeleniumAcceptanceTestHarness()
    {

        runOnlyQuarantinedTests = Boolean.valueOf(System.getProperty("atlassian.test.run.only.quarantined"));

        // runs before JIRA is setup since we can't have a working database
        addTest(TestSetupDatabase.class);
        addTest(TestJQueryLocatorStrategyInSelenium.class);

        // TestSetup tests the Setup page, and therefore must be run before JIRA is set up.
        addTest(TestSetup.class);
        addTest(Test500Page.class);
        addTest(TestCalendarPopUp.class);
        addTest(TestPluggableRendererComponents.class);
        addTest(TestPluggableRendererComponentsNoIE.class);
        addTest(TestProjectImportSelectProject.class);
        addTest(TestXmlImport.class);
        addTest(TestMultiSelectCustomField.class);

        //USER/ISSUE Pickers
        addTest(TestPopupUserPicker.class);

        addTest(TestEditLabelsAnonymous.class);
        addTest(TestIssuePickerInteractions.class);

        addTest(TestCommentDialog.class);
        addTest(TestMultiSelectInteractions.class);
        addTest(TestComponentsField.class);
        addTest(TestVersionsField.class);
        addTest(TestNavigatorUserPicker.class);
        addTest(TestEmailOptionsUserPicker.class);
        addTest(TestNotificationsUserPicker.class);
        addTest(TestIssueLevelSecurityUserPicker.class);
        addTest(TestPermissionsUserPicker.class);
        addTest(TestAdminSectionUserPicker.class);
        addTest(TestAssigneeFieldNoContext.class);

        addTest(TestIssueAggregateTimeTracking.class);
        addTest(TestWorklogAndTimeTrackingToggle.class);
        addTest(TestCommentPermlink.class);
        addTest(TestSubTaskQuickCreation.class);
        addTest(TestIssueNavigatorFavourites.class);
        addTest(TestBulkEditGroupMembers.class);
        addTest(TestBrowseProjectNavigation.class);
        addTest(TestBrowseProjectControls.class);
        addTest(TestChangeLog.class);
        addTest(TestBrowseProjects.class);
        addTest(TestIssuesProjectTabPanel.class);
        addTest(TestManageShareBrowse.class);
        addTest(TestHideShowToggleOnViewIssueScreen.class);
        addTest(TestBulkEdit.class);
        addTest(TestIssueNavigatorKeyboardNavigation.class);
        addTest(TestSearchFocus.class);
        addTest(TestIssueNavigatorCaretAndFocus.class);

        addTest(TestDeleteIssueLink.class);
        addTest(TestDialogInteractions.class);

        // View Issue
        addTest(TestOpsBar.class);
        addTest(TestViewIssue.class);
        addTest(TestAttachFile.class);
        addTest(TestManageAttachments.class);
        addTest(TestDeleteIssue.class);
        addTest(TestEditLabelsInViewIssue.class);
        addTest(TestEditLabelsInIssueNavigator.class);
        addTest(TestCommentToggling.class);
        addTest(TestWorkflowTransition.class);

        // HEADER
        addTest(TestUserMenu.class);
        addTest(TestIssuesMenu.class);

        // User
        addTest(TestUserProfileControls.class);

        // FILTERS
        addTest(TestEditFilter.class);
        addTest(TestFilterBrowser.class);
        addTest(TestFilterPopUp.class);
        addTest(TestFilterOrProjectPicker.class);
        addTest(TestManageFiltersControls.class);

        //DASHBOARD
        addTest(TestDashboardShared.class);
        addTest(TestDashboard.class);
        addTest(TestShareDashboardFavourites.class);
        addTest(TestManageDashboardsPermissionsCollapsor.class);
        addTest(TestAddPortalPage.class);
        addTest(TestEditPortalPage.class);
        addTest(TestDashboardEditing.class);
        addTest(TestDashboardGeneral.class);
        addTest(TestDashboardPermissions.class);
        addTest(TestGadgetPermissions.class);
        addTest(TestManageDashboardsControls.class);
        addTest(TestDashboardMessages.class);

        // Navigator
        addTest(TestIssueNavigatorCollapsing.class);

        //JQL Autocomplete
        addTest(TestJqlQueryTextBox.class);
        addTest(TestJqlAutoCompleteManipulatesJiraData.class);
        addTest(TestJqlAutoComplete.class);
        addTest(TestJqlDecimalValuesComplete.class);

        // GADGETS
        addTest(TestLoginGadget.class);
        addTest(TestIntroGadget.class);
        addTest(TestQuicklinksGadget.class);
        addTest(TestTimeSinceGadget.class);
        addTest(TestFilterResultsGadget.class);
        addTest(TestInProgressGadget.class);
        addTest(TestWatchedIssuesGadget.class);
        addTest(TestTwoDimensionalStatsGadget.class);
        addTest(TestRoadmapGadget.class);
        addTest(TestProjectGadget.class);
        addTest(TestVotedIssuesGadget.class);
        addTest(TestAssignedToMeGadget.class);
        addTest(TestRecentlyCreatedGadget.class);
        addTest(TestFavouriteFiltersGadget.class);
        addTest(TestCreatedVsResolvedGadget.class);
        addTest(TestAverageAgeGadget.class);
        addTest(TestStatsGadget.class);
        addTest(TestPieChartGadgetIrrelevantIssues.class);
        addTest(TestHeatMapGadget.class);
        addTest(TestHeatMapGadgetIrrelevantIssues.class);
        addTest(TestBugzillaGadget.class);
        addTest(TestTextGadget.class);
        addTest(TestLabelsGadget.class);

        addTest(Test2DStatsUpgrade.class);

        addTest(TestChartsView.class);
        addTest(TestManageFiltersFavourites.class); //TODO: The new header tests cover most of this.  Still needed?

        addTest(TestActivityStream.class);
        addTest(TestLookAndFeel.class);

        addTest(TestKeyboardShortcutHintsInIssueNavigator.class);

        addTest(TestKeyboardUpCommandToViewIssue.class);
        addTest(TestVotingAndWatching.class);

        addTest(TestDeletePortalPage.class);
        addTest(TestDeleteFilterDialog.class);
        addTest(TestStatsGadgetIrrelevantIssues.class);
        addTest(TestWatchedIssuesGadgetResolvedIssues.class);
        addTest(TestSearchProviderCorrectness.class);
        addTest(TestPopupGroupPicker.class);
        addTest(TestCustomFieldsInDialogs.class);
        addTest(TestEditIssueVersionPickers.class);

        addTest(TestCommentSecurityLevel.class);
        addTest(TestIssuePickerPopup.class);
        addTest(TestEditIssueComponentPicker.class);
        addTest(TestIssueUserPicker.class);

        addTest(TestFrotherControlRenderers.class);

        addTest(TestAppLinksAdministration.class);

        // Reloadable Plugins
        addTest(TestGadgetModuleTypeEnabling.class);
        addTest(TestWebSectionsAndItemsOnTheAdministrationPagePanelEnabling.class);
        addTest(TestWebSectionsAndItemsEnablingOnTheUserProfileDropDown.class);
        addTest(TestWebSectionsAndItemsEnablingOnTheUserAvatarHoverMenu.class);
        addTest(TestWebSectionsAndItemsEnablingOnTheIssueOperationsBar.class);
        addTest(TestWebSectionsAndItemsEnablingOnTheTopNavigationBar.class);
        addTest(TestKeyboardShortcutModuleTypeEnabling.class);
        addTest(TestWebResourceModuleTypeEnabling.class);
        addTest(TestProjectTabPanelModuleTypeEnabling.class);

        // WebSudo
        addTest(TestGadgetWhitelistUpgrade.class);
        addSingleRunTest(TestVisualRegressionSmoke.class);

        // Project Config
        addTest(TestTranslation.class);

        // Mentions
        addTest(TestMentionAutocomplete.class);
    }

    public static boolean shouldAddTest (Class<? extends TestCase> testClass,Browser currentBrowser)
    {
        boolean shouldAddTest = false;

        if (runOnlyQuarantinedTests)
        {
            if (testClass.getAnnotation(Quarantine.class) != null)
            {
                shouldAddTest = true;
            }
            else
            {
                logger.info("Skipping test " + testClass + ". Only running quarantined tests.");
            }
        }
        else
        {
            if(SkipInBrowserUtil.skip(currentBrowser , testClass))
            {
                logger.info("Skipping test " + testClass + ". This test is configured not to run in the current browser " + currentBrowser + ".");
            }
            else if (testClass.getAnnotation(Quarantine.class) != null)
            {
                logger.info("Skipping test " + testClass + ". Not running quarantined tests.");
            }
            else
            {
                shouldAddTest = true;
            }
        }

        return shouldAddTest;
    }

    public FuncTestSuite addTest(Class<? extends TestCase> testClass)
    {
        // we don't need freakin XML location just to read the browser string
        SeleniumConfiguration config = new JiraSeleniumConfiguration(null);
        Browser currentBrowser = Browser.typeOf(config.getBrowserStartString());

        if (shouldAddTest (testClass, currentBrowser))
        {
            super.addTest(testClass);
        }

        return this;
    }

    /**
     * @return The default setup of the LocalTestEnvironmentData
     */
    public static LocalTestEnvironmentData getLocalTestEnvironmentData()
    {
        //this is a bit of a hack to pick up the xml data location from the seleniumtest.properties file.
        return new LocalTestEnvironmentData(getSeleniumProperties().getProperty("jira.xml.data.location"));
    }

    private static Properties getSeleniumProperties()
    {
        return LocalTestEnvironmentData.loadProperties(SELENIUM_PROPERTY_LOCATION, DEFAULT_SELENIUMTEST_PROPERTIES);
    }

    @Override
    protected TestSuiteBuilder createFuncTestBuilder()
    {
        return super.createFuncTestBuilder().watch(10, 1, TimeUnit.MINUTES, SeleniumTest.class);
    }
}
