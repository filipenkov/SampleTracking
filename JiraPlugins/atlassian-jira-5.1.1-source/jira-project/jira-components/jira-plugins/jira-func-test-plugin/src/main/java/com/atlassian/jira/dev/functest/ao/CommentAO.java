package com.atlassian.jira.dev.functest.ao;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;

import java.util.Date;

/**
 * @since v4.4
 */
public interface CommentAO extends Entity
{
    @NotNull
    public String getComment();
    public void setComment(String comment);

    @NotNull
    public String getAuthor();
    public void setAuthor(String author);

    @NotNull
    public Date getDate();
    public void setDate(Date postedDate);

    public BlogAO getBlog();
    public void setBlog(BlogAO blog);
}
