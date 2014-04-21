/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 28, 2004
 * Time: 1:34:28 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.link.LinkDecorator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseLinkRendererComponent extends AbstractRegexRendererComponent
{
    // (^|[^\p{Alpha}!\^]) -- non-alpha or line-beginning before the pattern-match
    //                     -- also don't match '!' - CONF-3923
    //                     -- also don't match '^' - CONF-3447
    // ([\p{Lu}]          -- match starting with an upper-case Unicode character
    // [\p{Alnum}]+       -- potentially any number of alphanumeric characters of any case
    // [\p{L}&&[^\p{Lu}]] -- but there must be _at least one_ lower-case character
    // [\p{Alnum}]*       -- potentially followed by more alphanumerics
    // [\p{Lu}]           -- followed by an upper-case character
    // [\p{Alnum}]+)      -- and more alphanumerics to the end.
    static final Pattern LINK_CAMELCASE_PATTERN = Pattern.compile("(^|[^\\p{Alpha}!\\^])([\\p{Lu}][\\p{Alnum}]*[\\p{L}&&[^\\p{Lu}]][\\p{Alnum}]*[\\p{Lu}][\\p{Alnum}]+)", Pattern.DOTALL);

    private LinkResolver linkResolver;
    private RendererConfiguration rendererConfiguration;

    public CamelCaseLinkRendererComponent(LinkResolver linkResolver, RendererConfiguration rendererConfiguration)
    {
        this.linkResolver = linkResolver;
        this.rendererConfiguration = rendererConfiguration;
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderLinks() && rendererConfiguration.isAllowCamelCase();
    }

    public String render(String wiki, RenderContext context)
    {
        if (rendererConfiguration.isAllowCamelCase())
        {
            return regexRender(wiki, context, LINK_CAMELCASE_PATTERN);
        }
        else
        {
            return wiki;
        }
    }

    public void appendSubstitution(StringBuffer stringBuffer, RenderContext context, Matcher matcher)
    {
        String linkText = matcher.group(2);
        stringBuffer.append(matcher.group(1)); // the bit before the camelcase link
        Link link = linkResolver.createLink(context, linkText);
        stringBuffer.append(context.getRenderedContentStore().addInline(new LinkDecorator(link)));
    }
}