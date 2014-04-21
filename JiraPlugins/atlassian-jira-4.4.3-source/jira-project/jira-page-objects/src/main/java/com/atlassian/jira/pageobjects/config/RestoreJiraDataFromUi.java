package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.admin.RestoreDataPage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.StringDescription;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>
 * Implementation of {@link RestoreJiraData} that uses UI.
 *
 * <p>
 * Web-sudo needs to be disabled for this to work.
 *
 * @since v4.4
 */
public class RestoreJiraDataFromUi implements RestoreJiraData
{
    private JiraTestedProduct product;
    private JiraConfigProvider configProvider;
    
    @Inject public RestoreJiraDataFromUi(JiraTestedProduct product, JiraConfigProvider configProvider)
    {
        this.product = checkNotNull(product);
        this.configProvider = configProvider;
    }

    public void execute(String resourcePath)
    {
        final String name = prepareImportFile(resourcePath);
        product.gotoLoginPage().loginAsSysAdmin(JiraAdminHomePage.class);
        product.goTo(RestoreDataPage.class)
                .setFileName(name)
                .setQuickImport(true)
                .submitRestore()
                .waitForRestoreCompleted()
                .followLoginLink();
    }

    private String prepareImportFile(String resourcePath)
    {
        final String importFileName = importFileNameFor(checkNotNull(resourcePath));
        final InputStream resourceStream = getImportFileStream(resourcePath);
        final String targetPath = jiraImportPath() + "/" + importFileName;
        OutputStream targetStream = null;
        try
        {
            targetStream = new FileOutputStream(targetPath);
            IOUtils.copy(resourceStream, targetStream);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(new StringDescription()
                    .appendText("Error while trying to restore JIRA data from resource ").appendValue(resourcePath)
                    .toString(), ioe);
        }
        finally
        {
            IOUtils.closeQuietly(resourceStream);
            IOUtils.closeQuietly(targetStream);
        }
        return importFileName;
    }

    private String importFileNameFor(String resourcePath)
    {
        String extension = FilenameUtils.getExtension(resourcePath);
        return extension != null ? resourcePath.hashCode() + "." + extension : resourcePath.hashCode() + ".xml";
    }

    private InputStream getImportFileStream(String resourcePath)
    {
        return checkNotNull(getClass().getClassLoader().getResourceAsStream(resourcePath),
                "Import resource with path \"" + resourcePath + "\" not found");
    }

    private String jiraImportPath()
    {
        return configProvider.jiraHomePath() + "/import";
    }
}
