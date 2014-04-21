package com.atlassian.renderer.embedded;

import com.atlassian.renderer.RenderContext;

/**
 * Currently we cannot convert the HTML used to show an embedded flash/movie file back into wiki format.
 * Hence we are using this place holder renderer to render a place holder image in place of the actual animation/movie
 * This will fix CONF-4415
 */
public class PlaceholderImageRenderer implements EmbeddedResourceRenderer
{
    public PlaceholderImageRenderer()
    {
    }

    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        return context.addRenderedContent("<img src=\"" + context.getSiteRoot() + "/images/icons/film.gif\" width=\"32\" height=\"32\" imagetext=\"" + resource.getOriginalLinkText() + "\"/>");
    }
}
