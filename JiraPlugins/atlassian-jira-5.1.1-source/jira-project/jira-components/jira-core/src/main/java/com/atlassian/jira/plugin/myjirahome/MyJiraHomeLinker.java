package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Retrieves a link representing the current My JIRA Home. In case non is found, it falls back to {@link #DEFAULT_HOME}.
 *
 * @since 5.1
 */
public interface MyJiraHomeLinker
{
    String DEFAULT_HOME = "/secure/Dashboard.jspa";
    
    /**
     * Returns the My JIRA Home as a link for the given user.
     *
     * @param user the user for which the home link is requested
     * @return the user's My JIRA Home or {@link #DEFAULT_HOME} if none is defined or there were errors while loading.
     */
    @Nonnull
    String getHomeLink(@Nullable User user);
}
