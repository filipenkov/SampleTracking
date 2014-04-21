package com.atlassian.gadgets.renderer.internal.rewrite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.rewrite.lexer.DefaultContentRewriter;

@Singleton
public class AtlassianGadgetsContentRewriter extends DefaultContentRewriter
{

    @Inject
    public AtlassianGadgetsContentRewriter(GadgetSpecFactory specFactory,
                                           @Named("shindig.content-rewrite.include-urls")
                                           String includeUrls,
                                           @Named("shindig.content-rewrite.exclude-urls")
                                           String excludeUrls,
                                           @Named("shindig.content-rewrite.expires")
                                           String expires,
                                           @Named("shindig.content-rewrite.include-tags")
                                           String includeTags,
                                           @Named("shindig.content-rewrite.proxy-url")
                                           String proxyUrl,
                                           @Named("shindig.content-rewrite.concat-url")
                                           String concatUrl)
    {
        super(specFactory, includeUrls, excludeUrls, expires, includeTags, proxyUrl, concatUrl);
    }

    @Override
    protected String getProxyUrl()
    {
        // Since in DefaultContentRewriter, the proxyUrl is only used when it is
        // not null.  We force it to return null here.
        // Though content rewriting feature is disabled in AG, we made it always
        // null just to make sure things don't break when we turn it on in the 
        // future.
        return null;
    }

    @Override
    protected String getConcatUrl()
    {
        // Same reason as it is in getProxyUrl()
        return null;
    }
}
