package com.atlassian.jira.web.bean;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.task.TaskProgressIndicator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * A UI styler bean to help format up a {@link com.atlassian.jira.task.TaskDescriptor}.
 *
 * @since v3.13
 */
public class TaskDescriptorBean<V> implements TaskDescriptor<V>
{
    private final TaskDescriptor<V> taskDescriptor;
    private final I18nHelper i18nHelper;
    private final OutlookDateManager outlookDateManager;
    private Throwable exceptionCause;
    private final User currentUser;
    private final TaskProgressEvent lastProgressEvent;

    public TaskDescriptorBean(final TaskDescriptor<V> taskDescriptor, final I18nHelper i18nHelper, final OutlookDateManager outlookDateManager, final User currentUser)
    {
        Assertions.notNull("taskDescriptor", taskDescriptor);
        Assertions.notNull("i18nHelper", i18nHelper);
        Assertions.notNull("outlookDateManager", outlookDateManager);

        this.currentUser = currentUser;
        this.taskDescriptor = taskDescriptor;
        this.i18nHelper = i18nHelper;
        this.outlookDateManager = outlookDateManager;
        exceptionCause = null;

        final TaskProgressIndicator taskProgressIndicator = taskDescriptor.getTaskProgressIndicator();
        if (taskProgressIndicator == null)
        {
            lastProgressEvent = null;
        }
        else
        {
            lastProgressEvent = taskProgressIndicator.getLastProgressEvent();
        }
    }

    public TaskDescriptor getTaskDescriptor()
    {
        return taskDescriptor;
    }

    public String getFormattedElapsedRunTime()
    {
        final long elaspedTime = taskDescriptor.getElapsedRunTime() / 1000;
        final ResourceBundle resourceBundle = i18nHelper.getDefaultResourceBundle();
        return DateUtils.getDurationPrettySecondsResolution(elaspedTime, resourceBundle);
    }

    public V getResult() throws ExecutionException, InterruptedException
    {
        return taskDescriptor.getResult();
    }

    public boolean isStarted()
    {
        return taskDescriptor.isStarted();
    }

    public boolean isFinished()
    {
        return taskDescriptor.isFinished();
    }

    public Long getTaskId()
    {
        return taskDescriptor.getTaskId();
    }

    public Date getStartedTimestamp()
    {
        return taskDescriptor.getStartedTimestamp();
    }

    public String getFormattedStartedTimestamp()
    {
        return getFormattedTimestamp(getStartedTimestamp());
    }

    public Date getFinishedTimestamp()
    {
        return taskDescriptor.getFinishedTimestamp();
    }

    public String getFormattedFinishedTimestamp()
    {
        return getFormattedTimestamp(getFinishedTimestamp());
    }

    public Date getSubmittedTimestamp()
    {
        return taskDescriptor.getSubmittedTimestamp();
    }

    public String getFormattedSubmittedTimestamp()
    {
        return getFormattedTimestamp(getSubmittedTimestamp());
    }

    public long getElapsedRunTime()
    {
        return taskDescriptor.getElapsedRunTime();
    }

    public User getUser()
    {
        return taskDescriptor.getUser();
    }

    public String getDescription()
    {
        return taskDescriptor.getDescription();
    }

    public TaskContext getTaskContext()
    {
        return taskDescriptor.getTaskContext();
    }

    /**
     * We dont re-expose this.  Use getLastProgressEvent().
     *
     * @return always null
     */
    public TaskProgressIndicator getTaskProgressIndicator()
    {
        return null;
    }

    public String getFormattedProgress()
    {
        if (!taskDescriptor.isStarted())
        {
            return i18nHelper.getText("common.tasks.info.starting", getFormattedSubmittedTimestamp());
        }

        if (taskDescriptor.isFinished())
        {
            if (exceptionCause != null)
            {
                return i18nHelper.getText("common.tasks.info.completed.with.error", getFormattedElapsedRunTime());
            }
            else
            {
                return i18nHelper.getText("common.tasks.info.completed", getFormattedElapsedRunTime());
            }
        }

        if (lastProgressEvent != null)
        {
            if (lastProgressEvent.getTaskProgress() >= 0)
            {
                return i18nHelper.getText("common.tasks.info.progressing", String.valueOf(getProgressNumber()), getFormattedElapsedRunTime());
            }
        }

        return i18nHelper.getText("common.tasks.info.progress.unknown", getFormattedElapsedRunTime());
    }

    /**
     * Returns a number between 0 and 100 and caters for lack of progress and finished tasks.
     *
     * @return a number between 0 and 100 and caters for lack of progress and finished tasks.
     */

    public long getProgressNumber()
    {
        if (taskDescriptor.isFinished() || (taskDescriptor.getTaskProgressIndicator() == null))
        {
            return 100;
        }
        if (lastProgressEvent == null)
        {
            return 0;
        }
        return Math.max(Math.min(100, lastProgressEvent.getTaskProgress()), 0);
    }

    public long getInverseProgressNumber()
    {
        return 100 - getProgressNumber();
    }

    public void setExceptionCause(final Throwable cause)
    {
        exceptionCause = cause;
    }

    public Throwable getExceptionCause()
    {
        return exceptionCause;
    }

    public String getFormattedExceptionCause()
    {
        if (exceptionCause != null)
        {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            exceptionCause.printStackTrace(printWriter);
            printWriter.close();

            return stringWriter.toString();
        }
        return null;
    }

    public boolean isUserWhoStartedTask()
    {
        //during an import there may be no user.
        return (currentUser == null && taskDescriptor.getUser() == null) || (currentUser != null && currentUser.equals(taskDescriptor.getUser()));
    }

    public String getProgressURL()
    {
        String progressURL = taskDescriptor.getProgressURL();
        final HttpServletRequest servletRequest = ActionContext.getRequest();
        if (servletRequest != null)
        {
            progressURL = servletRequest.getContextPath() + progressURL;
        }
        return progressURL;
    }

    public String getUserURL()
    {
        //during an import there may be no user.
        if(getUser() != null)
        {
            String usrURL = "/secure/admin/user/ViewUser.jspa?name=" + getUser().getName();
            final HttpServletRequest servletRequest = ActionContext.getRequest();
            if ((servletRequest != null) && (servletRequest.getContextPath() != null))
            {
                usrURL = servletRequest.getContextPath() + usrURL;
            }
            return usrURL;
        }
        return "";
    }

    public TaskProgressEvent getLastProgressEvent()
    {
        return lastProgressEvent;
    }

    private String getFormattedTimestamp(final Date timestamp)
    {
        if (timestamp != null)
        {
            final OutlookDate date = outlookDateManager.getOutlookDate(i18nHelper.getLocale());
            return date.format(timestamp);
        }
        return "";
    }
}
