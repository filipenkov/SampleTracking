package com.atlassian.jira.plugins.mail.extensions;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * Validates whether there is at least one project present.
 *
 * @since v5.0
 */
public class ProjectRequiredMessageHandlerValidator implements MessageHandlerValidator
{
    private final ProjectManager projectManager;

    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectRequiredMessageHandlerValidator(ProjectManager projectManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.projectManager = projectManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }


    @Override
    public ErrorCollection validate()
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        if (projectManager.getProjectObjects().isEmpty())
        {
            errorCollection.addErrorMessage(jiraAuthenticationContext.getI18nHelper().getText("jmp.editServerDetails.no.projects"));
        }
        return errorCollection;
    }
}
