package mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.util.JiraDateUtils;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

public class MockComment implements MutableComment
{
    private final Long id;
    private String author;
    private String updateAuthor;
    private String body;
    private String groupLevel;
    private Long roleLevelId;
    private Date created;
    private Date updated;
    private final Issue issue;
    public static final String COMMENT_ROLE_NAME = "My Role";
    public static final String COMMENT_ROLE_DESCRIPTION = "My Test Role";

    public MockComment(final String author, final String body)
    {
        this(author, body, null, null, null);
    }

    public MockComment(final String author, final String body, final String groupLevel, final Long roleLevelId)
    {
        this(author, body, groupLevel, roleLevelId, null);
    }

    public MockComment(final String author, final String body, final String groupLevel, final Long roleLevelId, final Date created)
    {
        this(null, author, body, groupLevel, roleLevelId, created, null);
    }

    public MockComment(final Long id, final String author, final String body, final String groupLevel, final Long roleLevelId, final Date created, final Issue issue)
    {
        this(id, author, null, body, groupLevel, roleLevelId, created, null, issue);
    }

    public MockComment(final Long id, final String author, final String updateAuthor, final String body, final String groupLevel, final Long roleLevelId, final Date created, final Date updated, final Issue issue)
    {
        this.id = id;
        this.author = author;
        this.updateAuthor = updateAuthor;
        this.body = body == null ? "" : body;
        this.groupLevel = groupLevel;
        this.roleLevelId = roleLevelId;
        final Date createdDate = JiraDateUtils.copyOrCreateDateNullsafe(created);
        this.created = createdDate;
        this.updated = (updated == null) ? createdDate : updated;
        this.issue = issue;
    }

    public String getUpdateAuthor()
    {
        return updateAuthor; //To change body of implemented methods use File | Settings | File Templates.
    }

    public com.opensymphony.user.User getUpdateAuthorObject()
    {
        return null;
    }

    @Override
    public User getUpdateAuthorUser()
    {
        return null;
    }

    public String getUpdateAuthorFullName()
    {
        return null;
    }

    public Date getUpdated()
    {
        return updated; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final MockComment comment = (MockComment) o;

        if (author != null ? !author.equals(comment.author) : comment.author != null)
        {
            return false;
        }
        if (!body.equals(comment.body))
        {
            return false;
        }
        if (created != null ? !created.equals(comment.created) : comment.created != null)
        {
            return false;
        }
        if (groupLevel != null ? !groupLevel.equals(comment.groupLevel) : comment.groupLevel != null)
        {
            return false;
        }
        if (!id.equals(comment.id))
        {
            return false;
        }
        if (!issue.equals(comment.issue))
        {
            return false;
        }
        if (roleLevelId != null ? !roleLevelId.equals(comment.roleLevelId) : comment.roleLevelId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("id", id).append("author", author).append("updateAuthor", updateAuthor).append("body", body).append(
            "groupLevel", groupLevel).append("roleLevelId", roleLevelId).append("created", created).append("updated", updated).append("issue", issue).toString();
    }

    public String getAuthor()
    {
        return author;
    }

    public com.opensymphony.user.User getAuthorObject()
    {
        return null;
    }

    @Override
    public User getAuthorUser()
    {
        return null;
    }

    public String getAuthorFullName()
    {
        return null;
    }

    public String getBody()
    {
        return body;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    public Long getId()
    {
        return id;
    }

    public Long getRoleLevelId()
    {
        return roleLevelId;
    }

    public ProjectRole getRoleLevel()
    {
        return roleLevelId == null ? null : new ProjectRoleImpl(roleLevelId, COMMENT_ROLE_NAME, COMMENT_ROLE_DESCRIPTION);
    }

    public Issue getIssue()
    {
        return issue;
    }

    public String getUdpateAuthor()
    {
        return updateAuthor;
    }

    public void setUdpateAuthor(final String udpateAuthor)
    {
        updateAuthor = udpateAuthor;
    }

    public void setAuthor(final String author)
    {
        this.author = author;
    }

    public void setBody(final String body)
    {
        this.body = body;
    }

    public void setCreated(final Date created)
    {
        this.created = created;
    }

    public void setGroupLevel(final String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public void setRoleLevelId(final Long roleLevelId)
    {
        this.roleLevelId = roleLevelId;
    }

    public void setUpdateAuthor(final String updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    public void setUpdated(final Date updated)
    {
        this.updated = updated;
    }

}
