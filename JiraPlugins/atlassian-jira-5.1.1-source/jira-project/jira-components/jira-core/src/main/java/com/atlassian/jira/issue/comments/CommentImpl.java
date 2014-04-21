package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraDateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;

/**
 * Represents a comment on an issue by a user.
 * Comment is essentially a GenericValue wrapper with getters
 *
 * @see CommentManager#getCommentsForUser(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
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

    @Override
    public String getAuthor()
    {
        return author;
    }

    @Override
    public User getAuthorUser()
    {
        return UserUtils.getUserEvenWhenUnknown(getAuthor());
    }

    @Override
    public String getAuthorFullName()
    {
        final String author = getAuthor();
        return author == null ? null : getFullNameForUsername(author);
    }

    private String getFullNameForUsername(String author)
    {
        return UserUtils.getUserEvenWhenUnknown(author).getDisplayName();
    }

    @Override
    public String getBody()
    {
        return body;
    }

    /**
     * Returns a date when this comment was created. This is never null
     *
     * @return creation date
     */
    @Override
    public Date getCreated()
    {
        // return a defensive copy
        return JiraDateUtils.copyDateNullsafe(created);
    }

    @Override
    public String getGroupLevel()
    {
        return groupLevel;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String getUpdateAuthor()
    {
        return updateAuthor;
    }

    @Override
    public User getUpdateAuthorUser()
    {
        return UserUtils.getUserEvenWhenUnknown(getUpdateAuthor());
    }

    @Override
    public String getUpdateAuthorFullName()
    {
        final String updateAuthor = getUpdateAuthor();
        return updateAuthor == null ? null : getFullNameForUsername(updateAuthor);
    }

    @Override
    public Date getUpdated()
    {
        return updated;
    }

    @Override
    public void setAuthor(String author)
    {
        this.author = author;
    }

    @Override
    public void setBody(String body)
    {
        this.body = body;
    }

    @Override
    public void setGroupLevel(String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    @Override
    public void setRoleLevelId(Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    @Override
    public void setCreated(Date created)
    {
        this.created = created;
    }

    @Override
    public void setUpdateAuthor(String updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    @Override
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

    @Override
    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    @Override
    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : manager.getProjectRole(roleLevelId);
    }

    @Override
    public Issue getIssue()
    {
        return issue;
    }

    @Override
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

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }


}
