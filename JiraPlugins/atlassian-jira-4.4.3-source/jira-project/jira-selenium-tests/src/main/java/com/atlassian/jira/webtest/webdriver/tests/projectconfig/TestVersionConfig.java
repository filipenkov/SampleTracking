package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.WindowSession;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.versions.SummaryPanelVersion;
import com.atlassian.jira.pageobjects.project.summary.versions.VersionSummaryPanel;
import com.atlassian.jira.pageobjects.project.versions.EditVersionForm;
import com.atlassian.jira.pageobjects.project.versions.MergeDialog;
import com.atlassian.jira.pageobjects.project.versions.ReleaseVersionDialog;
import com.atlassian.jira.pageobjects.project.versions.Version;
import com.atlassian.jira.pageobjects.project.versions.VersionPageTab;
import com.atlassian.jira.pageobjects.project.versions.operations.DeleteOperation;
import com.atlassian.jira.pageobjects.project.versions.operations.VersionOperationDropdown;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.collect.Lists;
import org.junit.Test;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */

@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore ("xml/versionspanel.xml")
public class TestVersionConfig extends BaseJiraWebTest
{
    private static final String EXPECTED_OPERATION = "Expected [%s] to have [%s]";
    private static final String EXPECTED_NO_OPERATION = "Expected [%s] NOT to have [%s]";
    private static final String EXPECTED_FIELD_POPULATED = "Expected [%s] field to still be populated";
    private static final String NEW_VERSION_1 = "New Version 1";
    private static final String NEW_VERSION_5 = "New Version 5";
    private static final String NEW_VERSION_4 = "New Version 4";
    private static final String EDIT_OPERATION = "Edit Operation";
    private static final String ARCHIVE_OPERATION = "Archive Operation";
    private static final String RELEASE_OPERATION = "Release Operation";
    private static final String DELETE_OPERATION = "Delete Operation";
    private static final String UNARCHIVE_OPERATION = "Unarchive Operation";
    private static final String UNRELEASE_OPERATION = "Unrelease Operation";
    private static final String THAT_NAME_IS_ALREADY_USED = "A version with this name already exists in this project.";
    private static final String EXPECTED_ERROR_S = "Expected error [%s]";
    private static final String INVALID_DATE = "Please enter the date in the following format: d/MMM/yy";
    private static final String NEW_VERSION_6 = "New Version 6";
    private static final String DATE_VAL = "23/Mar/50";
    private static final String A_NEW_VERSION = "A new version!";
    private static final String BLAH = "blah";
    private static final String HSP = "HSP";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String RELEASE_DATE = "release date";

    private static final String NO_NAME_ERROR = "You must specify a valid version name";
    private static final String VERSION_NAME_TOO_LONG = "Description is too long. Please enter a description shorter than 255 characters.";
    private static final String VERSION_1 = "version 1";
    private static final String VERSION_2 = "version 2";
    private static final String VERSION_3 = "version 3";
    private static final String XSS = "XSS";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMM/yy");

    @Inject
    private AtlassianWebDriver driver;

    @Test
    public void testTabNavigation()
    {
        ProjectSummaryPageTab config = jira.gotoLoginPage().loginAsSysAdmin(ProjectSummaryPageTab.class, HSP);
        assertTrue(config.getTabs().isSummaryTabSelected());

        VersionPageTab versionPageTab = config.getTabs().gotoVersionsTab();
        assertTrue(versionPageTab.getTabs().isVersionsTabSelected());
        assertEquals(HSP, versionPageTab.getProjectKey());
    }

