/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.AlreadyExecutingException;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import webwork.action.ServletActionContext;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

@WebSudoRequired
public class IndexAdminImpl extends ProjectActionSupport implements IndexAdmin
{
    public static final String JIRA_IS_BEING_REINDEXED = "JIRA is currently being reindexed. Depending on how large the database is, this may take a few minutes. Jira will automatically become available as soon as this task is complete.";
    private static final String PROGRESS = "progress";

    private final IndexLifecycleManager indexLifecycleManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final TaskManager taskManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FileFactory fileFactory;

    private Long taskId;
    private TaskDescriptorBean<IndexCommandResult> currentTask;

    private long reindexTime = 0;
    private String indexPath;
    private final OutlookDateManager outlookDateManager;
    private TaskDescriptor<IndexCommandResult> currentTaskDescriptor;
    private final IndexPathManager indexPathManager;
    private IndexPathManager.Mode indexMode;

    // this constructor added for backwards-compatibility
    public IndexAdminImpl(IndexLifecycleManager indexLifecycleManager, final GlobalPermissionManager globalPermissionManager, final TaskManager taskManager, final JiraAuthenticationContext authenticationContext, final OutlookDateManager outlookDateManager, final IndexPathManager indexPathManager, FileFactory fileFactory)
    {
        this(ManagerFactory.getProjectManager(), ManagerFactory.getPermissionManager(), indexLifecycleManager, globalPermissionManager, taskManager, authenticationContext, outlookDateManager, indexPathManager, fileFactory);
    }

    public IndexAdminImpl(ProjectManager projectManager, PermissionManager permissionManager, IndexLifecycleManager indexLifecycleManager, final GlobalPermissionManager globalPermissionManager, final TaskManager taskManager, final JiraAuthenticationContext authenticationContext, final OutlookDateManager outlookDateManager, final IndexPathManager indexPathManager, FileFactory fileFactory)
    {
        super(projectManager, permissionManager);
        this.outlookDateManager = outlookDateManager;
        this.indexLifecycleManager = indexLifecycleManager;
        this.globalPermissionManager = globalPermissionManager;
        this.taskManager = taskManager;
        this.authenticationContext = authenticationContext;
        this.indexPathManager = indexPathManager;
        this.fileFactory = fileFactory;
        indexMode = indexPathManager.getMode();
        indexPath = indexPathManager.getIndexRootPath();
    }

    public String getIndexPathOption()
    {
            return indexMode.toString();
    }

    public void setIndexPathOption(final String indexPathOption)
    {
        indexMode = IndexPathManager.Mode.valueOf(indexPathOption);
    }

    /**
     * Returns the absolute path for the Default Index directory ([jira-home]/caches/index/)
     * This is used for read-only info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Index directory ([jira-home]/caches/index/)
     */
    public String getDefaultIndexPath()
    {
        return indexPathManager.getDefaultIndexRootPath();
    }

    public boolean getShowCustom()
    {
        // we need the second check (disabled && path not null) because they may have had a custom
        // path set...but then indexing got disabled somehow during startup.
        return indexMode == IndexPathManager.Mode.CUSTOM ||
                (indexMode == IndexPathManager.Mode.DISABLED && indexPath != null);
    }

    public String getIndexPath()
    {
        return indexPath;
    }

    private void validateIndexPath(final String path)
    {
        if (StringUtils.isBlank(path))
        {
            addError("indexPath", getText("admin.errors.you.must.specify.a.path"));
        }
        else
        {
            final File actualPath = fileFactory.getFile(path);

            if (!actualPath.isAbsolute())
            {
                addError("indexPath", getText("setup.error.filepath.notabsolute"));
            }
            else
            {
                if (!actualPath.exists()) // if doesn't exist, try to create it
                {
                    actualPath.mkdirs();
                }

                if (!actualPath.exists() || !actualPath.isDirectory())
                {
                    addError("indexPath", getText("admin.errors.path.entered.does.not.exist"));
                }
                else if (!actualPath.canRead() || !actualPath.canWrite())
                {
                    addError("indexPath", getText("admin.errors.path.entered.is.not.readable"));
                }
            }
        }
    }

    @Override
    public String doExecute() throws Exception
    {
        final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
        if (taskDescriptor != null)
        {
            return getRedirect(taskDescriptor.getProgressURL());
        }
        return super.doExecute();
    }

    @RequiresXsrfCheck
    public String doActivate() throws Exception
    {
        if (!isHasSystemAdminPermission())
        {
            addErrorMessage(getText("admin.errors.no.perm.to.activate"));
            return ERROR;
        }

        if (indexMode == IndexPathManager.Mode.DISABLED)
        {
            addErrorMessage(getText("admin.errors.you.must.specify.a.path"));
            return ERROR;
        }

        //Add a warning that an upgrade is in progress
        JohnsonEventContainer eventCont = null;

        final ServletContext ctx = ServletActionContext.getServletContext();
        if (ctx != null)
        {
            eventCont = JohnsonEventContainer.get(ctx);
        }
        // otherwise non-web action; assume something else (caller?) is responsible for notifying the user
        if (!isIndexing())
        {
            if (indexMode == IndexPathManager.Mode.CUSTOM)
            {
                validateIndexPath(indexPath);
            }

            if (!invalidInput())
            {
                updateIndexPathManager();
                final Callable<IndexCommandResult> activateCommand = new ActivateAsyncIndexerCommand(false, eventCont, indexLifecycleManager, log,
                    authenticationContext.getI18nHelper());
                final String taskName = getText("admin.indexing.jira.indexing");
                try
                {
                    final TaskDescriptor<IndexCommandResult> taskDescriptor = taskManager.submitTask(activateCommand, taskName,
                        new IndexTaskContext());
                    return getRedirect(taskDescriptor.getProgressURL());
                }
                catch (final AlreadyExecutingException e)
                {
                    return getRedirect(e.getTaskDescriptor().getProgressURL());
                }
                catch (final RejectedExecutionException e)
                {
                    addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
                    return ERROR;
                }
            }
            else
            {
                return ERROR;
            }
        }
        return getRedirect(getRedirectUrl());
    }

