package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * Provide differing implementations of {@link RestoreJiraData}, depending on whether the
 * func test plugin is installed.
 */
public class RestoreJiraDataProvider implements Provider<RestoreJiraData>
{
    @Inject
    private JiraTestedProduct jiraProduct;

    @Inject
    private FuncTestPluginDetector pluginDetector;

    @Inject
    private JiraConfigProvider jiraConfigProvider;

    @Override
    public RestoreJiraData get()
    {
        if (pluginDetector.isFuncTestPluginInstalled())
        {
            return new RestoreJiraDataFromBackdoor(jiraProduct);
        }
        else
        {
            return new RestoreJiraDataFromUi(jiraProduct, jiraConfigProvider);
        }
    }
}
