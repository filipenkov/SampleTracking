package com.atlassian.gadgets.renderer.internal.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import com.atlassian.gadgets.LocalGadgetSpecProvider;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.SpecParserException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An alternate implementation of Shindig's {@code GadgetSpecFactory} implementation that is capable of retrieving local
 * gadget specs without performing an HTTP loopback request.
 *
 * @since 1.1
 */
@Singleton
public class LocalGadgetSpecFactory implements GadgetSpecFactory
{
    private final Parser parser;
    private final Cache cache;
    private final GadgetSpecFactory fallback;

    @Inject
    public LocalGadgetSpecFactory(Iterable<LocalGadgetSpecProvider> providers,
                                  @Named("fallback") GadgetSpecFactory fallback)
    {
        this.parser = new Parser(checkNotNull(providers, "providers"));
        this.cache = new Cache(parser);
        this.fallback = checkNotNull(fallback, "fallback");
    }

    /* The following is ripped out of Shindig's DefaultGadgetSpecFactory */
    static final String RAW_GADGETSPEC_XML_PARAM_NAME = "rawxml";
    static final Uri RAW_GADGET_URI = Uri.parse("http://localhost/raw.xml");

    public GadgetSpec getGadgetSpec(GadgetContext context) throws GadgetException
    {
        String rawxml = context.getParameter(RAW_GADGETSPEC_XML_PARAM_NAME);
        if (rawxml != null)
        {
            // Set URI to a fixed, safe value (localhost), preventing a gadget rendered
            // via raw XML (eg. via POST) to be rendered on a locked domain of any other
            // gadget whose spec is hosted non-locally.
            return new GadgetSpec(RAW_GADGET_URI, rawxml);
        }
        return getGadgetSpec(context.getUrl(), context.getIgnoreCache());
    }

    public GadgetSpec getGadgetSpec(URI gadgetUri, boolean ignoreCache) throws GadgetException
    {
        GadgetSpec spec;
        if (ignoreCache)
        {
            spec = parser.get(gadgetUri);
        }
        else
        {
            spec = cache.get(gadgetUri);
        }
        if (spec != null)
        {
            return spec;
        }
        return fallback.getGadgetSpec(gadgetUri, ignoreCache);
    }

    private static final class CacheableGadgetSpec extends GadgetSpec
    {
        private final static Log log = LogFactory.getLog(CacheableGadgetSpec.class);

        private final URI uri;
        private final LocalGadgetSpecProvider provider;
        private final Date cachedAt = new Date();

        public CacheableGadgetSpec(LocalGadgetSpecProvider provider, URI uri, String xml) throws SpecParserException
        {
            super(Uri.fromJavaUri(uri), xml);
            this.provider = checkNotNull(provider, "provider");
            this.uri = checkNotNull(uri, "uri");
        }

        public boolean isExpired()
        {
            try
            {
                return provider.getLastModified(uri).after(cachedAt);
            }
            catch (RuntimeException e)
            {
                // Probably because the service proxy became unavailable
                if (log.isDebugEnabled())
                {
                    log.debug("Could not determine whether " + provider + " contains " + uri, e);
                }
                return true;
            }
        }
    }

    private static final class Cache
    {
        private final Parser parser;
        private final Map<URI, CacheableGadgetSpec> cache = new MapMaker().softValues().makeMap();

        public Cache(Parser parser)
        {
            this.parser = checkNotNull(parser, "parser");
        }

        public CacheableGadgetSpec get(URI gadgetUri) throws GadgetException
        {
            CacheableGadgetSpec spec = cache.get(gadgetUri);
            if (spec != null && !spec.isExpired())
            {
                return spec;
            }
            spec = parser.get(gadgetUri);
            if (spec == null)
            {
                return null;
            }
            cache.put(gadgetUri, spec);
            return spec;
        }
    }

    private static final class Parser
    {
        private final static Log log = LogFactory.getLog(Parser.class);

        private final Iterable<LocalGadgetSpecProvider> providers;

        public Parser(Iterable<LocalGadgetSpecProvider> providers)
        {
            this.providers = providers;
        }

        public CacheableGadgetSpec get(URI gadgetUri) throws GadgetException
        {
            for (LocalGadgetSpecProvider provider : providers)
            {
                try
                {
                    if (!provider.contains(gadgetUri))
                    {
                        continue;
                    }
                }
                catch (RuntimeException e)
                {
                    // Probably because the service proxy became unavailable
                    if (log.isDebugEnabled())
                    {
                        log.debug("Could not determine whether " + provider + " contains " + gadgetUri, e);
                    }
                    continue;
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try
                {
                    provider.writeGadgetSpecTo(gadgetUri, output);
                }
                catch (RuntimeException e)
                {
                    // Probably because the service proxy became unavailable or a gadget plugin was uninstalled after the
                    // "provider.contains(gadgetUri)" check from above returned.
                    if (log.isDebugEnabled())
                    {
                        log.warn("Could not retrieve gadget spec " + gadgetUri + " from " + provider, e);
                    }
                    else if (log.isWarnEnabled())
                    {
                        log.warn(
                            "Could not retrieve gadget spec " + gadgetUri + " from " + provider + ": " + e.getMessage());
                    }
                    continue;
                }
                catch (IOException e)
                {
                    throw new GadgetException(GadgetException.Code.FAILED_TO_RETRIEVE_CONTENT, e);
                }
                /*
                 * Shindig requires spec XML to be passed in as a string. This isn't ideal, since it could contain an XML
                 * banner that specifies an encoding, but in practice it shouldn't be a huge problem since when reading
                 * specs it ignores the banner and just uses whatever encoding was specified by the HTTP Content-Type
                 * header, or auto-detects it if nothing was specified explicitly.
                 *
                 * We also mis-handle encoding in GadgetProcessorImpl. I'm hoping that by using UTF-8 here it will at least
                 * preserve all of the characters from the original spec, but we need to test with some more exotic
                 * encodings. TODO AG-361
                 */
                try
                {
                    return new CacheableGadgetSpec(provider, gadgetUri, new String(output.toByteArray(), "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new AssertionError("UTF-8 encoding is required by the Java specification");
                }
            }
            return null;
        }
    }
}