    private boolean isCustomPathChanged()
    {
        return (indexMode == IndexPathManager.Mode.CUSTOM) && (!indexPath.equals(indexPathManager.getIndexRootPath()));
    }

    private boolean isModeChanged()
    {
        return indexMode != indexPathManager.getMode();
    }

    @RequiresXsrfCheck
    public String doReindex() throws Exception
    {
        //Add a warning that an upgrade is in progress
        JohnsonEventContainer eventCont = null;

        if (isIndexing())
        {
            final ServletContext ctx = ServletActionContext.getServletContext();
            if (ctx != null)
            {
                eventCont = JohnsonEventContainer.get(ctx);
            }
            Callable<IndexCommandResult> indexCallable;
            if (isHasSystemAdminPermission())
            {
                if (isCustomPathChanged())
                {
                    validateIndexPath(indexPath);
                    if (invalidInput())
                    {
                        return ERROR;
                    }
                }

                if (isCustomPathChanged() || isModeChanged())
                {
                    updateIndexPathManager();
                    indexCallable = new ActivateAsyncIndexerCommand(true, eventCont, indexLifecycleManager, log,
                        authenticationContext.getI18nHelper());
                }
                else
                {
                    indexCallable = new ReIndexAsyncIndexerCommand(eventCont, indexLifecycleManager, log, authenticationContext.getI18nHelper());
                }
            }
            else
            {
                indexCallable = new ReIndexAsyncIndexerCommand(eventCont, indexLifecycleManager, log, authenticationContext.getI18nHelper());
            }
            final String taskName = getText("admin.indexing.jira.indexing");
            try
            {
                // re direct to progress action command
                return getRedirect(taskManager.submitTask(indexCallable, taskName, new IndexTaskContext()).getProgressURL());
            }
            catch (final AlreadyExecutingException e)
            {
                return getRedirect(e.getTaskDescriptor().getProgressURL());
            }
            catch (final RejectedExecutionException e)
            {
                addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
                return ERROR;
            }
        }
        return getRedirect(getRedirectUrl());
    }

    private void updateIndexPathManager()
    {
        if (indexMode == IndexPathManager.Mode.DEFAULT)
        {
            indexPathManager.setUseDefaultDirectory();
        }
        else if (indexMode == IndexPathManager.Mode.CUSTOM)
        {
            indexPathManager.setIndexRootPath(indexPath);
        }
    }

    public String doProgress() throws ExecutionException, InterruptedException
    {
        if (taskId == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.id"));
            return ERROR;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.found"));
            return ERROR;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof IndexTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", IndexTaskContext.class.getName(), context.getClass().getName()));
            return ERROR;
        }

        currentTask = new TaskDescriptorBean<IndexCommandResult>(currentTaskDescriptor, authenticationContext.getI18nHelper(), outlookDateManager,
            authenticationContext.getUser());
        if (currentTaskDescriptor.isFinished())
        {
            try
            {
                final IndexCommandResult result = currentTaskDescriptor.getResult();
                if (result.isSuccessful())
                {
                    reindexTime = result.getReindexTime();
                }
                else
                {
                    addErrorCollection(result.getErrorCollection());
                }
            }
            catch (final ExecutionException e)
            {
                currentTask.setExceptionCause(e.getCause() == null ? e : e.getCause());
            }
            catch (final InterruptedException e)
            {
                currentTask.setExceptionCause(e);
            }
        }
        return PROGRESS;
    }

    private String getRedirectUrl()
    {
        return "IndexAdmin.jspa";
    }

    public long getReindexTime()
    {
        return reindexTime;
    }

    public void setReindexTime(final long reindexTime)
    {
        this.reindexTime = reindexTime;
    }

    public boolean isAnyLiveTasks()
    {
        return !taskManager.getLiveTasks().isEmpty();
    }

    public boolean isHasSystemAdminPermission()
    {
        return globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getRemoteUser());
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public TaskDescriptorBean<IndexCommandResult> getOurTask()
    {
        return currentTask;
    }

    public TaskDescriptorBean<IndexCommandResult> getCurrentTask()
    {
        if (currentTask == null)
        {
            final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
            if (taskDescriptor != null)
            {
                currentTask = new TaskDescriptorBean<IndexCommandResult>(taskDescriptor, authenticationContext.getI18nHelper(), outlookDateManager,
                    authenticationContext.getUser());
            }
        }
        return currentTask;
    }

    private TaskDescriptor<IndexCommandResult> getCurrentTaskDescriptor()
    {
        if (currentTaskDescriptor == null)
        {
            currentTaskDescriptor = taskManager.getLiveTask(new IndexTaskContext());
        }

        return currentTaskDescriptor;
    }

    public String getDestinationURL()
    {
        return "/secure/admin/jira/IndexAdmin.jspa?reindexTime=" + reindexTime;
    }
}
