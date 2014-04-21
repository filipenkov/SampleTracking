package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.UrlLink;
import com.atlassian.renderer.v2.components.HtmlEscaper;

/**
 * Simple {@link UrlLink} that escapes the URL before while being constructed.
 *
 * Implemented to fix JRA-15812. There is logic like this in the Atlassian renderer 4.1,
 * but we did not want to upgrade the library on branch.
 *
 * @since v3.13
 */
class JiraUrlLink extends UrlLink
{
    JiraUrlLink(final GenericLinkParser parser)
    {
        super(parser);
        this.url = HtmlEscaper.escapeAll(this.url, true);
    }
}
