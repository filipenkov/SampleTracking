package com.atlassian.renderer.v2;

import com.atlassian.mail.HtmlToTextConverter;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

import java.io.IOException;
import java.util.List;

public class V2Renderer implements MutableRenderer
{
    public static final Category log = Category.getInstance(V2Renderer.class);
    public static final String RENDERER_TYPE = "atlassian-wiki-renderer";

    private RendererComponent[] components = new RendererComponent[0];

    public V2Renderer() {}

    public V2Renderer(List components)
    {
        this.components = (RendererComponent[]) components.toArray(new RendererComponent[components.size()]);
    }

    public void setComponents(List components)
    {
        this.components = (RendererComponent[]) components.toArray(new RendererComponent[components.size()]);
    }

    public String render(String wiki, RenderContext renderContext)
    {
        try
        {
            if (!TextUtils.stringSet(wiki))
            {
                return "";
            }

            if (renderContext.getRenderMode().renderNothing())
            {
                return wiki;
            }

            String renderedWiki = wiki;

            for (int i = 0; i < components.length; i++)
            {
                RendererComponent rendererComponent = components[i];
                RenderMode renderMode = renderContext.getRenderMode();
                if (rendererComponent.shouldRender(renderMode))
                {
                    renderedWiki = rendererComponent.render(renderedWiki, renderContext);
                }
            }

            return renderedWiki;
        }
        catch (Throwable t)
        {
            log.error("Unable to render content due to system error: " + t.getMessage(), t);
            return RenderUtils.error("Unable to render content due to system error: " + t.getMessage());
        }
    }

    public String renderAsText(String originalContent, RenderContext context)
    {
        try
        {
            return new HtmlToTextConverter().convert(render(originalContent, context));
        }
        catch (IOException e)
        {
            return originalContent;
        }
    }

    public String getRendererType()
    {
        return RENDERER_TYPE;
    }
}
