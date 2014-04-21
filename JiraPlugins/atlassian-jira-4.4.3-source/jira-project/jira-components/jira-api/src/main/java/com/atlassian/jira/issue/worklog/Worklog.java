package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;

import java.util.Date;

/**
 * Represents an issue worklog.<br>
 */
public interface Worklog
{
    public Long getId();

    public String getAuthor();

    public String getAuthorFullName();

    public String getUpdateAuthor();

    public String getUpdateAuthorFullName();

    public Date getStartDate();

    public Long getTimeSpent();

    public String getGroupLevel();

    public Long getRoleLevelId();

    public ProjectRole getRoleLevel();

    public String getComment();

    public Date getCreated();

    public Date getUpdated();

    public Issue getIssue();

}
