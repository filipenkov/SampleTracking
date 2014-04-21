package com.atlassian.jira.plugin.issuenav;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.issuenav.event.KickassIssuesEvent;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects to the plugin's issue navigator servlet.
 *
 * @since v5.1
 */
public class IssueNavRewriteFilter extends AbstractHttpFilter
{

    private final EventPublisher eventPublisher;

    public IssueNavRewriteFilter(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        eventPublisher.publish(new KickassIssuesEvent());
        request.getRequestDispatcher("/secure/IssueNavAction!default.jspa").forward(request, response);
    }
}
