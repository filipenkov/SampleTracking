package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.RestoreDataPage;

import javax.inject.Inject;

/**
 * Config provider that uses jira-func-test-plugin if possible, or falls back to parsing JIRA UI.
 *
 * @since v4.4
 */
public class AdaptingConfigProvider implements JiraConfigProvider
{
    @Inject
    private JiraTestedProduct jiraProduct;

    @Inject
    FuncTestPluginDetector pluginDetector;

    private RestConfigProvider restProvider;

    private String jiraHomePath; // this shouldn't change evar!

    @Override
    public String jiraHomePath()
    {
        if (jiraHomePath == null)
        {
            jiraHomePath = initJiraHomePath();
        }
        return jiraHomePath;
    }

    private String initJiraHomePath()
    {
        if (pluginDetector.isFuncTestPluginInstalled())
        {

            return rest().jiraHomePath();
        }
        else
        {
            // simple assumption is that we're AMPS:) so JIRA is setup
            jiraProduct.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
            String importPath = jiraProduct.goTo(RestoreDataPage.class).getDefaultImportPath();
            if (importPath.endsWith("/import"))
            {
                return importPath.substring(0, importPath.lastIndexOf("/import"));
            }
            else
            {
                throw new RuntimeException("This is not a freakin import dir :P " + importPath);
            }
        }
    }

    @Override
    public boolean isSetUp()
    {
        if (pluginDetector.isFuncTestPluginInstalled())
        {
            return rest().isSetUp();
        }
        else
        {
            // simple assumption is that we're AMPS:) so JIRA is setup
            return true;
        }
    }

    private JiraConfigProvider rest()
    {
        if (restProvider == null)
        {
            restProvider = new RestConfigProvider(jiraProduct.getProductInstance());
        }
        return restProvider;
    }
}

