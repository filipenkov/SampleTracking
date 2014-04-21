package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.RenderContext;

import java.util.Map;

public class NoformatMacro extends AbstractPanelMacro
{
    public NoformatMacro()
    {
    }

    public NoformatMacro(V2SubRenderer subRenderer)
    {
        setSubRenderer(subRenderer);
    }

    protected String getPanelCSSClass()
    {
        return "preformatted panel";
    }

    protected String getPanelHeaderCSSClass()
    {
        return "preformattedHeader panelHeader";
    }

    protected String getPanelContentCSSClass()
    {
        return "preformattedContent panelContent";
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        if ("true".equals(parameters.get("nopanel")))
            return getBodyContent(parameters, body, renderContext);
        else
            return super.execute(parameters, body, renderContext);
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.allow(RenderMode.F_HTMLESCAPE);
    }

    protected String getBodyContent(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        if (body.startsWith("\n"))
            body = body.substring(1);

        if (body.startsWith("\r\n"))
            body = body.substring(2);

        return super.getBodyContent(parameters, "<pre>" + body + "</pre>", renderContext);
    }

    public boolean suppressMacroRenderingDuringWysiwyg()
    {
        return false;
    }
}
