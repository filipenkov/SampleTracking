package com.atlassian.renderer.macro.macros;

import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.RenderContext;
import org.radeox.macro.parameter.MacroParameter;

import java.io.IOException;
import java.io.Writer;

public class PanelMacro extends AbstractPanelMacro
{
    private WikiStyleRenderer wikiStyleRenderer;

    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer)
    {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }

    public String getName()
    {
        return "panel";
    }

    protected String getPanelCSSClass()
    {
        return "panel";
    }

    protected String getPanelContentCSSClass()
    {
        return "panelContent";
    }

    protected String getPanelHeaderCSSClass()
    {
        return "panelHeader";
    }

    protected void writeContent(Writer writer, MacroParameter macroParameter, String content, String backgroundColor) throws IOException
    {
        String renderedContent = wikiStyleRenderer.convertWikiToXHtml((RenderContext)macroParameter.getContext().getParameters().get("RENDER_CONTEXT"), content.trim());

        super.writeContent(writer, macroParameter, renderedContent, backgroundColor);
    }
}
