package com.atlassian.streams.jira;

import com.atlassian.jira.issue.comments.Comment;

import com.google.common.base.Function;

public final class Comments
{
    public static Function<Comment, String> getCommentBody()
    {
        return GetCommentBody.INSTANCE;
    }

    private enum GetCommentBody implements Function<Comment, String>
    {
        INSTANCE;

        public String apply(Comment c)
        {
            return c.getBody();
        }
    }
}
