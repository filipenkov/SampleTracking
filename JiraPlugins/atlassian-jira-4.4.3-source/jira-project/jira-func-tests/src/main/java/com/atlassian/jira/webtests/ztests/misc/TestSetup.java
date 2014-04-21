package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.meterware.httpunit.WebTable;

import java.io.File;
import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.LICENSING })
public class TestSetup extends FuncTestCase
{
    /**
     * Try to navigate to all Setup URLs to ensure Setup cannot be run again once it has already been run
     */
    public void testSetupCannotBeRunTwice()
    {
        administration.restoreBlankInstance();
        String[] actions = new String[] {   "SetupDatabase.jspa", "Setup.jspa", "Setup!input.jspa", "Setup!default.jspa",
                                            "Setup2.jspa", "Setup2!default.jspa",
                                            "SetupExisting.jspa", "SetupExisting!default.jspa",
                                            "Setup3.jspa", "Setup3!default.jspa",
                                            "SetupComplete.jspa", "SetupComplete!default.jspa",
                                            "SetupImport.jspa", "SetupImport!default.jspa" };

        String[] views = new String[] { "setup-db.jsp", "setup.jsp", "setup2.jsp", "setup2-existingadmins.jsp", "setup3.jsp", "setup-import.jsp" };

        for (String action : actions)
        {
            tester.gotoPage("/secure/" + action);
            assertSetupAlreadyLong();
        }

        for (String view : views)
        {
            tester.gotoPage("/views/" + view);
            assertSetupAlreadyShort();
        }
    }

    public void testMissingTitle() throws Exception
    {
        // Revert to not set up state
        gotoSetupStep2();
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        // We should not be allowed to continue
        tester.assertTextPresent("Step 2 of 4: Application properties");
        tester.assertTextPresent("You must specify a title.");
        tester.assertTextNotPresent("Invalid license key specified.");
    }

    public void testMissingLicense() throws Exception
    {
        // Revert to not set up state
        gotoSetupStep2();
        tester.setFormElement("title", "My JIRA");
        tester.submit();
        // We should not be allowed to continue
        tester.assertTextPresent("Step 2 of 4: Application properties");
        tester.assertTextNotPresent("You must specify a title.");
        tester.assertTextPresent("Invalid license key specified.");
    }

    public void testInvalidLicense() throws Exception
    {
        // Revert to not set up state
        gotoSetupStep2();
        tester.setFormElement("title", "My JIRA");
        tester.setFormElement("license", "blah");
        tester.submit();
        // We should not be allowed to continue
        tester.assertTextPresent("Step 2 of 4: Application properties");
        tester.assertTextNotPresent("You must specify a title.");
        tester.assertTextPresent("Invalid license key specified.");
    }

    public void testInvalidSmtpPorts() throws Exception
    {
        doSetupStep1();
        doSetupStep3();

        //Lets try an invalid ports and make sure they don't work.
        tester.checkRadioOption("noemail","false");
        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("port", "-1");
        tester.submit("finish");
        tester.assertTextPresent("SMTP port must be a number between 0 and 65535");

        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("port", String.valueOf(0xFFFF + 1));
        tester.submit("finish");
        tester.assertTextPresent("SMTP port must be a number between 0 and 65535");
    }

    public void testSetupWithDefaultDirectories() throws IOException
    {
        // Revert to not set up state
        gotoSetupStep2();
        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "My JIRA");
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        // Submit Step 1 with Default paths.
        tester.submit();
        // Finish setup with standard values
        doSetupStep3and4();

        // Now assert JIRA is setup as expected:

