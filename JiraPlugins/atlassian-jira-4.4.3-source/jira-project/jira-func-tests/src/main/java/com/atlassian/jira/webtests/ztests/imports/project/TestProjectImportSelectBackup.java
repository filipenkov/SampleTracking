package com.atlassian.jira.webtests.ztests.imports.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.File;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT })
public class TestProjectImportSelectBackup extends AbstractProjectImportTestCase
{
    protected void setUpTest()
    {
        this.administration.restoreBlankInstance();
    }

    public void testValidationInvalidPaths()
    {
        this.administration.attachments().enable();
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        tester.clickLinkWithText("Edit Configuration");
        tester.checkCheckbox("attachmentPathOption", "DEFAULT");
        tester.submit("Update");
        String attachmentsPath = administration.getJiraHomeDirectory() + "/data/attachments/";

        this.navigation.gotoAdminSection("project_import");

        // Test no backup file specified
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", "");
        tester.submit("Next");
        tester.assertTextPresent("You must provide a path to the JIRA backup XML file.");

        // Test the backup file specified does not exist
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", "/iamafilethatwillneverexisthahahahaha/bak.xml");
        tester.submit("Next");
        tester.assertTextPresent("The path to the JIRA backup XML file is not valid.");

        // Test the backup file is a directory not a file
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", System.getProperty("java.io.tmpdir"));
        tester.submit("Next");
        tester.assertTextPresent("The path to the JIRA backup XML file is not valid.");
    }

    public void testValidationInvalidbackupAttachmentsNotEnabled()
    {
        this.administration.attachments().disable();

        this.navigation.gotoAdminSection("project_import");

        // Test no backup file specified
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", "");
        tester.submit("Next");
        tester.assertTextPresent("You must provide a path to the JIRA backup XML file.");

        // Test the backup file specified does not exist
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", "/iamafilethatwillneverexisthahahahaha/bak.xml");
        tester.submit("Next");
        tester.assertTextPresent("The path to the JIRA backup XML file is not valid.");

        // Test the backup file is a directory not a file
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", System.getProperty("java.io.tmpdir"));
        tester.submit("Next");
        tester.assertTextPresent("The path to the JIRA backup XML file is not valid.");
    }

    public void testJumpToProgressScreen()
    {
        // First go to the initial page to make sure the session has been cleared.
        this.navigation.gotoAdminSection("project_import");

        tester.gotoPage("/secure/admin/ProjectImportBackupOverviewProgress.jspa");
        tester.assertTextPresent("Can not find any running task information. Perhaps your session has timed out, please restart the project import wizard.");
        tester.assertTextNotPresent("Refresh");
        tester.assertTextPresent("Cancel");
    }

    public void testParseExceptionInJIRAData() throws Exception
    {
        File file = importAndExportBackupAndSetupCurrentInstance("TestProjectImportParseExceptionScreen1.xml", "blankprojects.xml");
        try
        {
            this.navigation.gotoAdminSection("project_import");
            // Get to the project select page
            tester.assertTextPresent("Project Import: Select Backup File");
            tester.setFormElement("backupXmlPath", file.getAbsolutePath());
            tester.submit("Next");

            advanceThroughWaitingPage();
            tester.assertTextPresent("Project Import: Select Backup File");
            text.assertTextPresentHtmlEncoded("There was a problem parsing the backup XML file at " + file.getAbsolutePath() + ": No 'key' field for Issue 10022.");
        }
        finally
        {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }
}