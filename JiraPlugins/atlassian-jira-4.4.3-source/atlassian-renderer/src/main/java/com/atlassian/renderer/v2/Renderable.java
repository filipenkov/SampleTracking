/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 20, 2004
 * Time: 12:40:00 PM
 */
package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;

public interface Renderable
{
    void render(SubRenderer renderer, RenderContext context, StringBuffer buffer);
}