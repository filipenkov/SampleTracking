package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.JiraDateUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Represents a comment on an issue by a user.
 * Comment is essentially a GenericValue wrapper with getters
 *
 * @see CommentManager#getCommentsForUser(com.atlassian.jira.issue.Issue, com.opensymphony.user.User)
 */
public class CommentImpl implements MutableComment
{
    private final CommentManager manager;

    private Long id;
    private String author;
    private String body;
    private String groupLevel;
    private Long roleLevelId;
    private Date created;
    private Issue issue;
    private String updateAuthor;
    private Date updated;

    /**
     * Creates a new instance of this class. The constructor id package protected in order to allow only
     * the DefaultCommentManager to create new instances (a.k.a. comment factory)
     *
     * @param manager      comment manager
     * @param author       user name of the author, required
     * @param updateAuthor user name of the author that has last updated
     * @param body         body of the comment, required
     * @param groupLevel   group visibility level
     * @param roleLevelId  role ID visibility level
     * @param created      created date, set to new Date if null
     * @param updated      updated date, set to created Date if null
     * @param issue        related issue
     * @throws IllegalArgumentException if ivalid data was passed
     */
    public CommentImpl(CommentManager manager, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, Issue issue)
            throws IllegalArgumentException
    {
        if (StringUtils.isNotBlank(groupLevel) && (roleLevelId != null))
        {
            throw new IllegalArgumentException("Cannot specify both grouplevel and rolelevel comment visibility");
        }

        this.manager = manager;

        this.author = author;
        this.updateAuthor = updateAuthor;
        this.body = body == null ? "" : body;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
        this.created = createdDate;
        this.updated = (updated == null) ? createdDate : updated;
        this.issue = issue;
    }

    public String getAuthor()
    {
        return author;
    }

    public com.opensymphony.user.User getAuthorObject()
    {
        return OSUserConverter.convertToOSUser(getAuthorUser());
    }

    @Override
    public User getAuthorUser()
    {
        return UserUtils.getUserEvenWhenUnknown(getAuthor());
    }

    public String getAuthorFullName()
    {
        final String author = getAuthor();
        return author == null ? null : getFullNameForUsername(author);
    }

    private String getFullNameForUsername(String author)
    {
        return UserUtils.getUserEvenWhenUnknown(author).getDisplayName();
    }

    public String getBody()
    {
        return body;
    }

    /**
     * Returns a date when this comment was created. This is never null
     *
     * @return creation date
     */
    public Date getCreated()
    {
        // return a defensive copy
        return JiraDateUtils.copyDateNullsafe(created);
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public Long getId()
    {
        return id;
    }

    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    public com.opensymphony.user.User getUpdateAuthorObject()
    {
        return OSUserConverter.convertToOSUser(getUpdateAuthorUser());
    }

    @Override
    public User getUpdateAuthorUser()
    {
        return UserUtils.getUserEvenWhenUnknown(getUpdateAuthor());
    }

    public String getUpdateAuthorFullName()
    {
        final String updateAuthor = getUpdateAuthor();
        return updateAuthor == null ? null : getFullNameForUsername(updateAuthor);
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public void setGroupLevel(String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public void setRoleLevelId(Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public void setUpdateAuthor(String updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    public void setUpdated(Date updated)
    {
        this.updated = updated;
        // Don't set the modified
    }

    // NOTE: package protected in order to allow only the manager to call it after the inctance has been consturcted
    // and persisted
    void setId(Long id)
    {
        this.id = id;
    }

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : manager.getProjectRole(roleLevelId);
    }

    public Issue getIssue()
    {
        return issue;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof CommentImpl))
        {
            return false;
        }

        final CommentImpl comment = (CommentImpl) obj;

        if (id != null ? !id.equals(comment.id) : comment.id != null)
        {
            return false;
        }
        if (!author.equals(comment.author))
        {
            return false;
        }
        if (!created.equals(comment.created))
        {
            return false;
        }
        if (!body.equals(comment.body))
        {
            return false;
        }
        if (roleLevelId != null ? !roleLevelId.equals(comment.roleLevelId) : comment.roleLevelId != null)
        {
            return false;
        }
        if (groupLevel != null ? !groupLevel.equals(comment.groupLevel) : comment.groupLevel != null)
        {
            return false;
        }
        if (updateAuthor != null ? !updateAuthor.equals(comment.updateAuthor) : comment.updateAuthor != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(comment.updated) : comment.updated != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + author.hashCode();
        result = 29 * result + body.hashCode();
        result = 29 * result + (groupLevel != null ? groupLevel.hashCode() : 0);
        result = 29 * result + (roleLevelId != null ? roleLevelId.hashCode() : 0);
        result = 29 * result + created.hashCode();
        return result;
    }


}
