package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;

import java.util.Map;

/**
 * A simple macro to make large quoted blocks
 */
public class QuoteMacro extends BaseMacro
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
        return RenderMode.ALL;
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return false;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        return "<blockquote>" + body + "</blockquote>";
    }
}
