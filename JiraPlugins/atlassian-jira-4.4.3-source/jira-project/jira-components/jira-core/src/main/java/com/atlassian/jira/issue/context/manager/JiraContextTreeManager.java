package com.atlassian.jira.issue.context.manager;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.project.ProjectManager;

public class JiraContextTreeManager
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private JiraContextNode rootNode;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public JiraContextTreeManager(ProjectManager projectManager, ConstantsManager constantsManager)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public JiraContextNode getRootNode()
    {
        if (rootNode == null)
        {
            recreateRootNode();
        }

        return rootNode;
    }

    public void refresh()
    {
        rootNode = null;
    }



    // ------------------------------------------------------------------------------------------ Private Helper Methods
    private void recreateRootNode()
    {
        rootNode = new GlobalIssueContext(this);
    }

    public static JiraContextNode getRootContext()
    {
        return new GlobalIssueContext(new JiraContextTreeManager(ManagerFactory.getProjectManager(), ManagerFactory.getConstantsManager()));
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }
}
