package com.atlassian.renderer;

import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.v2.RenderMode;
import org.apache.log4j.Category;

import java.util.*;

/**
 * Encapsulates the context in which some piece of content is being rendered. The RenderContext is initialised
 * by the renderer, and passed through every RendererComponent.
 *
 * <p>Components may manipulate the RenderContext - for example to change the render mode before passing the
 * context to some sub-component, but all components <b>must</b> ensure that they return the context to its
 * original state before passing control back to the Renderer.
 *
 * <p>The RenderContext also holds a reference to the {@link RenderedContentStore} that is to be used throughout
 * the rendering process.
 *
 * @see RenderedContentStore
 */
public class RenderContext implements RenderContextOutputType
{
    private static Category log = Category.getInstance(RenderContext.class);

    private Stack renderModes = new Stack();
    private RenderedContentStore store;

    /**
     * The base url. When prefixed to an application relativeUrl, an absolute URL is created.
     */
    private String baseUrl;

    private String siteRoot;
    private String imagePath;
    private String attachmentsPath;
    private String characterEncoding;
    private LinkRenderer linkRenderer;
    private EmbeddedResourceRenderer resourceRenderer;
    private boolean renderingForWysiwyg;
    private List externalReferences = new LinkedList();
    private Map parameters = new HashMap();

    private String outputType;

    /**
     * Construct a new render context in a default state.
     */
    public RenderContext()
    {
        this(new RenderedContentStore());
    }

    /**
     * The Confluence PageContext needs this so that sub-rendered pages can inherit the RenderedContentStore
     * of their parents.
     */
    protected RenderContext(RenderedContentStore store)
    {
        this.renderModes.push(RenderMode.ALL);
        this.store = (store == null) ? new RenderedContentStore() : store; 
    }

    /**
     * Get the current render mode. The renderer uses this mode to determine which components
     * should be run against the wiki text.
     *
     * @return the current RenderMode
     */
    public RenderMode getRenderMode()
    {
        if (renderModes.empty())
        {
            return RenderMode.ALL;
        }

        return (RenderMode) renderModes.peek();
    }

    /**
     * Push a new RenderMode onto the stack. This will become the new current render mode as
     * returned by {@link #getRenderMode}. If you call this method, you <b>must</b> also call
     * {@link #popRenderMode} once you have completed the operation that requires the new mode.
     *
     * @param renderMode the new current render mode
     */
    public void pushRenderMode(RenderMode renderMode)
    {
        renderModes.push(renderMode);
    }

    /**
     * Return to the render mode that was current before {@link #pushRenderMode} was last called
     *
     * @return the render mode that was just popped off the stack
     */
    public RenderMode popRenderMode()
    {
        if (renderModes.empty())
        {
            log.warn("Render mode stack is empty!", new Exception("Render mode stack is empty"));
            return RenderMode.ALL;
        }

        return (RenderMode) renderModes.pop();
    }

    /**
     * Get this rendering's RenderedContentStore
     *
     * @return this rendering's RenderedContentStore
     */
    public RenderedContentStore getRenderedContentStore()
    {
        return store;
    }

    /**
     * Convenience method so people don't have to keep retrieving the renderedcontentstore
     *
     * @param content the content to store
     * @return the token replacement
     */
    public String addRenderedContent(Object content)
    {
        return store.addBlock(content);
    }

    /**
     * Get the URL path to the image directory for this rendering. The path to images may be different
     * depending on the context of the rendering: for example, an HTML export will have images in a different
     * place to the online website.
     *
     * @return the URL path to the root of the image directory. Do not add a trailing "/".
     */
    public String getImagePath()
    {
        return imagePath;
    }

