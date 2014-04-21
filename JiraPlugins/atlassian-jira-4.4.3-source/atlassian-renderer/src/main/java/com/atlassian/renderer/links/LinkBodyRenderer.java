/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 7:00:53 PM
 */
package com.atlassian.renderer.links;

import com.atlassian.renderer.RenderContext;

/**
 * A simple little interface to allow us to plug renderers for link bodies.
 */
public interface LinkBodyRenderer
{
    /**
     * Render the body of a link.
     * @return Rendered HTML representing the body of this link.
     */
    String render(Link link, RenderContext context);
}