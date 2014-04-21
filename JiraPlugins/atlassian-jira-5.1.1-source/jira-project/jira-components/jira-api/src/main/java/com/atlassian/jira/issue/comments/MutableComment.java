package com.atlassian.jira.issue.comments;

import com.atlassian.annotations.PublicApi;

import java.util.Date;

/**
 * Represents a comment's in JIRA.
 * After calling any 'setter' method, you will need to call
 * {@link com.atlassian.jira.bc.issue.comment.CommentService#update} which does permission checking or
 * {@link CommentManager#update} which will just store the provided object, to persist the change to 
 * the database.
 */
@PublicApi
public interface MutableComment extends Comment
{
    public void setAuthor(String author);

    public void setBody(String body);

    public void setCreated(Date created);

    public void setGroupLevel(String groupLevel);

    public void setRoleLevelId(Long roleLevelId);

    public void setUpdateAuthor(String updateAuthor);

    public void setUpdated(Date updated);

}