        // Attachments
        navigation.gotoAdminSection("attachments");
        tester.assertTextPresent("Attachment Settings");
        // Assert the cells in table 'AttachmentSettings'.
        WebTable AttachmentSettings = tester.getDialog().getWebTableBySummaryOrId("table-AttachmentSettings");
        // Assert row 1: |Allow Attachments|ON|
        assertEquals("Cell (1, 0) in table 'AttachmentSettings' should be 'Allow Attachments'.", "Allow Attachments", AttachmentSettings.getCellAsText(0, 0).trim());
        assertEquals("Cell (1, 1) in table 'AttachmentSettings' should be 'ON'.", "ON", AttachmentSettings.getCellAsText(0, 1).trim());
        // Assert row 2: |Attachment Path|Default Directory [/home/mlassau/jira_homes/jira_trunk/data/attachments]|
        assertEquals("Cell (2, 0) in table 'AttachmentSettings' should be 'Attachment Path'.", "Attachment Path", AttachmentSettings.getCellAsText(1, 0).trim());
        assertTrue("Default Directory [/home/mlassau/jira_homes/jira_trunk/data/attachments]", AttachmentSettings.getCellAsText(1, 1).trim().startsWith("Default Directory ["));

        // Indexes
        navigation.gotoAdminSection("indexing");
        tester.assertTextPresent("Re-Indexing");

        // Automated Backups
        administration.services().goTo();
        tester.assertTextPresent("Backup Service");
        tester.assertTextPresent("<strong>USE_DEFAULT_DIRECTORY:</strong> true");
        tester.assertTextNotPresent("DIR_NAME:");
        administration.services().clickEdit("Backup Service");
        tester.assertFormElementEquals("USE_DEFAULT_DIRECTORY", "true");

