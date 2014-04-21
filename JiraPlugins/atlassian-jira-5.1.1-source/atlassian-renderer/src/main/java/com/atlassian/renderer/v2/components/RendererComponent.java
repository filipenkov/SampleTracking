/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 3:31:40 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;

public interface RendererComponent
{
    /**
     * Whether or not this render component is required given the current render mode.
     */
    boolean shouldRender(RenderMode renderMode);

    /**
     * Render this piece of wiki text.
     */
    String render(String wiki, RenderContext context);
}