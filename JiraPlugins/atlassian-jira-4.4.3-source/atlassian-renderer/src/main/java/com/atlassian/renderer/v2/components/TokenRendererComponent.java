/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 1:44:57 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.util.RenderedContentToken;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

import java.util.regex.Matcher;

public class TokenRendererComponent extends AbstractRegexRendererComponent
{
    private static final RenderedContentToken BLOCK_TOKEN = RenderedContentStore.BLOCK_TOKEN;
    private static final RenderedContentToken INLINE_TOKEN = RenderedContentStore.INLINE_TOKEN;

    private final SubRenderer subRenderer;

    public TokenRendererComponent(final SubRenderer subRenderer)
    {
        this.subRenderer = subRenderer;
    }

    public boolean shouldRender(final RenderMode renderMode)
    {
        return renderMode.resolveTokens() && renderMode.tokenizes();
    }

    public String render(String wiki, final RenderContext context)
    {
        while (BLOCK_TOKEN.matches(wiki) || INLINE_TOKEN.matches(wiki))
        {
            wiki = regexRender(wiki, context, BLOCK_TOKEN.getPattern());
            wiki = regexRender(wiki, context, INLINE_TOKEN.getPattern());
        }
        return wiki;
    }

    public void appendSubstitution(final StringBuffer buffer, final RenderContext context, final Matcher matcher)
    {
        final Object obj = context.getRenderedContentStore().get(matcher.group(0));

        if (obj instanceof Renderable)
        {
            final Renderable renderable = (Renderable) obj;
            renderable.render(subRenderer, context, buffer);
        }
        else if (obj instanceof String)
        {
            buffer.append((String) obj);
        }
        else
        {
            throw new RuntimeException("Found object " + obj + " in token store?");
        }
    }
}