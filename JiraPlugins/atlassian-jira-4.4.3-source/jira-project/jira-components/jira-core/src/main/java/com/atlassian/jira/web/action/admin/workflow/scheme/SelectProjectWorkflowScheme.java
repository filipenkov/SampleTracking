/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.AbstractSelectProjectScheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.migration.enterprise.EnterpriseWorkflowTaskContext;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class SelectProjectWorkflowScheme extends AbstractSelectProjectScheme
{
    private final TaskManager taskManager;

    private TaskDescriptorBean currentActivateTask;

    private final JiraAuthenticationContext authenticationContext;

    private final OutlookDateManager outlookDateManager;

    public SelectProjectWorkflowScheme(TaskManager taskManager, JiraAuthenticationContext authenticationContext, OutlookDateManager outlookDateManager)
    {
        this.outlookDateManager = outlookDateManager;
        this.authenticationContext = authenticationContext;
        this.taskManager = taskManager;
    }

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getWorkflowSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }

    protected TaskManager getTaskManager()
    {
        return taskManager;
    }

    public boolean isAnyLiveTasks()
    {
        return !taskManager.getLiveTasks().isEmpty();
    }

    protected void initTaskDescriptorBean(TaskDescriptor taskDescriptor)
    {
        currentActivateTask = new TaskDescriptorBean(taskDescriptor, authenticationContext.getI18nHelper(), outlookDateManager, authenticationContext.getUser());
    }

    /**
     * Return the {@link com.atlassian.jira.web.bean.TaskDescriptorBean} associated with the task that is currently
     * migrating the workflow for the current project. The method can be told search for such a task if necessary. This
     * feature is useful when the action needs to see if there are currently any tasks migrating the current project.
     *
     * @param searchForTask When true the method will attempt to find a task that is currently migrating our project.
     *  When false will simply return the bean configured with {@link #initTaskDescriptorBean(com.atlassian.jira.task.TaskDescriptor)}
     *  or null is no bean was configured. 
     *
     * @return a task or null if none can be found.
     */

    protected TaskDescriptorBean getCurrentTask(boolean searchForTask)
    {
        if (currentActivateTask == null && searchForTask)
        {
            EnterpriseWorkflowTaskContext taskContext = new EnterpriseWorkflowTaskContext(getProjectId(), getSchemeId());
            TaskDescriptor taskDescriptor = taskManager.getLiveTask(taskContext);
            if (taskDescriptor != null)
            {
                initTaskDescriptorBean(taskDescriptor);
            }
        }
        return currentActivateTask;
    }

    public TaskDescriptorBean getCurrentTask()
    {
        return getCurrentTask(true);
    }
}
