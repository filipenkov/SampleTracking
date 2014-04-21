package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraDateUtils;

import java.util.Date;

/**
 * Represents an issue worklog.<br>
 */
public class WorklogImpl implements Worklog
{
    private final WorklogManager worklogManager;

    private final Long id;
    private final String author;
    private final String updateAuthor;
    private final String comment;
    private final String groupLevel;
    private final Long roleLevelId;
    private final Date created;
    private final Date updated;
    private final Date startDate;
    private final Long timeSpent;
    private final Issue issue;

    public WorklogImpl(WorklogManager worklogManager, Issue issue, Long id, String author, String comment, Date startDate, String groupLevel, Long roleLevelId, Long timeSpent)
    {
        if (timeSpent == null)
        {
            throw new IllegalArgumentException("timeSpent must be set!");
        }
        this.worklogManager = worklogManager;
        this.author = author;
        this.updateAuthor = author;
        this.comment = comment;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        this.timeSpent = timeSpent;
        Date createDate = new Date();
        this.startDate = (startDate == null) ? createDate : startDate;
        this.created = createDate;
        this.updated = createDate;
        this.issue = issue;
        this.id = id;
    }

    public WorklogImpl(WorklogManager worklogManager, Issue issue, Long id, String author, String comment, Date startDate, String groupLevel, Long roleLevelId, Long timeSpent, String updateAuthor, Date created, Date updated)
    {
        if (timeSpent == null)
        {
            throw new IllegalArgumentException("timeSpent must be set!");
        }
        this.worklogManager = worklogManager;
        this.author = author;
        if(updateAuthor == null)
        {
            updateAuthor = this.author;
        }
        this.updateAuthor = updateAuthor;
        this.comment = comment;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        this.timeSpent = timeSpent;
        Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
        this.startDate = (startDate == null) ? createdDate : startDate;
        this.created = createdDate;
        this.updated = (updated == null) ? createdDate : updated;
        this.issue = issue;
        this.id = id;
    }

    public Long getId()
    {
        return this.id;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getAuthorFullName()
    {
        return getUserFullName(getAuthor());
    }

    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    public String getUpdateAuthorFullName()
    {
        return getUserFullName(getUpdateAuthor());
    }

    public Date getStartDate()
    {
        return JiraDateUtils.copyDateNullsafe(startDate);
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : worklogManager.getProjectRole(roleLevelId);
    }

    public String getComment()
    {
        return this.comment;
    }


    public Date getCreated()
    {
        return created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public Issue getIssue()
    {
        return issue;
    }

    private String getUserFullName(String username)
    {
        String fullName = null;
        if (username != null)
        {
            fullName = UserUtils.getUserEvenWhenUnknown(username).getDisplayName();
        }
        return fullName;
    }
}
