package com.atlassian.jira.webtests.ztests.admin.services;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.TempDirectoryUtil;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebTable;
import org.apache.commons.io.FileUtils;

import java.io.File;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestBackupService extends FuncTestCase
{
    public void testAddAndEditBackupService() throws Exception
    {
        administration.restoreData("TestBackupService.xml");
        // Goto Services
        navigation.gotoAdminSection("services");
        tester.assertTextPresent("Add a new service by entering a name and class below. You can then edit it to set properties");
        // Add a Backup Service
        tester.setFormElement("name", "My Backup Service");
        tester.setFormElement("clazz", "com.atlassian.jira.service.services.export.ExportService");
        tester.setFormElement("delay", "100");
        tester.submit("Add Service");
        tester.assertTextPresent("Edit Service: My Backup Service");
        tester.assertFormElementEquals("delay", "100");

        // Configure it
        tester.checkCheckbox("USE_DEFAULT_DIRECTORY", "true");
        // Change our mind on the delay and set to 99
        tester.setFormElement("delay", "99");
        tester.submit("Update");

        // Assert the Service is as expected:
        // Assert the cells in table 'tbl_services'.
        WebTable tbl_services = tester.getDialog().getWebTableBySummaryOrId("tbl_services");
        // Assert row 0: |Name / Class|Properties|Delay (mins)|Operations|
        assertEquals("Cell (0, 0) in table 'tbl_services' should be 'Name / Class'.", "Name / Class", tbl_services.getCellAsText(0, 0).trim());
        assertEquals("Cell (0, 1) in table 'tbl_services' should be 'Properties'.", "Properties", tbl_services.getCellAsText(0, 1).trim());
        assertEquals("Cell (0, 2) in table 'tbl_services' should be 'Delay (mins)'.", "Delay (mins)", tbl_services.getCellAsText(0, 2).trim());
        assertEquals("Cell (0, 3) in table 'tbl_services' should be 'Operations'.", "Operations", tbl_services.getCellAsText(0, 3).trim());

        // Assert row: |My Backup Service com.atlassian.jira.service.services.export.ExportService|USE_DEFAULT_DIRECTORY:true |99|Edit Delete|
        TableLocator tableLocator = new TableLocator(tester, "tbl_services");
        text.assertTextSequence(tableLocator, "My Backup Service", "com.atlassian.jira.service.services.export.ExportService", "USE_DEFAULT_DIRECTORY", "true", "99");
        text.assertTextNotPresent(tableLocator, "DIR_NAME");

        // Now we will edit this service
        tester.clickLink("edit_10020");
        tester.uncheckCheckbox("USE_DEFAULT_DIRECTORY");
        tester.setFormElement("delay", "3600");
        tester.submit("Update");

        // Assert row: |My Backup Service com.atlassian.jira.service.services.export.ExportService|USE_DEFAULT_DIRECTORY:true |99|Edit Delete|
        tableLocator = new TableLocator(tester, "tbl_services");
        text.assertTextSequence(tableLocator, "My Backup Service", "com.atlassian.jira.service.services.export.ExportService", "3600");
        text.assertTextNotPresent(tableLocator, "USE_DEFAULT_DIRECTORY");
    }

    /**
     * This tests that if they have custom paths in legacy data, then there are displayed and respected but cant be
     * edited
     * <p/>
     * The previous tests check that the paths cannot be set during creation
     */
    public void testWhenDataHasLegacyPathInIt() throws Exception
    {
        File attachment = TempDirectoryUtil.createTempDirectory("tWDHLPII");
        File indexes = TempDirectoryUtil.createTempDirectory("tWDHLPII");
        File fileservice = TempDirectoryUtil.createTempDirectory("tWDHLPII");
        File backup = TempDirectoryUtil.createTempDirectory("tWDHLPII");

        administration.restoreDataWithReplacedTokens("TestCustomPathInLegacyData.xml",
                ImmutableMap.<String, String>of("@INDEX_DIR@", indexes.getAbsolutePath(),
                        "@ATTACHMENT_DIR@", attachment.getAbsolutePath(),
                        "@BACKUP_DIR@", backup.getAbsolutePath(),
                        "@FILESERVICE_DIR@", fileservice.getAbsolutePath()));
        navigation.gotoAdminSection("services");

        TableLocator tableLocator = new TableLocator(tester, "tbl_services");

        /// it should reflect that they cant do stuff
        text.assertTextSequence(tableLocator, "File Service", "com.atlassian.jira.service.services.file.FileService", "directory", fileservice.getAbsolutePath(), "handler", "Non Quoted Comment Handler");
        text.assertTextSequence(tableLocator, "Backup Service", "com.atlassian.jira.service.services.export.ExportService", "DIR_NAME", backup.getAbsolutePath());

        navigation.gotoPage(page.addXsrfToken("secure/admin/EditService!default.jspa?id=10021")); // edit file service
        text.assertTextPresent(fileservice.getAbsolutePath());
        tester.submit("Update");

        navigation.gotoPage(page.addXsrfToken("secure/admin/EditService!default.jspa?id=10001")); // edit backup service
        text.assertTextPresent(backup.getAbsolutePath());
        tester.submit("Update");

        // the paths should not have changed at all
        text.assertTextSequence(tableLocator, "File Service", "com.atlassian.jira.service.services.file.FileService", "directory", fileservice.getAbsolutePath(), "handler", "Non Quoted Comment Handler");
        text.assertTextSequence(tableLocator, "Backup Service", "com.atlassian.jira.service.services.export.ExportService", "DIR_NAME", backup.getAbsolutePath());

        //Make sure we ain't using any of the directories before we kill them.
        administration.restoreBlankInstance();

        FileUtils.deleteDirectory(attachment);
        FileUtils.deleteDirectory(indexes);
        FileUtils.deleteDirectory(fileservice);
        FileUtils.deleteDirectory(backup);
    }

    private void createPaths(String[] paths) throws Exception
    {
        for (String path: paths)
        {
             setupDirectory(path);
        }

    }

      private void setupDirectory(String directoryName) throws Exception
    {
        File directory = new File(directoryName);
        if (!directory.exists())
        {
            directory.mkdirs();
        }
    }
}
