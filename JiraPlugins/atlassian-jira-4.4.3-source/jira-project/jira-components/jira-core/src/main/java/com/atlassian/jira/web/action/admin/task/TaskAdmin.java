package com.atlassian.jira.web.action.admin.task;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The webwork action for task administration.
 *
 * NOTE : TaskAdmin is not quite ready to be GENERALLY AVAILABLE.  The reasoning is that we dont yet have enough
 * type of JIRA operations converted to long running tasks.  Until we do this page will be present but no linked to via system
 * admin menus.
 *
 * @since v3.13
 */
@WebSudoRequired
public class TaskAdmin extends JiraWebActionSupport
{
    private final Collection allTasks;
    private final long liveTaskCount;

    /**
     * This constructor takes a snap-shot of the tasks at the time the webwork action is created.  This ensures that their is a consistent
     * "view" between the action and is jsp view.  Problems arise if you dont do this.
     * 
     * @param taskManager the {@link com.atlassian.jira.task.TaskManager}  needed
     * @param outlookDateManager the {@link com.atlassian.jira.web.util.OutlookDateManager}  needed
     * @param authenticationContext the {@link com.atlassian.jira.security.JiraAuthenticationContext} needed
     */
    public TaskAdmin(final TaskManager taskManager, final OutlookDateManager outlookDateManager, final JiraAuthenticationContext authenticationContext)
    {
        // ----------------------------------------------------------------
        //
        // if you need some code to create tasks look at TerriblyHackyTaskAdminHelperCopy in the test packages.  It
        // may help you in your testing.
        //
        // TerriblyHackyTaskAdminHelperCopy.awfulHackToKickOffSomeTasksMaybe(taskManager);
        //
        // ----------------------------------------------------------------

        // we take a snap shot of tasks at the time this action comes into being so that we
        // dont get discrepancy bertween the action and the view
        final Collection rawTasks = taskManager.getAllTasks();
        int liveTaskCount = 0;
        allTasks = new ArrayList();
        for (final Iterator iterator = rawTasks.iterator(); iterator.hasNext();)
        {
            final TaskDescriptor taskDescriptor = (TaskDescriptor) iterator.next();
            if (!taskDescriptor.isFinished())
            {
                liveTaskCount++;
            }
            final TaskDescriptorBean taskDescriptorBean = new TaskDescriptorBean(taskDescriptor, authenticationContext.getI18nHelper(),
                outlookDateManager, authenticationContext.getUser());
            allTasks.add(taskDescriptorBean);
        }
        this.liveTaskCount = liveTaskCount;
    }

    /** @return a Collection of {@link com.atlassian.jira.web.bean.TaskDescriptorBean} from the TaskManager */
    public Collection /* TaskDescriptorBean */getAllTasks()
    {
        return allTasks;
    }

    /**
     * @return how many tasks are currently live
     */
    public boolean isHasLiveTasks()
    {
        return liveTaskCount > 0;
    }

    /**
     * Returns the URL to allow a task to be acknowledged and then have it return back to the task admin page
     *
     * @param task the task in question
     * @return the URL that will acknowledge the task and return to here
     */
    public String getAcknowledgementURL(final TaskDescriptorBean task)
    {
        final String contextPath = ActionContext.getRequest().getContextPath();
        final StringBuffer sb = new StringBuffer();
        sb.append(contextPath);
        sb.append("/");
        sb.append("secure/admin/jira/AcknowledgeTask.jspa?taskId=");
        sb.append(task.getTaskId());
        sb.append("&destinationURL=");
        sb.append("secure/admin/jira/TaskAdmin.jspa");
        return sb.toString();
    }

}