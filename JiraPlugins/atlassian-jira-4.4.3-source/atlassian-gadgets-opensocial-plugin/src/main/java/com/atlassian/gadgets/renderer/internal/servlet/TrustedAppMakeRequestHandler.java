package com.atlassian.gadgets.renderer.internal.servlet;

import com.atlassian.gadgets.renderer.internal.http.TrustedAppContentFetcherFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shindig.gadgets.rewrite.ContentRewriterRegistry;
import org.apache.shindig.gadgets.servlet.MakeRequestHandler;

@Singleton
public class TrustedAppMakeRequestHandler extends MakeRequestHandler
{
    @Inject
    public TrustedAppMakeRequestHandler(TrustedAppContentFetcherFactory contentFetcherFactory,
            ContentRewriterRegistry contentRewriterRegistry)
    {
        super(contentFetcherFactory, contentRewriterRegistry);
    }    
}