    /**
     * Set the URL path to the image directory for this rendering.  The path to images may be different
     * depending on the context of the rendering: for example, an HTML export will have images in a different
     * place to the online website.
     *
     * @param imagePath the URL path to the root of the image directory. No trailing "/".
     */
    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    /**
     * Set the root URL of the site on which the rendering is occurring.
     *
     * @param siteRoot the root URL of the site on which the rendering is occurring
     */
    public void setSiteRoot(String siteRoot)
    {
        this.siteRoot = siteRoot;
    }

    /**
     * Get the root URL of the site on which the rendering is occurring
     *
     * @return siteRoot the root URL of the site on which the rendering is occurring
     */
    public String getSiteRoot()
    {
        return siteRoot;
    }

    /**
     * Set the link renderer for this rendering run (exports might need different link
     * rendering behaviour, for example);
     *
     * @param linkRenderer the link renderer to use for this run
     */
    public void setLinkRenderer(LinkRenderer linkRenderer)
    {
        this.linkRenderer = linkRenderer;
    }

    /**
     * Retrieve the link renderer for this rendering run
     *
     * @return linkRenderer the link renderer to use for this run
     */
    public LinkRenderer getLinkRenderer()
    {
        return linkRenderer;
    }

    /**
     * Set the embedded resource renderer for this rendering run.
     *  
     * @param renderer
     */
    public void setEmbeddedResourceRenderer(EmbeddedResourceRenderer renderer)
    {
        this.resourceRenderer = renderer;
    }

    /**
     * Retrieve the embedded resource renderer for this rendering run.
     *
     * @return
     */
    public EmbeddedResourceRenderer getEmbeddedResourceRenderer()
    {
        return this.resourceRenderer;
    }

    public String getAttachmentsPath()
    {
        return attachmentsPath;
    }

    public void setAttachmentsPath(String attachmentsPath)
    {
        this.attachmentsPath = attachmentsPath;
    }

    /**
     * @see Object#equals
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RenderContext))
        {
            return false;
        }

        final RenderContext renderContext = (RenderContext) o;

        if (imagePath != null ? !imagePath.equals(renderContext.imagePath) : renderContext.imagePath != null)
        {
            return false;
        }
        if (renderModes != null ? !renderModes.equals(renderContext.renderModes) : renderContext.renderModes != null)
        {
            return false;
        }
        if (store != null ? !store.equals(renderContext.store) : renderContext.store != null)
        {
            return false;
        }

        return true;
    }

    /**
     * @see Object#hashCode
     */
    public int hashCode()
    {
        int result;
        result = (renderModes != null ? renderModes.hashCode() : 0);
        result = 29 * result + (store != null ? store.hashCode() : 0);
        result = 29 * result + (imagePath != null ? imagePath.hashCode() : 0);
        return result;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieve the confluence instances base url, useful when generating absolute references.
     * @return
     */
    public String getBaseUrl()
    {
        return this.baseUrl;
    }

    public boolean isRenderingForWysiwyg()
    {
        return renderingForWysiwyg;
    }

    public void setRenderingForWysiwyg(boolean renderingForWysiwyg)
    {
        this.renderingForWysiwyg = renderingForWysiwyg;
    }

    public void addExternalReference(Link link)
    {
        if (!externalReferences.contains(link))
        {
            externalReferences.add(link);
        }
    }

    public List getExternalReferences()
    {
        return externalReferences;
    }

    public String getCharacterEncoding()
    {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding)
    {
        this.characterEncoding = characterEncoding;
    }
    
    public Map getParams()
    {
        return parameters;
    }

    public void addParam(Object key, Object value)
    {
        parameters.put(key, value);
    }

    public Object getParam(Object key)
    {
        return parameters.get(key);
    }

    /**
     * Returns the output type that is configured for the PageContext
     *
     * @return The current output type
     * @see com.atlassian.renderer.RenderContextOutputType
     */
    public String getOutputType()
    {
        // If the output type hasn't been set, default to DISPLAY
        if (outputType == null)
            return DISPLAY;
        else
            return outputType;
    }

    public void setOutputType(String outputType)
    {
        this.outputType = outputType;
    }
}
