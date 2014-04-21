package com.atlassian.jira.webtests.ztests.ao;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@WebTest ({Category.FUNC_TEST, Category.REFERENCE_PLUGIN, Category.ACTIVE_OBJECTS, Category.PLUGINS })
public class TestActiveObjectsRestore extends FuncTestCase
{
    public TestActiveObjectsRestore(String name)
    {
        this.setName(name);
    }


    public void testRestoreDataSuccessfully()
    {
        administration.restoreBlankInstance();
        String filePath = "ActiveObjects.zip";
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + filePath);
        copyFileToJiraImportDirectory(file);
        // restore data again
        getTester().gotoPage("secure/admin/XmlRestore!default.jspa");
        getTester().setWorkingForm("jiraform");

        getTester().setFormElement("filename", filePath);
        getTester().setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        getTester().submit();
        administration.waitForRestore();
        getTester().assertTextPresent("Your project has been successfully imported");
        getTester().assertTextNotPresent("NullPointerException");
    }

    public void testRestoreWithDatabaseErrors()
    {
        try
        {
            administration.restoreData("ActiveObjectsBadData.zip", false);
        }
        catch (Throwable e)
        {
            assertTrue("Active objects bad data",e.getMessage().startsWith("Failed to restore JIRA data from"));
        }
        final String expectedMessage = "There was a problem restoring data for the Atlassian JIRA - Plugins - Development Only - Reference Plugin plugin.";
        if (!tester.getDialog().isTextInResponse(expectedMessage))
        {
            fail(String.format("Could not find '%s' in page. Are you sure you have the jira-reference-plugin installed?", expectedMessage));
        }
        tester.assertTextPresent(expectedMessage);
    }


    private void copyFileToJiraImportDirectory(File file)
    {
        String filename = file.getName();
        File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
        try
        {
            FileUtils.copyFileToDirectory(file, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + file.getAbsolutePath() +
                    " to the import directory in jira home " + jiraImportDirectory, e);
        }
    }
}