        assertTimeTrackingActivationAndDefaultValues();
        assertIssueLinking();
    }

    public void testSetupImportMissingFilename() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use custom path
        tester.setFormElement("filename", "");
        tester.submit();

        // We should not be allowed to continue
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("You must enter the location of an XML file.");
        tester.assertTextNotPresent("You must specify a location for index files");
    }

    public void testSetupImportInvalidLicense() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use custom path
        tester.setFormElement("filename", File.createTempFile("import", ".xml").getAbsolutePath());
        tester.setFormElement("license", "wrong");
        tester.submit();
        administration.waitForRestore();

        // We should not be allowed to continue
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("Invalid license key specified.");
        tester.assertTextNotPresent("You must enter the location of an XML file.");
    }


    public void testSetupImportWithOldLicenseInXML() throws IOException
    {
        restoreEmptyInstance();

        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", new File(environmentData.getXMLDataLocation(), "oldlicense.xml").getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        text.assertTextPresent(new WebPageLocator(tester), "Please upgrade your license or generate an evaluation license.");
    }

    public void testSetupImportWithDodgyIndexPath() throws IOException
    {
        restoreEmptyInstance();

        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File indexPath = File.createTempFile("testXmlImportWithInvalidIndexDirectory", null);
        indexPath.createNewFile();
        indexPath.deleteOnExit();

        File dataFile = administration.replaceTokensInFile("TestSetupInvalidIndexPath.xml", EasyMap.build("@@INDEX_PATH@@", indexPath.getAbsolutePath()));

        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        text.assertTextPresent(new WebPageLocator(tester), "Cannot write to index directory. Check that the application server and JIRA have permissions to write to: " + indexPath.getAbsolutePath());
    }

    public void testSetupImportWithDodgyAttachmentPath() throws IOException
    {
        restoreEmptyInstance();

        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File attachmentPath = File.createTempFile("testXmlImportWithInvalidAttachmentDirectory", null);
        attachmentPath.createNewFile();
        attachmentPath.deleteOnExit();

        File dataFile = administration.replaceTokensInFile("TestSetupInvalidAttachmentPath.xml", EasyMap.build("@@ATTACHMENT_PATH@@", attachmentPath.getAbsolutePath()));

        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        text.assertTextPresent(new WebPageLocator(tester), "Cannot write to attachment directory. Check that the application server and JIRA have permissions to write to: " + attachmentPath.getAbsolutePath());
    }

    public void testSetupImportDefaultIndexDirectory() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", new File(environmentData.getXMLDataLocation(), "oldlicense.xml").getAbsolutePath());
        // need to set a new license, or we won't be allowed to log in.
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        administration.waitForRestore();
        tester.assertTextPresent("Setup Complete");

        // Now assert JIRA is setup with the Default Index directory
        navigation.disableWebSudo();
        navigation.login(ADMIN_USERNAME);
        navigation.gotoAdminSection("indexing");
        tester.assertTextPresent("Re-Indexing");
    }

    public void testSetupImportDefaultsForSetupComplete() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("This setup page is to import existing data from another JIRA installation.");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", new File(environmentData.getXMLDataLocation(), "oldlicense.xml").getAbsolutePath());
        // need to set a new license, or we won't be allowed to log in.
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        administration.waitForRestore();
        tester.assertTextPresent("Setup Complete");

        // Now assert JIRA is setup with sub tasks disabled
        navigation.disableWebSudo();
        navigation.login(ADMIN_USERNAME);

        assertSubTasksDisabled();

        assertDefaultTextRendererIsSetForAllRenderableFields();
    }

    private void assertSubTasksDisabled()
    {
        assertFalse("Sub-tasks were enabled when they shouldn't have been", administration.subtasks().isEnabled());
    }

    private void assertDefaultTextRendererIsSetForAllRenderableFields()
    {
        final String[] renderableFields = { "Comment", "Description", "Environment" };
        for (String fieldName : renderableFields)
        {
            assertEquals("Default Text Renderer", administration.fieldConfigurations().defaultFieldConfiguration().getRenderer(fieldName));
        }
    }

    private void restoreEmptyInstance()
    {
        administration.restoreNotSetupInstance();
    }

    private void assertTimeTrackingActivationAndDefaultValues()
    {
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        tester.assertTextPresent("The number of working hours per day is <b>8");
        tester.assertTextPresent("The number of working days per week is <b>5");
    }

    private void assertIssueLinking()
    {
        tester.gotoPage("secure/admin/ViewLinkTypes!default.jspa");
        WebPageLocator page = new WebPageLocator(tester);
        text.assertTextPresent(page,"Issue linking is currently ON.");
        text.assertTextSequence(page, new String[] { "Blocks", "blocks", "is blocked by" });
        text.assertTextSequence(page, new String[] { "Cloners", "clones", "is cloned by" });
        text.assertTextSequence(page, new String[] { "Duplicate", "duplicates", "is duplicated by" });
        text.assertTextSequence(page, new String[] { "Relates", "relates to", "relates to" });
    }

    private void assertSetupAlreadyLong()
    {
        tester.assertTextPresent("SETUP ALREADY");
        tester.assertTextPresent("It seems that you have tried to setup JIRA when it is already setup.");
    }

    private void assertSetupAlreadyShort()
    {
        tester.assertTextPresent("JIRA has already been set up.");
    }

    private void doSetupStep1()
    {
        gotoSetupStep2();

        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "TestSetup JIRA");
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        // Submit Step 1 with Default paths.
        tester.submit();
    }

    private void doSetupStep3and4()
    {
        doSetupStep3();
        doSetupStep4();
    }

    private void doSetupStep4()
    {
        log("Noemail");
        tester.submit("finish");
        log("Noemail");
        tester.assertTextPresent("Setup Complete");
        navigation.disableWebSudo();
        navigation.login(ADMIN_USERNAME);
    }

    private void doSetupStep3()
    {
        // Step 2
        tester.assertTextPresent("Step 3 of 4: " + ADMIN_FULLNAME + " account");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.setFormElement("fullname", "Mary Magdelene");
        tester.setFormElement("email", "admin@example.com");
        tester.submit();
        tester.assertTextPresent("Step 4 of 4: Email Notification");
    }

    private void gotoSetupStep2()
    {
        restoreEmptyInstance();
        tester.gotoPage("secure/Setup.jspa");
        tester.assertTextPresent("Step 2 of 4: Application properties");
    }
}
