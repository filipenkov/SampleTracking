package com.atlassian.renderer;

import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;

import java.util.Map;

/**
 * A simple test macro that returns null for the body render mode.
 */
public class SimpleMacro extends BaseMacro
{
    public boolean isInline()
    {
        return false;
    }

    public boolean hasBody()
    {
        return true;
    }

    public RenderMode getBodyRenderMode()
    {
        return null;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        return body;
    }
}
