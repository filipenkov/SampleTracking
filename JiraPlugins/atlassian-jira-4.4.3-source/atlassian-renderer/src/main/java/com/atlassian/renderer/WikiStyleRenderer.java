package com.atlassian.renderer;

/**
 * Component for converting wiki text into HTML markup.
 *
 * <p>The WikiStyleRenderer is a facade on top of the entire rendering subsystem
 */
public interface WikiStyleRenderer
{
    /**
     * Convert a given piece of wiki text into HTML markup.
     *
     * @param context the context in which to render the text
     * @param wiki the text to render
     * @return the HTML text produced by the rendering subsystem
     */
    String convertWikiToXHtml(RenderContext context, String wiki);
}
