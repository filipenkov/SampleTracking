package com.atlassian.renderer.v2.macro;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

import java.util.Map;

public class ResourceAwareMacroDecorator extends BaseMacro implements ResourceAware
{
    private Macro macro;
    private String resourcePath;

    public ResourceAwareMacroDecorator(Macro macro)
    {
        this.macro = macro;
    }

    public String getResourcePath()
    {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath)
    {
        this.resourcePath = resourcePath;
    }

    public boolean isInline()
    {
        return macro.isInline();
    }

    public boolean hasBody()
    {
        return macro.hasBody();
    }

    public RenderMode getBodyRenderMode()
    {
        return macro.getBodyRenderMode();
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        return macro.execute(parameters, body, renderContext);
    }

    public Macro getMacro()
    {
        return macro;
    }

    public boolean suppressSurroundingTagDuringWysiwygRendering()
    {
        return macro.suppressSurroundingTagDuringWysiwygRendering();
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return macro.suppressMacroRenderingDuringWysiwyg();
    }
}
