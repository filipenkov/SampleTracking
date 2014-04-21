package com.atlassian.jira.dev.functest.ao;

import net.java.ao.OneToMany;
import net.java.ao.RawEntity;
import net.java.ao.schema.AutoIncrement;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;

/**
 * @since v4.4
 */
public interface BlogAO extends RawEntity<Long>
{
    @AutoIncrement
    @NotNull
    @PrimaryKey ("ID")
    public long getID();

    @NotNull
    String getAuthor();
    void setAuthor(String author);

    @NotNull
    String getText();
    void setText(String text);

    @OneToMany
    CommentAO[] getComments();
}
