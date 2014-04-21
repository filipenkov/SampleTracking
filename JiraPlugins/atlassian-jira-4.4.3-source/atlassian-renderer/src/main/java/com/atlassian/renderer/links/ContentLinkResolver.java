package com.atlassian.renderer.links;

import com.atlassian.renderer.RenderContext;
import java.text.ParseException;

/**
 * Resolves parsed content links into Link objects.  This interface can be used by hosting applications that wish
 * to allow their plugins to customise handling content links.  For example, a JIRA plugin could provide an
 * implementation that creates a Confluence link from the parsed information.
 */
public interface ContentLinkResolver
{
    /**
     * Given some parsed link text, convert it into a link object that can then be rendered into
     * the page.  The parseAsContentLink() method must have been called on the GenericLinkParser
     * object before being passed to this method.
     *
     * @param context The render context
     * @param parsedLink The parsed link information
     * @return the corresponding link. If no link can be created, null is returned
     */
    Link createContentLink(RenderContext context, GenericLinkParser parsedLink) throws ParseException;
}
