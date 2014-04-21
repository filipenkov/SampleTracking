package com.atlassian.jira.pageobjects.pages.viewissue.link.activity;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a comment in a JIRA issue.
 *
 * @since v5.0
 */
public class Comment
{
    private final String text;

    public Comment(String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("text", text).
                toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Comment comment = (Comment) o;

        if (text != null ? !text.equals(comment.text) : comment.text != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return text != null ? text.hashCode() : 0;
    }
}
