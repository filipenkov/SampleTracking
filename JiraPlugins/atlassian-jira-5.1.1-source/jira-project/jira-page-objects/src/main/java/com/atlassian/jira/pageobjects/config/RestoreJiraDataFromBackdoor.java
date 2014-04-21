package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.functest.framework.backdoor.DataImportControl;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;

import java.io.File;

/**
 * Implementation of {@link RestoreJiraData} that uses the func test plugin REST resource if present
 */
public class RestoreJiraDataFromBackdoor implements RestoreJiraData
{
    private DataImportControl dataImportControl;
    private JiraTestedProduct jiraProduct;

    public RestoreJiraDataFromBackdoor(JiraTestedProduct jiraProduct)
    {
        this.jiraProduct = jiraProduct;
        dataImportControl = new DataImportControl(jiraProduct.environmentData());
    }

    @Override
    public void execute(String resourcePath)
    {
        final String fileName = getClass().getClassLoader().getResource(resourcePath).getFile();
        final File importFile = new File(fileName);

        // When provided, the resourcePath is of the form "xml/<backup>.xml" as the WebDriver RestoreJiraDataFromUI
        // implementation uses classpath resource loading to copy XMLs, as opposed to copying the file.
        // Unfortunately, the func test plugin expects only the <backup>.xml fragment of the path to be provided.
        //
        // For compatibility,
        //
        //  * Get the physical file location from the classpath resource
        //  * Get the actual XML location as provided by environment data
        //  * Remove the XML location fragment from the physical file location of the classpath resource
        //
        final String importFilePath = importFile.getAbsolutePath()
                .replace(jiraProduct.environmentData().getXMLDataLocation().getAbsolutePath(), "");

        dataImportControl.restoreData(importFilePath);
        jiraProduct.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
    }

}
