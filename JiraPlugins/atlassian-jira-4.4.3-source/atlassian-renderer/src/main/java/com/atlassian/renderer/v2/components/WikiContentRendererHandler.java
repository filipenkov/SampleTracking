package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;

public class WikiContentRendererHandler implements WikiContentHandler
{
    private MacroRendererComponent macroRendererComponent;
    private RenderContext context;

    public WikiContentRendererHandler(MacroRendererComponent macroRendererComponent, RenderContext context)
    {
        this.macroRendererComponent = macroRendererComponent;
        this.context = context;
    }

    public void handleMacro(StringBuffer buffer, MacroTag macroTag, String body, boolean hasEndTag)
    {
        macroRendererComponent.makeMacro(buffer, macroTag, body, context, hasEndTag);
    }

    public void handleText(StringBuffer buffer, String s)
    {
        buffer.append(s);
    }
}
