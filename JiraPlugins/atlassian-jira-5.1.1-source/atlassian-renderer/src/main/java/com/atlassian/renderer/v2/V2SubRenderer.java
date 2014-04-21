package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;

public class V2SubRenderer implements SubRenderer
{
    private Renderer renderer;

    public V2SubRenderer() {}
    
    public V2SubRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public String render(String wiki, RenderContext renderContext)
    {
        return renderer.render(wiki, renderContext);
    }

    public String renderAsText(String originalContent, RenderContext context)
    {
        return originalContent;
    }

    public String getRendererType()
    {
        return renderer.getRendererType();
    }

    public String render(String wiki, RenderContext renderContext, RenderMode newRenderMode)
    {
        try
        {
            if (newRenderMode != null)
            {
                renderContext.pushRenderMode(newRenderMode);
            }
            return renderer.render(wiki, renderContext);
        }
        finally
        {
            if (newRenderMode != null)
            {
                renderContext.popRenderMode();
            }
        }
    }
}
