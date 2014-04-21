package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.SessionKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to getting and setting {@link SearchRequest} objects in session.
 *
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager()
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 * @since v4.2
 */
@NonInjectableComponent
public class DefaultSessionSearchRequestManager extends AbstractSessionSearchObjectManager<SearchRequest>
        implements SessionSearchRequestManager
{
    public DefaultSessionSearchRequestManager(final HttpServletRequest request, final Session session)
    {
        super(request, session);
    }

    protected String getLastViewedSessionKey()
    {
        return SessionKeys.SEARCH_REQUEST;
    }
}