    @Test
    public void testDisplayLogic()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);

        List<Version> versions = hspVersionPage.getVersions();

        // test order
        assertTrue(versions.get(0).getName().equals(NEW_VERSION_5));
        assertTrue(versions.get(1).getName().equals(NEW_VERSION_4));
        assertTrue(versions.get(2).getName().equals(NEW_VERSION_1));

        final Version newVersion5 = versions.get(0);
        final VersionOperationDropdown newVersion5Operations = newVersion5.openOperationsCog();

        // unreleased, unarchived

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_5, ARCHIVE_OPERATION),
                newVersion5Operations.hasOperation("Archive"));

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_5, RELEASE_OPERATION),
                newVersion5Operations.hasOperation("Release"));

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_5, DELETE_OPERATION),
                newVersion5Operations.hasOperation("Delete"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_5, UNARCHIVE_OPERATION),
                newVersion5Operations.hasOperation("Unarchive"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_5, UNRELEASE_OPERATION),
                newVersion5Operations.hasOperation("Unrelease"));

        assertTrue("Expected " + NEW_VERSION_5 + " to be overdue", newVersion5.isOverdue());

        newVersion5Operations.close();

        // archived

        final Version newVersion4 = versions.get(1);
        final VersionOperationDropdown newVersion4Operations = newVersion4.openOperationsCog();

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_4, UNARCHIVE_OPERATION),
                newVersion4Operations.hasOperation("Unarchive"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_4, EDIT_OPERATION),
                newVersion4Operations.hasOperation("Edit Details"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_4, ARCHIVE_OPERATION),
                newVersion4Operations.hasOperation("Archive"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_4, DELETE_OPERATION),
                newVersion4Operations.hasOperation("Delete"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_4, RELEASE_OPERATION),
                newVersion4Operations.hasOperation("Release"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_4, UNRELEASE_OPERATION),
                newVersion4Operations.hasOperation("Unrelease"));

        newVersion4Operations.close();

        // released, unarchived

        final Version newVersion1 = versions.get(2);
        final VersionOperationDropdown newVersion1Operations = newVersion1.openOperationsCog();

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_1, ARCHIVE_OPERATION),
                newVersion1Operations.hasOperation("Archive"));

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_1, UNRELEASE_OPERATION),
                newVersion1Operations.hasOperation("Unrelease"));

        assertTrue(String.format(EXPECTED_OPERATION, NEW_VERSION_1, DELETE_OPERATION),
                newVersion1Operations.hasOperation("Delete"));

        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_1, UNARCHIVE_OPERATION),
                newVersion1Operations.hasOperation("Unarchive"));
        
        assertFalse(String.format(EXPECTED_NO_OPERATION, NEW_VERSION_1, RELEASE_OPERATION),
                newVersion1Operations.hasOperation("Release"));

        newVersion1Operations.close();
    }


    @Test
    public void testCreateVersion() throws InterruptedException
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);

        EditVersionForm createVersionForm = hspVersionPage.getEditVersionForm()
                .fill(NEW_VERSION_5, A_NEW_VERSION, BLAH)
                .submit();

        final EditVersionForm.Field nameField = createVersionForm.getNameField();
        final EditVersionForm.Field descriptionField = createVersionForm.getDescriptionField();
        final EditVersionForm.Field releasedDateField = createVersionForm.getReleaseDateField();

        // fields should still be populated
        assertTrue(String.format(EXPECTED_FIELD_POPULATED, NAME),
                nameField.value().equals(NEW_VERSION_5));

        assertTrue(String.format(EXPECTED_FIELD_POPULATED, DESCRIPTION),
                descriptionField.value().equals(A_NEW_VERSION));

        assertTrue(String.format(EXPECTED_FIELD_POPULATED, RELEASE_DATE),
                releasedDateField.value().equals(BLAH));

        // errors

        assertTrue(String.format(EXPECTED_ERROR_S, THAT_NAME_IS_ALREADY_USED),
                nameField.getError().equals(THAT_NAME_IS_ALREADY_USED));

        assertTrue(String.format(EXPECTED_ERROR_S, INVALID_DATE),
                releasedDateField.getError().equals(INVALID_DATE));

        String longText = "";

        while (longText.length() < 256)
        {
            longText += "a";
        }

        EditVersionForm versionNameTooLongForm = hspVersionPage.getEditVersionForm()
                .fill(longText, "", null)
                .submit();

        assertTrue(String.format(EXPECTED_ERROR_S, VERSION_NAME_TOO_LONG),
                versionNameTooLongForm.getNameField().getError().equals(VERSION_NAME_TOO_LONG));


        EditVersionForm noVersionNameForm = hspVersionPage.getEditVersionForm()
                .fill("", "", BLAH)
                .submit();


        assertTrue(String.format(EXPECTED_ERROR_S, NO_NAME_ERROR),
                noVersionNameForm.getNameField().getError().equals(NO_NAME_ERROR));


        hspVersionPage.getEditVersionForm()
                .fill(NEW_VERSION_6, A_NEW_VERSION, DATE_VAL)
                .submit();


        List<Version> versions = hspVersionPage.getVersions();

        assertEquals(new SummaryPanelVersion(NEW_VERSION_6).setDescription(A_NEW_VERSION).setReleaseDate(DATE_VAL).setOverdue(true),
                new SummaryPanelVersion(versions.get(0)));
    }

    @Test
    public void testArchiveVersion() throws InterruptedException
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        assertTrue(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_5));

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        final Version versionToArchive = versions.get(0);

        assertTrue(versionToArchive.getName().equals(NEW_VERSION_5));
        assertFalse(versionToArchive.isArchived());

        VersionOperationDropdown versionOperationDropdown = versionToArchive.openOperationsCog();
        assertTrue(versionOperationDropdown.hasOperation("Archive"));

        versionOperationDropdown.click("Archive");

        waitUntilTrue(versionToArchive.hasFinishedVersionOperation());
        assertTrue(versionToArchive.isArchived());

        assertFalse(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_5));
    }

    @Test
    public void testArchiveVersionWithServerError() throws InterruptedException
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        assertTrue(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_5));

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        final Version versionToArchive = versions.get(0);

        assertTrue(versionToArchive.getName().equals(NEW_VERSION_5));
        assertFalse(versionToArchive.isArchived());

        driver.manage().deleteAllCookies();

        VersionOperationDropdown versionOperationDropdown = versionToArchive.openOperationsCog();
        assertTrue(versionOperationDropdown.hasOperation("Archive"));

        versionOperationDropdown.click("Archive");

        waitUntilTrue(versionToArchive.hasFinishedVersionOperation());
        assertFalse(versionToArchive.isArchived());
        assertTrue("Expected a login prompt as the user is logged out", hspVersionPage.getServerError().contains("log in"));


        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        assertTrue(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_5));
    }

    @Test
    public void testUnArchiveVersion() throws InterruptedException
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        assertFalse(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_4));

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        final Version versionToUnarchive = versions.get(1);

        assertTrue(versionToUnarchive.getName().equals(NEW_VERSION_4));
        assertTrue(versionToUnarchive.isArchived());

        VersionOperationDropdown versionOperationDropdown = versionToUnarchive.openOperationsCog();
        assertTrue(versionOperationDropdown.hasOperation("Unarchive"));

        versionOperationDropdown.click("Unarchive");

        waitUntilTrue(versionToUnarchive.hasFinishedVersionOperation());
        assertFalse(versionToUnarchive.isArchived());

        assertTrue(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_4));
    }

    @Test
    public void testUnArchiveVersionWithServerError() throws InterruptedException
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        assertFalse(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_4));

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        final Version versionToUnarchive = versions.get(1);

        assertTrue(versionToUnarchive.getName().equals(NEW_VERSION_4));
        assertTrue(versionToUnarchive.isArchived());

        driver.manage().deleteAllCookies();

        VersionOperationDropdown versionOperationDropdown = versionToUnarchive.openOperationsCog();
        assertTrue(versionOperationDropdown.hasOperation("Unarchive"));

        versionOperationDropdown.click("Unarchive");

        waitUntilTrue(versionToUnarchive.hasFinishedVersionOperation());
        assertTrue(versionToUnarchive.isArchived());
        assertTrue("Expected a login prompt as the user is logged out", hspVersionPage.getServerError().contains("log in"));

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        assertFalse(navigateToSummaryPageFor(HSP).openPanel(VersionSummaryPanel.class)
                .hasVersion(NEW_VERSION_4));
    }

    @Test
    public void testCreateVersionServerErrorHandling() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);

        driver.manage().deleteAllCookies();

        pageBinder.bind(VersionPageTab.class, HSP).getEditVersionForm()
                .fill(NEW_VERSION_5, A_NEW_VERSION, BLAH)
                .submit();

        assertTrue("Expected a login prompt as the user is logged out", hspVersionPage.getServerError().contains("log in"));
    }


    @Test
    public void testEditVersion() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        versions.get(0)
                .edit("name")
                .fill("Scott", "Scott's Version", "16/Apr/11")
                .submit();

        Version version = hspVersionPage.getVersions().get(0);

        assertEquals("Scott", version.getName());
        assertEquals("Scott's Version", version.getDescription());

        try
        {
            assertEquals(DATE_FORMAT.parse("16/Apr/11"), version.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        EditVersionForm editForm = version.edit("name")
                .fill("Scott", "Scott's Version", "blah");

        assertNotNull(editForm
                .submit()
                .getReleaseDateField()
                .getError());

        editForm.cancel();

    }

    @Test
    public void testReleaseVersionWithNoUnresolvedIssues() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);
        List<Version> versions = hspVersionPage.getVersions();

        Version versionToRelease = versions.get(0);
        assertFalse(versionToRelease.isReleased());
        try
        {
            assertEquals(DATE_FORMAT.parse("16/Feb/11"), versionToRelease.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);
        assertFalse(releaseVersionDialog.hasUnresolvedIssues());
        assertFalse(releaseVersionDialog.hasIgnoreOption());
        assertFalse(releaseVersionDialog.hasMoveOption());
        assertFalse(releaseVersionDialog.hasUnresolvedMessage());

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());

        hspVersionPage = pageBinder.bind(VersionPageTab.class, HSP);

        Version version = hspVersionPage.getVersions().get(0);

        assertTrue(version.isReleased());


        try
        {
            assertEquals(DATE_FORMAT.parse("23/Apr/11"), version.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

    }

    @Test
    @Restore("xml/TestVersionsRelease.xml")
    public void testReleaseVersionWithUnresolvedIssuesBuNoOtherUnreleasedVersions() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab xssVersionPage = navigateToVersionsPageFor(XSS);

        Version versionToRelease = xssVersionPage.getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);

        assertTrue(releaseVersionDialog.hasUnresolvedIssues());
        assertTrue(releaseVersionDialog.hasIgnoreOption());
        assertFalse(releaseVersionDialog.hasMoveOption());

        assertEquals(addContextPath("/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10010&fixfor=10010&resolution=-1"),
                releaseVersionDialog.unresolvedIssueLinkUrl());
        assertEquals("Ignore and proceed with release - there are no other unreleased fix versions available.",
                releaseVersionDialog.getIgnoreOptionLabelText());
        assertEquals("There are still 1 unresolved issue(s) for this version.",
                releaseVersionDialog.getUnresolvedMessage());

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());

        Version version = xssVersionPage.getVersionByName(VERSION_1);

        assertTrue(version.isReleased());


        try
        {
            assertEquals(DATE_FORMAT.parse("23/Apr/11"), version.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        // Go to issue navigator, ensure there are still open issues for this version
        int remainingOpenIssues = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_1 + "\" AND resolution=unresolved")
                .submit()
                .getResults()
                .getTotalCount();
        assertEquals(1, remainingOpenIssues);

    }

    @Test
    @Restore("xml/TestVersionsRelease.xml")
    public void testReleaseVersionWithUnresolvedIssuesAndIgnore() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab xssVersionPage = navigateToVersionsPageFor(XSS);

        xssVersionPage.getEditVersionForm().fill(VERSION_2, "", null).submit();

        Version createdVersion = xssVersionPage.getVersionByName(VERSION_2);
        assertFalse(createdVersion.isReleased());

        Version versionToRelease = xssVersionPage.getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);
        assertTrue(releaseVersionDialog.hasUnresolvedIssues());
        assertTrue(releaseVersionDialog.hasIgnoreOption());
        assertTrue(releaseVersionDialog.hasMoveOption());

        assertEquals(addContextPath("/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10010&fixfor=10010&resolution=-1"),
                releaseVersionDialog.unresolvedIssueLinkUrl());
        assertEquals("Ignore and proceed with release", releaseVersionDialog.getIgnoreOptionLabelText());
        assertEquals("There are still 1 unresolved issue(s) for this version.",
                releaseVersionDialog.getUnresolvedMessage());

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .ignoreUnresolvedIssues()
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());

        versionToRelease = xssVersionPage.getVersionByName(VERSION_1);

        assertTrue(versionToRelease.isReleased());


        try
        {
            assertEquals(DATE_FORMAT.parse("23/Apr/11"), versionToRelease.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        // Go to issue navigator, ensure there are still open issues for this version
        int remainingOpenIssues = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_1 + "\" AND resolution=unresolved")
                .submit()
                .getResults()
                .getTotalCount();
        assertEquals(1, remainingOpenIssues);

    }

    @Test
    @Restore("xml/TestVersionsRelease.xml")
    public void testReleaseVersionAndServerError() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab xssVersionPage = navigateToVersionsPageFor(XSS);

        Version versionToRelease = xssVersionPage.getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);

        assertTrue(releaseVersionDialog.hasUnresolvedIssues());
        assertTrue(releaseVersionDialog.hasIgnoreOption());
        assertFalse(releaseVersionDialog.hasMoveOption());

        assertEquals(addContextPath("/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10010&fixfor=10010&resolution=-1"),
                releaseVersionDialog.unresolvedIssueLinkUrl());
        assertEquals("Ignore and proceed with release - there are no other unreleased fix versions available.",
                releaseVersionDialog.getIgnoreOptionLabelText());
        assertEquals("There are still 1 unresolved issue(s) for this version.",
                releaseVersionDialog.getUnresolvedMessage());

        driver.manage().deleteAllCookies();

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .ignoreUnresolvedIssues()
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());
        assertEquals("You are not authorised to perform this operation. Please try to log in or sign up for an account.",
                xssVersionPage.getServerError());

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        versionToRelease = navigateToVersionsPageFor(XSS)
                .getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());
    }

    @Test
    @Restore("xml/TestVersionsRelease.xml")
    public void testReleaseVersionWithUnresolvedIssuesAndMove() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab xssVersionPage = navigateToVersionsPageFor(XSS);

        xssVersionPage.getEditVersionForm().fill(VERSION_2, "", null).submit();

        Version createdVersion = xssVersionPage.getVersionByName(VERSION_2);
        assertFalse(createdVersion.isReleased());

        Version versionToRelease = xssVersionPage.getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);
        assertTrue(releaseVersionDialog.hasUnresolvedIssues());
        assertTrue(releaseVersionDialog.hasIgnoreOption());
        assertTrue(releaseVersionDialog.hasMoveOption());

        assertEquals(addContextPath("/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10010&fixfor=10010&resolution=-1"),
                releaseVersionDialog.unresolvedIssueLinkUrl());
        assertEquals("Ignore and proceed with release", releaseVersionDialog.getIgnoreOptionLabelText());
        assertEquals("There are still 1 unresolved issue(s) for this version.",
                releaseVersionDialog.getUnresolvedMessage());

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .moveUnresolvedIssues(VERSION_2)
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());

        versionToRelease = xssVersionPage.getVersionByName(VERSION_1);

        assertTrue(versionToRelease.isReleased());

        try
        {
            assertEquals(DATE_FORMAT.parse("23/Apr/11"), versionToRelease.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        // Go to issue navigator, ensure there are no open issues for this version
        // ensure there are open issues in some other version
        assertEquals("No matching issues found.", pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_1 + "\" AND resolution=unresolved")
                .submit()
                .getJQLInfo());

        int remainingOpenIssues = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_2 + "\" AND resolution=unresolved")
                .submit()
                .getResults()
                .getTotalCount();
        assertEquals(1, remainingOpenIssues);


    }

    @Test
    @Restore("xml/TestVersionsRelease.xml")
    public void testReleaseVersionWithInvalidDate() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab xssVersionPage = navigateToVersionsPageFor(XSS);

        xssVersionPage.getEditVersionForm().fill(VERSION_2, "", null).submit();

        Version createdVersion = xssVersionPage.getVersionByName(VERSION_2);
        assertFalse(createdVersion.isReleased());

        Version versionToRelease = xssVersionPage.getVersionByName(VERSION_1);
        assertFalse(versionToRelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToRelease
                .openOperationsCog();

        assertTrue(versionOperationDropdown.hasOperation("Release"));
        versionOperationDropdown.click("Release");

        ReleaseVersionDialog releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);
        assertTrue(releaseVersionDialog.hasUnresolvedIssues());
        assertTrue(releaseVersionDialog.hasIgnoreOption());
        assertTrue(releaseVersionDialog.hasMoveOption());

        assertEquals(addContextPath("/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10010&fixfor=10010&resolution=-1"),
                releaseVersionDialog.unresolvedIssueLinkUrl());
        assertEquals("Ignore and proceed with release", releaseVersionDialog.getIgnoreOptionLabelText());
        assertEquals("There are still 1 unresolved issue(s) for this version.",
                releaseVersionDialog.getUnresolvedMessage());

        releaseVersionDialog
                .setReleaseDate(BLAH)
                .submit();

        releaseVersionDialog = pageBinder.bind(ReleaseVersionDialog.class);
        assertTrue(releaseVersionDialog.hasReleaseDateErrorMessage());

        releaseVersionDialog
                .setReleaseDate("23/Apr/11")
                .submit();

        waitUntilTrue(releaseVersionDialog.isClosed());

        versionToRelease = xssVersionPage.getVersionByName(VERSION_1);

        assertTrue(versionToRelease.isReleased());

        try
        {
            assertEquals(DATE_FORMAT.parse("23/Apr/11"), versionToRelease.getReleaseDate());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        // Go to issue navigator, ensure there are still open issues for this version
        int remainingOpenIssues = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_1 + "\" AND resolution=unresolved")
                .submit()
                .getResults()
                .getTotalCount();
        assertEquals(1, remainingOpenIssues);
    }

    @Test
    @Restore ("xml/TestVersionsRelease.xml")
    public void testUnreleaseVersion()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        VersionPageTab versionsTab = navigateToVersionsPageFor(HSP);
        List<Version> versions = versionsTab.getVersions();

        Version versionToUnrelease = versions.get(2);
        assertTrue(versionToUnrelease.isReleased());

        VersionOperationDropdown versionOperationDropdown = versionToUnrelease.openOperationsCog();
        assertTrue(versionOperationDropdown.hasOperation("Unrelease"));

        versionOperationDropdown.click("Unrelease");
        waitUntilTrue(versionToUnrelease.hasFinishedVersionOperation());
        assertFalse(versionToUnrelease.isReleased());

        versionOperationDropdown = versionToUnrelease.openOperationsCog();
        assertFalse(versionOperationDropdown.hasOperation("Unrelease"));
    }

    @Test
    @Restore ("xml/DeleteVersion.xml")
    public void testDeleteVersion() throws InterruptedException
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        // New Version 1 (Affects Version(1))

        VersionPageTab hspVersionPage = navigateToVersionsPageFor(HSP);

        DeleteOperation deleteDialog = hspVersionPage.getVersionByName("New Version 1")
                .openOperationsCog()
                .clickDelete();

        Map<String, Boolean> operations = deleteDialog.getOperations();

        assertTrue("Expected [New Version 1] to have swap affects version operation",
                operations.get("swapAffectVersion"));

        assertTrue("Expected [New Version 1] to have remove affects version operation",
                operations.get("swapAffectVersion"));

        assertEquals(1, deleteDialog.getAffectsCount());

        deleteDialog.setAffectsToRemoveVersion();

        assertFalse("Expected [New Version 1] NOT to have swap fix version operation",
                operations.get("swapFixVersion"));

        assertFalse("Expected [New Version 1] NOT to have remove fix version operation",
                operations.get("removeFixVersion"));

        hspVersionPage = deleteDialog.submit();

        assertNull(hspVersionPage.getVersionByName("New Version 1"));

        assertEquals("The value 'New Version 1' does not exist for the field 'affectedVersion'.",
                pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("affectedVersion=\"New Version 1\"")
                .submit()
                .getJQLError());

        hspVersionPage = navigateToVersionsPageFor(HSP);



        // New Version 5 (Affects Version(1), Fix Version(1))

        deleteDialog = hspVersionPage.getVersionByName("New Version 5")
                .openOperationsCog()
                .clickDelete();

        operations = deleteDialog.getOperations();

        assertTrue("Expected [New Version 5] to have swap affects version operation",
                operations.get("swapAffectVersion"));

        assertTrue("Expected [New Version 5] to have remove affects version operation",
                operations.get("swapAffectVersion"));

        assertEquals(1, deleteDialog.getAffectsCount());

        assertTrue("Expected [New Version 5] to have swap fix version operation",
                operations.get("swapFixVersion"));

        assertTrue("Expected [New Version 5] to have remove fix version operation",
                operations.get("removeFixVersion"));

        deleteDialog.setAffectsToSwapVersion("Migrate Version");
        deleteDialog.setFixToSwapVersion("Migrate Version");

        assertEquals(1, deleteDialog.getFixCount());

        hspVersionPage = deleteDialog.submit();

        assertNull(hspVersionPage.getVersionByName("New Version 5"));

        AdvancedSearch advancedSearch = pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"New Version 5\"")
                .submit();

        assertEquals("The value 'New Version 5' does not exist for the field 'fixVersion'.",
                advancedSearch.getJQLError());

        advancedSearch = advancedSearch.enterQuery("fixVersion=\"Migrate Version\"").submit();

        assertEquals(1, advancedSearch.getResults().getTotalCount());

        advancedSearch = advancedSearch.enterQuery("affectedVersion=\"Migrate Version\"").submit();

        assertEquals(1, advancedSearch.getResults().getTotalCount());

        hspVersionPage = navigateToVersionsPageFor(HSP);

        deleteDialog = hspVersionPage.getVersionByName("Another version")
                .openOperationsCog()
                .clickDelete();

        operations = deleteDialog.getOperations();

        // Another version (No Issues related to this version)

        assertFalse("Expected [Another version] NOT to have swap affects version operation",
                operations.get("swapAffectVersion"));

        assertFalse("Expected [Another version] NOT to have remove affects version operation",
                operations.get("swapAffectVersion"));

        assertFalse("Expected [Another version] NOT to have swap fix version operation",
                operations.get("swapFixVersion"));

        assertFalse("Expected [Another version] NOT to have remove fix version operation",
                operations.get("removeFixVersion"));


        assertEquals("There are no issues related to this version. It is safe to delete.",
                deleteDialog.getInfoMessage());

        deleteDialog.submit();

        assertNull(hspVersionPage.getVersionByName("Another version"));
    }

    @Test
    @Restore("xml/TestVersionReordering.xml")
    public void testVersionReordering()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        VersionPageTab versionPage = navigateToVersionsPageFor(XSS);

        // Move a version to bottom of list
        versionPage.moveVersionBelow(versionPage.getVersionByName("version 3"), versionPage.getVersionByName("version 1"));
        List<String> expectedVersions = Lists.newArrayList(
            "version 4",
            "version 2",
            "version 1",
            "version 3"
        );
        compareVersionSortOrder(expectedVersions, versionPage.getVersions());

        // Move a version to top of list
        versionPage.moveVersionAbove(versionPage.getVersionByName("version 2"), versionPage.getVersionByName("version 4"));
        expectedVersions = Lists.newArrayList(
            "version 2",
            "version 4",
            "version 1",
            "version 3"
        );
        compareVersionSortOrder(expectedVersions, versionPage.getVersions());

        // Verify persistence across page loads
        versionPage = navigateToVersionsPageFor(XSS);
        compareVersionSortOrder(expectedVersions, versionPage.getVersions());
    }

    @Test
    @Restore("xml/TestVersionReordering.xml")
    public void testVersionReorderingPermissions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        VersionPageTab versionPage = navigateToVersionsPageFor(XSS);

        driver.manage().deleteAllCookies();

        // Move a version to bottom of list
        versionPage.moveVersionBelow(versionPage.getVersionByName(VERSION_3), versionPage.getVersionByName(VERSION_1));
        assertTrue("Expected a login prompt as the user is logged out", versionPage.getServerError().contains("log in"));
    }

    @Test
    @Restore("xml/TestVersionReordering.xml")
    public void testVersionReorderingErrors()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        VersionPageTab versionPage = navigateToVersionsPageFor(XSS);

        WindowSession windowSession = jira.openWindowSession();
        final WindowSession.BrowserWindow otherVersions =
               windowSession.openNewWindow("otherVersions").switchTo();

        final VersionPageTab otherXssVersionPage = navigateToVersionsPageFor(XSS);
        otherXssVersionPage.getVersionByName(VERSION_3).openOperationsCog()
            .clickDelete()
            .submit();

        assertNull(otherXssVersionPage.getVersionByName(VERSION_3));

        otherVersions.close();
        windowSession.switchToDefault();

        // Move the deleted version.
        versionPage.moveVersionAbove(versionPage.getVersionByName(VERSION_3), versionPage.getVersionByName(VERSION_1));
        assertEquals("Could not find version for id '10111'", versionPage.getServerError());

        versionPage.closeServerErrorDialog();

        // Move a version "after" the deleted version.
        versionPage.moveVersionAbove(versionPage.getVersionByName(VERSION_1), versionPage.getVersionByName(VERSION_3));
        assertEquals("Could not find version with id '10111' for project 'XSS'", versionPage.getServerError());
    }

    @Test
    @Restore("xml/TestVersionMerge.xml")
    public void testVersionMergeWithNoVersions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab versionPage = navigateToVersionsPageFor(XSS);
        final PageElement mergeLink = versionPage.getMergeLink();

        assertTrue(mergeLink.isPresent());
        assertFalse(mergeLink.isVisible());
    }

    @Test
    @Restore("xml/TestVersionMerge.xml")
    public void testVersionMergeWithOneOrMoreVersions()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        // Starts off with:
        // - 1 affectsVersion = version 2,
        // - 1 fixVersion = version 2,
        // - 1 fixVersion = version 2, affectsVersion = version 2,
        // - 1 fixVersion = version 2, version 3, affectsVersion = version 3, version 2


        final VersionPageTab versionPage = navigateToVersionsPageFor(HSP);

        MergeDialog mergeDialog = versionPage.openMergeDialog();

        assertFalse(mergeDialog.hasErrorMessages());
        assertTrue(mergeDialog.hasWarning());
        assertFalse(mergeDialog.hasNoVersions());

        assertEquals("WARNING: You cannot un-merge these versions once they have merged.",
                mergeDialog.getWarningText());

        mergeDialog.merge(VERSION_1, VERSION_2, VERSION_3)
                .submit();

        // Go to issue navigator, ensure the issues tally up
        assertEquals(3, pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_1 + "\"")
                .submit()
                .getResults()
                .getTotalCount());

        assertEquals(3, pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("affectedVersion=\"" + VERSION_1 + "\"")
                .submit()
                .getResults()
                .getTotalCount());

        assertEquals("The value 'version 2' does not exist for the field 'fixVersion'.", pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_2 + "\"")
                .submit()
                .getJQLError());

        assertEquals("The value 'version 2' does not exist for the field 'affectedVersion'.", pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("affectedVersion=\"" + VERSION_2 + "\"")
                .submit()
                .getJQLError());

        assertEquals("The value 'version 3' does not exist for the field 'fixVersion'.", pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("fixVersion=\"" + VERSION_3 + "\"")
                .submit()
                .getJQLError());


        assertEquals("The value 'version 3' does not exist for the field 'affectedVersion'.", pageBinder.navigateToAndBind(AdvancedSearch.class)
                .enterQuery("affectedVersion=\"" + VERSION_3 + "\"")
                .submit()
                .getJQLError());
    }

    @Test
    @Restore("xml/TestVersionMerge.xml")
    public void testVersionMergeWithSingleVersion()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab versionPage = navigateToVersionsPageFor(HSP);

        MergeDialog mergeDialog = versionPage.openMergeDialog();

        mergeDialog
                .merge(VERSION_1, VERSION_1)
                .submit();

        final List<String> expectedErrorMessages = Lists.newArrayList(
                "You cannot move the issues to the version being deleted."
        );

        assertTrue(mergeDialog.hasErrorMessages());
        assertTrue(mergeDialog.hasWarning());

        assertEquals(expectedErrorMessages, mergeDialog.getErrorMessages());
        assertEquals("WARNING: You cannot un-merge these versions once they have merged.",
                mergeDialog.getWarningText());

    }

    @Test
    @Restore("xml/TestVersionMerge.xml")
    public void testVersionMergeWithServerError()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final VersionPageTab versionPage = navigateToVersionsPageFor(HSP);

        MergeDialog mergeDialog = versionPage.openMergeDialog();

        mergeDialog.merge(VERSION_1, VERSION_2);

        driver.manage().deleteAllCookies();

        mergeDialog.submit();

        assertNotNull(versionPage.getVersionByName(VERSION_2));
        assertTrue("Expected a login prompt as the user is logged out",
                versionPage.getServerError().contains("log in"));
    }

    private void compareVersionSortOrder(final List<String> expectedVersionNameOrder, final List<Version> actualVersions)
    {
        assertEquals(expectedVersionNameOrder.size(), actualVersions.size());
        for(int i = 0; i < expectedVersionNameOrder.size(); ++i)
        {
            assertEquals(expectedVersionNameOrder.get(i), actualVersions.get(i).getName());
        }
    }

    private String addContextPath(final String url)
    {
        return jira.getProductInstance().getContextPath() + url;
    }

    private VersionPageTab navigateToVersionsPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(VersionPageTab.class, projectKey);
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    // not working due to WD issues
//    private void openNewWindow(String name)
//    {
//        String id = String.valueOf(name.hashCode());
//        driver.executeScript("jQuery('body').append(\"<a id='" + id + "' href='" + jira.getProductInstance().getBaseUrl()
//                +"' target='" + name + "'>test</a>\");");
//        driver.findElement(By.id(id)).click();
//        driver.executeScript("jQuery('#" + id +"').remove()");
//        driver.switchTo().window(name);
//    }
}
