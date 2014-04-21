package com.atlassian.jira.webtest.selenium.admin.imports.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @since v3.13
 */
@SkipInBrowser(browsers={Browser.IE}) //Expected text not found "Data exported to" - JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestProjectImportSelectProject extends JiraSeleniumTest
{
    private File tempFile;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestProjectImportSelectProject.xml");
        try
        {
            tempFile = File.createTempFile("ProjectImportFuncTest", ".xml");
            tempFile.deleteOnExit();
            tempFile = new File(exportDataToPath(tempFile.getAbsolutePath()));
            tempFile.deleteOnExit();
            FileUtils.copyFileToDirectory(tempFile, new File(getWebUnitTest().getAdministration().getJiraHomeDirectory(), "import"));
            deleteDogProject();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    private void deleteDogProject()
    {
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("view_projects");
        getNavigator().clickAndWaitForPageLoad("link=Delete");
        getNavigator().clickAndWaitForPageLoad("Delete");
    }

    public void testProjectDetailsAndWarnings()
    {
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("project_import");
        client.type("backupXmlPath", tempFile.getName());
        getNavigator().clickAndWaitForPageLoad("project-import-submit");
        advanceThroughWaitingPage();

        // Assert stuff about the dog project
        assertThat.elementHasText("projectDetailsTable", "This is a dog description.");
        assertThat.elementHasText("projectDetailsTable", "dog@example.com");
        assertThat.elementHasText("projectDetailsTable", "0");
        assertThat.elementHasText("warningBox", "No project with key 'DOG' exists in this instance of JIRA. The importer will create a project with this key and the details of the backup project using the default schemes.");
        // There was a warning, but we are still allowed to import, therefore the "overwrite" checkbox and "Import" button should be enabled.
        assertFalse(client.isVisible("overwriteelements"));
        assertTrue(client.isEditable("Next"));

        // Assert stuff about the homosap project
        client.select("project_select", "label=homosapien");
        assertEquals("Project:", client.getTable("projectDetailsTable.0.0"));
        assertEquals("homosapien", client.getTable("projectDetailsTable.0.1"));
        assertEquals("Key:", client.getTable("projectDetailsTable.1.0"));
        assertEquals("HSP", client.getTable("projectDetailsTable.1.1"));
        assertEquals("Description:", client.getTable("projectDetailsTable.2.0"));
        assertEquals("This is the description for project homosapiens. This is a long description so we can see what this will do to the html we are using to display this on the project import, project selection screen.", client.getTable("projectDetailsTable.2.1"));
        assertEquals("Lead:", client.getTable("projectDetailsTable.3.0"));
        assertEquals("admin", client.getTable("projectDetailsTable.3.1"));
        assertEquals("URL:", client.getTable("projectDetailsTable.4.0"));
        assertEquals("http://www.homosapien.com", client.getTable("projectDetailsTable.4.1"));
        assertEquals("Sender Address:", client.getTable("projectDetailsTable.5.0"));
        assertEquals("tester@example.com", client.getTable("projectDetailsTable.5.1"));
        assertEquals("Default Assignee:", client.getTable("projectDetailsTable.6.0"));
        assertEquals("Project Lead", client.getTable("projectDetailsTable.6.1"));
        assertEquals("Issues:", client.getTable("projectDetailsTable.7.0"));
        assertEquals("26", client.getTable("projectDetailsTable.7.1"));
        assertEquals("Components:", client.getTable("projectDetailsTable.8.0"));
        assertEquals("3", client.getTable("projectDetailsTable.8.1"));
        assertEquals("Versions:", client.getTable("projectDetailsTable.9.0"));
        assertEquals("3", client.getTable("projectDetailsTable.9.1"));
        // Assert the expected Error Messages
        assertThat.elementHasText("errorBox", "The existing project with key 'HSP' contains '26' issues. You can not import a backup project into a project that contains existing issues.");
        assertThat.elementHasText("errorBox", "The existing project with key 'HSP' contains '3' versions. You can not import a backup project into a project that contains existing versions.");
        assertThat.elementHasText("errorBox", "The existing project with key 'HSP' contains '3' components. You can not import a backup project into a project that contains existing components.");
        // The errors mean that we are not allowed to improt the project.
        // ASsert that the button and checkbox are disabled.
        assertFalse(client.isVisible("overwriteelements"));
        assertFalse(client.isEditable("Next"));

        // Change to monkey
        client.select("project_select", "label=monkey");
        // Assert the values in the Project Details Table
        assertEquals("Project:", client.getTable("projectDetailsTable.0.0"));
        assertEquals("monkey", client.getTable("projectDetailsTable.0.1"));
        assertEquals("Key:", client.getTable("projectDetailsTable.1.0"));
        assertEquals("MKY", client.getTable("projectDetailsTable.1.1"));
        assertEquals("Description:", client.getTable("projectDetailsTable.2.0"));
        assertEquals("project for monkeys", client.getTable("projectDetailsTable.2.1"));
        assertEquals("Lead:", client.getTable("projectDetailsTable.3.0"));
        assertEquals("admin", client.getTable("projectDetailsTable.3.1"));
        assertEquals("URL:", client.getTable("projectDetailsTable.4.0"));
        assertEquals("http://www.monkey.com", client.getTable("projectDetailsTable.4.1"));
        assertEquals("Sender Address:", client.getTable("projectDetailsTable.5.0"));
        assertEquals("dylan@atlassian.com", client.getTable("projectDetailsTable.5.1"));
        assertEquals("Default Assignee:", client.getTable("projectDetailsTable.6.0"));
        assertEquals("Unassigned", client.getTable("projectDetailsTable.6.1"));
        assertEquals("Issues:", client.getTable("projectDetailsTable.7.0"));
        assertEquals("0", client.getTable("projectDetailsTable.7.1"));
        assertEquals("Components:", client.getTable("projectDetailsTable.8.0"));
        assertEquals("0", client.getTable("projectDetailsTable.8.1"));
        assertEquals("Versions:", client.getTable("projectDetailsTable.9.0"));
        assertEquals("0", client.getTable("projectDetailsTable.9.1"));
        assertTrue(client.isVisible("overwriteelements"));
        assertTrue(client.isEditable("Next"));
    }

    public void advanceThroughWaitingPage()
    {
        int count = 0;
        while (client.isTextPresent("Project Import: Progress"))
        {
            // We need to click the refresh which should take us to the error page
            getNavigator().clickAndWaitForPageLoad("Refresh");
            // OK - we are still in progress. Wait a little while before we try again.
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Not expected.
                throw new RuntimeException(e);
            }
            // Safety net to make sure that we don't get in an infinite loop.
            count++;
            if (count >= 100)
            {
                fail("Our project import backup selection has taken too long!");
            }
        }
    }

    private String exportDataToPath(String absolutePath)
    {
        getNavigator().gotoAdmin();
        getNavigator().clickAndWaitForPageLoad("backup_data");

        getNavigator().gotoPage("secure/admin/XmlBackup!default.jspa", true);
        client.type("filename", absolutePath);
        getNavigator().clickAndWaitForPageLoad("Backup");

        if (client.isTextPresent("Please confirm whether you want to replace this file."))
        {
            getNavigator().clickAndWaitForPageLoad("Replace File");
        }

        assertThat.textPresent("Data exported to");
        //find the export path
        final String response = client.getHtmlSource();
        int startIndex = response.indexOf("<b>", response.indexOf("Data exported to"));
        int endIndex = response.indexOf("</b>", response.indexOf("Data exported to"));
        return response.substring(startIndex + "<b>".length(), endIndex);
    }
}
