package com.atlassian.renderer.v2;

import com.atlassian.renderer.*;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkRenderer;
import com.opensymphony.util.TextUtils;

/**
 * The facade class for the whole rendering system. This is what you call if you want to render something from
 * anywhere OUTSIDE the rendering subsystem. The facade is responsible for setting up the environment before
 * the renderer is invoked, if there is anything missing.
 * <p/>
 * Inside the rendering subsystem, you are more likely to call the V2Renderer directly (or the SubRenderer)
 */
public class V2RendererFacade implements WikiStyleRenderer
{
    private RendererConfiguration rendererConfiguration;
    private LinkRenderer defaultLinkRenderer;
    private EmbeddedResourceRenderer defaultEmbeddedRenderer;
    private Renderer renderer;

    public V2RendererFacade() {}
    
    public V2RendererFacade(RendererConfiguration rendererConfiguration, LinkRenderer defaultLinkRenderer, EmbeddedResourceRenderer defaultEmbeddedRenderer, Renderer renderer)
    {
        this.rendererConfiguration = rendererConfiguration;
        this.defaultLinkRenderer = defaultLinkRenderer;
        this.defaultEmbeddedRenderer = defaultEmbeddedRenderer;
        this.renderer = renderer;
    }

    public void setRendererConfiguration(RendererConfiguration rendererConfiguration)
    {
        this.rendererConfiguration = rendererConfiguration;
    }


    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
    }

    public String convertWikiToXHtml(RenderContext context, String wiki)
    {
        if (!TextUtils.stringSet(wiki))
        {
            return "";
        }

        if (context.getRenderMode() != null && context.getRenderMode().renderNothing())
        {
            return wiki;
        }

        initializeContext(context);

        return renderer.render(wiki, context);
    }

    public String convertWikiToText(RenderContext context, String wiki)
    {
        if (!TextUtils.stringSet(wiki))
        {
            return "";
        }

        if (context.getRenderMode().renderNothing())
        {
            return wiki;
        }

        initializeContext(context);

        return renderer.renderAsText(wiki, context);
    }

    public void setDefaultLinkRenderer(LinkRenderer linkRenderer)
    {
        this.defaultLinkRenderer = linkRenderer;
    }

    public void setDefaultEmbeddedRenderer(EmbeddedResourceRenderer embeddedRenderer)
    {
        this.defaultEmbeddedRenderer = embeddedRenderer;
    }

    private void initializeContext(RenderContext context)
    {
        if (context.getSiteRoot() == null)
        {
            context.setSiteRoot(rendererConfiguration.getWebAppContextPath());
        }

        if (context.getImagePath() == null)
        {
            context.setImagePath(context.getSiteRoot() + "/images");
        }

        if (context.getLinkRenderer() == null)
        {
            context.setLinkRenderer(defaultLinkRenderer);
        }

        if (context.getEmbeddedResourceRenderer() == null)
        {
            context.setEmbeddedResourceRenderer(defaultEmbeddedRenderer);
        }

        if (context.getCharacterEncoding() == null)
        {
            context.setCharacterEncoding(rendererConfiguration.getCharacterEncoding());
        }

    }
}