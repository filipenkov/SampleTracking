package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecProvider;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.spec.GadgetSpecFactory;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.emptySet;

/**
 * A {@link com.atlassian.gadgets.directory.internal.DirectoryEntryProvider} that aggregates an arbitrary number of
 * {@link com.atlassian.gadgets.GadgetSpecProvider} implementations to produce its results.
 */
public class GadgetSpecProviderDirectoryEntryProvider extends AbstractDirectoryEntryProvider<URI>
{
    private final Log log = LogFactory.getLog(getClass());

    /*
     * This will be injected as an OSGi service reference proxy, which means it could potentially disappear at runtime,
     * or might not even get linked up in the first place, since this is an optional service the app might not have.
     * Ensure that anytime you dereference it, you catch ServiceUnavailableException and handle it appropriately.
     */
    private Iterable<GadgetSpecProvider> gadgetSpecProviders;

    public GadgetSpecProviderDirectoryEntryProvider(GadgetSpecFactory gadgetSpecFactory,
            Iterable<GadgetSpecProvider> gadgetSpecProviders,
            final ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory)
    {
        super(gadgetSpecFactory, threadLocalDelegateExecutorFactory);
        this.gadgetSpecProviders = gadgetSpecProviders;
    }

    public boolean contains(final URI gadgetSpecUri)
    {
        return any(gadgetSpecProviders, new Predicate<GadgetSpecProvider>()
        {
            public boolean apply(GadgetSpecProvider provider)
            {
                try
                {
                    return provider.contains(gadgetSpecUri);
                }
                catch (RuntimeException e)
                {
                    // Probably because the service proxy became unavailable
                    if (log.isDebugEnabled())
                    {
                        log.debug("Could not determine whether " + provider + " contains " + gadgetSpecUri, e);
                    }
                    return false;
                }
            }
        });
    }

    @Override
    protected Iterable<URI> internalEntries()
    {
        return concat(transform(gadgetSpecProviders, new Function<GadgetSpecProvider, Iterable<URI>>()
        {
            public Iterable<URI> apply(GadgetSpecProvider provider)
            {
                try
                {
                    return provider.entries();
                }
                catch (RuntimeException e)
                {
                    // Probably because the service proxy became unavailable
                    if (log.isDebugEnabled())
                    {
                        log.warn("Could not retrieve directory entries from " + provider, e);
                    }
                    else if (log.isWarnEnabled())
                    {
                        log.warn("Could not retrieve directory entries from " + provider + ": " + e.getMessage());
                    }
                    return emptySet();
                }
            }
        }));
    }

    @Override
    protected Function<URI, Directory.Entry> convertToLocalizedDirectoryEntry(
            final GadgetRequestContext gadgetRequestContext)
    {
        return new Function<URI, Directory.Entry>()
        {
            public Directory.Entry apply(URI gadgetSpecUri)
            {
                try
                {
                    return new GadgetSpecDirectoryEntry(getGadgetSpec(gadgetSpecUri, gadgetRequestContext), false, null);
                }
                catch (GadgetParsingException e)
                {
                    // Couldn't retrieve the gadget spec
                    if (log.isDebugEnabled())
                    {
                        log.debug("Couldn't retrieve " + gadgetSpecUri, e);
                    }
                    return null;
                }
            }
        };
    }


    @Override
    public String toString()
    {
        return "application-provided gadget specs";
    }
}
