package com.atlassian.renderer;

import com.atlassian.renderer.util.RenderedContentToken;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.Replacer;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Hides content from the renderer so that it will not be processed any further.
 * <p/>
 * <p>The RenderedContentStore associates blocks of rendered content (or Renderable objects that can later be
 * transformed into rendered content) with tokens. These tokens can then be substituted for the content blocks
 * during the rendering process. At the end of the chain of renderers, all tokens are replaced once more
 * with their associated content blocks.
 * <p/>
 * <p>Using this technique, it is possible to 'hide' content from the renderer, making it unnecessary to perform
 * nasty hacks to prevent the output of macros or renderer components from being broken by subsequent rendering
 * steps.
 */
public class RenderedContentStore
{
    private static Category log = Category.getInstance(RenderedContentStore.class);

    /**
     * An unlikely-to-appear-in-real-text token, for use in block-replacement
     */
    public static final RenderedContentToken BLOCK_TOKEN = new RenderedContentToken("block");

    /**
     * An unlikely-to-appear-in-real-text token, for use in inline content replacement
     */
    public static final RenderedContentToken INLINE_TOKEN = new RenderedContentToken("inline");

    /**
     * The key under which the store is placed in the render context
     */
    public static final String MAP_KEY = "RenderedContentStore";

    private final Store store = new Store();

    /**
     * Retrieve the store from the render context. If the store is not in the context, it
     * is created.
     *
     * @param renderContext the render context to retrieve the store from
     * @return the active store for this context
     */
    public static RenderedContentStore getFromRenderContext(final RenderContext renderContext)
    {
        return renderContext.getRenderedContentStore();
    }

    /**
     * Put some content block into the store. Returns a token that can be placed in
     * the page to be looked up later. The renderer knows not to draw things like
     * paragraphs around another block.
     *
     * @param content the content to add to the store
     * @return a token that the ContentReplaceFilter will be able to use to locate
     *         the content at a later date.
     */
    public String addBlock(final Object content)
    {
        return store.add(content, BLOCK_TOKEN);
    }

    /**
     * Put some inline content into the store. Returns a token that can be placed in
     * the page to be looked up later. Inline elements will be surrounded by paragraphs
     * and have newlines inserted between them as necessary.
     *
     * @param content the content to add to the store
     * @return a token that the ContentReplaceFilter will be able to use to locate
     *         the content at a later date.
     */
    public String addInline(final Object content)
    {
        return store.add(content, INLINE_TOKEN);
    }

    /**
     * Get some content back, given the entire token that was returned from a previous 'add'
     *
     * @param token a token that was returned from a previous add
     * @return the appropriate content, or null if the content could not be found for that
     *         token.
     */
    public Object get(final String token)
    {
        try
        {
            if ((token == null) || (token.length() < 3))
            {
                log.warn("Could not find stored token: the token was null or too short. A filter or macro may be broken.");
                return null;
            }

            return store.get(RenderedContentToken.getIndex(token));
        }
        catch (final Exception e)
        {
            log.warn("Could not find stored token. A filter or macro may be broken. Exception: " + e.getMessage());
            return null;
        }
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RenderedContentStore))
        {
            return false;
        }

        final RenderedContentStore renderedContentStore = (RenderedContentStore) o;

        if (store != null ? !store.equals(renderedContentStore.store) : renderedContentStore.store != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (store != null ? store.hashCode() : 0);
    }

    public static String stripTokens(String text)
    {
        text = new Replacer(BLOCK_TOKEN.getPattern(), "", new String[] {}).replaceAll(text);
        return new Replacer(INLINE_TOKEN.getPattern(), "", new String[] {}).replaceAll(text);
    }

    private final class Store
    {
        private final List list = new ArrayList();

        String add(final Object content, final RenderedContentToken delimiter)
        {
            if (!(content instanceof String) && !(content instanceof Renderable))
            {
                throw new RuntimeException("You can only store String and Renderable objects.");
            }

            list.add(content);
            return delimiter.token((list.size() - 1));
        }

        public Object get(final int index)
        {
            return list.get(index);
        }
    }
}
