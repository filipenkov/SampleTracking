package com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtests.LicenseKeys;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@WebTest({Category.SELENIUM_TEST })
public class TestXmlImport extends JiraSeleniumTest
{
    public void testRestoreUsingDefaultPaths()
    {
        restoreBlankInstance();
        
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath(), "TestXMLRestore.xml");
        File jiraImportDirectory = new File(getWebUnitTest().getAdministration().getJiraHomeDirectory(), "import");
        try
        {
            FileUtils.copyFileToDirectory(file, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + file.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
        }

        getNavigator().gotoAdmin();
        client.click("restore_data", true);
        client.type("filename", file.getName());
        client.type("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        client.submit("//form[@name='jiraform']");
        client.waitForPageToLoad(PAGE_LOAD_WAIT_TIME);
        waitForRestore();

        assertThat.textPresent("Either create the paths shown below and reimport, or reimport using default paths.");
        assertThat.textPresent("The index path");
        assertThat.textPresent("The attachment path");

        client.click("reimport", true);
        waitForRestore();
        assertThat.textPresent("Your project has been successfully imported.");    
    }
}
