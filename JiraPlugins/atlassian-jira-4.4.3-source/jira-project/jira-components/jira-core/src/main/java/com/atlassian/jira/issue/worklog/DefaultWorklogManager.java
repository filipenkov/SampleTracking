package com.atlassian.jira.issue.worklog;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.TextAnalyzer;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;

import java.util.List;

public class DefaultWorklogManager implements WorklogManager
{
    private ProjectRoleManager projectRoleManager;
    private TextAnalyzer textAnalyzer;
    private final WorklogStore worklogStore;
    private final TimeTrackingIssueUpdater timeTrackingIssueUpdater;

    public DefaultWorklogManager(TextAnalyzer textAnalyzer, ProjectRoleManager projectRoleManager, WorklogStore worklogStore, TimeTrackingIssueUpdater timeTrackingIssueUpdater)
    {
        this.textAnalyzer = textAnalyzer;
        this.projectRoleManager = projectRoleManager;
        this.worklogStore = worklogStore;
        this.timeTrackingIssueUpdater = timeTrackingIssueUpdater;
    }

    @Override
    public boolean delete(com.opensymphony.user.User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        return delete((User) user, worklog, newEstimate, dispatchEvent);
    }

    public boolean delete(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, false);

        timeTrackingIssueUpdater.updateIssueOnWorklogDelete(user, worklog, newEstimate, dispatchEvent);

        return worklogStore.delete(worklog.getId());
    }

    @Override
    public Worklog update(com.opensymphony.user.User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        return update((User) user, worklog, newEstimate, dispatchEvent);
    }

    public Worklog create(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, true);

        Worklog newWorklog = worklogStore.create(worklog);
        // analyze and dispatch the content of the worklog comment so that we respect any trackbacks that may need to be handled.
        textAnalyzer.analyseContent(newWorklog.getIssue(), newWorklog.getComment());

        // Update the issues time tracking fields
        timeTrackingIssueUpdater.updateIssueOnWorklogCreate(user, newWorklog, newEstimate, dispatchEvent);

        return newWorklog;
    }

    public Worklog update(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklog(worklog, false);

        // We need to lookup the value as stored in the DB so that we can determine the original value of the
        // timeSpent so that we can correctly recalculate the issues total time spent field.
        Worklog originalWorklog = getById(worklog.getId());
        if(originalWorklog == null)
        {
            throw new IllegalArgumentException("Unable to find a worklog in the datastore for the provided id: '" + worklog.getId() + "'");
        }

        Long originalTimeSpent = originalWorklog.getTimeSpent();
        Worklog newWorklog = worklogStore.update(worklog);
        // analyze and dispatch the content of the worklog comment so that we respect any trackbacks that may need to be handled.
        textAnalyzer.analyseContent(newWorklog.getIssue(), newWorklog.getComment());

        // Update the issues time tracking fields
        timeTrackingIssueUpdater.updateIssueOnWorklogUpdate(user, originalWorklog, newWorklog, originalTimeSpent, newEstimate, dispatchEvent);

        return newWorklog;
    }

    @Override
    public Worklog create(com.opensymphony.user.User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        return create((User) user, worklog, newEstimate, dispatchEvent);
    }

    public Worklog getById(Long id)
    {
        return worklogStore.getById(id);
    }

    public List<Worklog> getByIssue(Issue issue)
    {
        if(issue == null)
        {
            throw new IllegalArgumentException("Cannot resolve worklogs for null issue.");
        }
        return worklogStore.getByIssue(issue);
    }

    public int swapWorklogGroupRestriction(String groupName, String swapGroup)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        if (swapGroup == null)
        {
            throw new IllegalArgumentException("You must provide a non null swap group name.");
        }

        return worklogStore.swapWorklogGroupRestriction(groupName, swapGroup);
    }

    public long getCountForWorklogsRestrictedByGroup(String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }
        return worklogStore.getCountForWorklogsRestrictedByGroup(groupName);
    }
    
    public ProjectRole getProjectRole(Long projectRoleId)
    {
        return projectRoleManager.getProjectRole(projectRoleId);
    }

    void validateWorklog(Worklog worklog, boolean create)
    {
        if(worklog == null)
        {
            throw new IllegalArgumentException("Worklog must not be null.");
        }

        if(worklog.getIssue() == null)
        {
            throw new IllegalArgumentException("The worklogs issue must not be null.");
        }

        if(!create && worklog.getId() == null)
        {
            throw new IllegalArgumentException("Can not modify a worklog with a null id.");
        }
    }

}
