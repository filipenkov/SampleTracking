package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;

import java.util.Date;

public interface Comment
{
    /**
     * Get the user that created the comment
     * @return username of the user that created the comment
     */
    public String getAuthor();

    /**
     * Get the user that created the comment
     * @return a User object
     * @deprecated Use {@link #getAuthorUser()} instead. Since v4.4.
     */
    public com.opensymphony.user.User getAuthorObject();

    /**
     * Get the user that created the comment
     * @return a User object
     */
    public User getAuthorUser();

    public String getAuthorFullName();

    public String getBody();

    public Date getCreated();

    public String getGroupLevel();

    public Long getId();

    public Long getRoleLevelId();

    public ProjectRole getRoleLevel();

    public Issue getIssue();

    public String getUpdateAuthor();

    /**
     * Get the user that performed the update
     * @return a User object
     * @deprecated Use {@link #getUpdateAuthorUser()} instead. Since v4.4.
     */
    public com.opensymphony.user.User getUpdateAuthorObject();

    /**
     * Get the user that performed the update
     * @return a User object
     */
    public User getUpdateAuthorUser();

    public String getUpdateAuthorFullName();

    public Date getUpdated();

}
