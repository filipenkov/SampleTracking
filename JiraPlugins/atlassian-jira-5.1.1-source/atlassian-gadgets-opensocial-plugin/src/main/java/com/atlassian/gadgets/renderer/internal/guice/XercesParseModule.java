package com.atlassian.gadgets.renderer.internal.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import org.apache.shindig.gadgets.parse.GadgetHtmlParser;
import org.apache.shindig.gadgets.parse.nekohtml.NekoSimplifiedHtmlParser;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;

/**
 * Replacement for the Shindig {@link org.apache.shindig.gadgets.parse.ParseModule}. This one provides a hard-coded
 * Xerces {@link DOMImplementation} instead of using hacky classloader lookups to resolve it.
 */
public class XercesParseModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(GadgetHtmlParser.class).to(NekoSimplifiedHtmlParser.class);
        bind(DOMImplementation.class).toProvider(XercesDOMImplementationProvider.class);
    }
    
    public static class XercesDOMImplementationProvider implements Provider<DOMImplementation>
    {
        public DOMImplementation get()
        {
            return DOMImplementationImpl.getDOMImplementation();
        }
    }
}
