package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.DirectoryEntryProvider;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class DirectoryImpl implements Directory
{
    private static final Log log = LogFactory.getLog(DirectoryImpl.class);

    private final Iterable<? extends DirectoryEntryProvider> providers;

    public DirectoryImpl(Iterable<? extends DirectoryEntryProvider> providers)
    {
        this.providers = providers;
    }

    public Iterable<Entry> getEntries(GadgetRequestContext gadgetRequestContext)
    {
        return concat(transform(providers, providerEntries(gadgetRequestContext)));
    }

    public boolean contains(URI gadgetSpecUri)
    {
        return any(providers, providerContains(gadgetSpecUri));
    }

    private static Function<DirectoryEntryProvider, Iterable<Entry>> providerEntries(
            final GadgetRequestContext gadgetRequestContext)
    {
        return new Function<DirectoryEntryProvider, Iterable<Entry>>()
        {
            public Iterable<Entry> apply(DirectoryEntryProvider provider)
            {
                try
                {
                    return provider.entries(gadgetRequestContext);
                }
                catch (RuntimeException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.warn("Could not retrieve directory entries from " + provider, e);
                    }
                    else if (log.isWarnEnabled())
                    {
                        log.warn("Could not retrieve directory entries from " + provider + ": " + e.getMessage());
                    }
                    return ImmutableSet.of();
                }
            }
        };
    }

    private static Predicate<DirectoryEntryProvider> providerContains(final URI gadgetSpecUri)
    {
        return new Predicate<DirectoryEntryProvider>()
        {
            public boolean apply(DirectoryEntryProvider provider)
            {
                try
                {
                    return provider.contains(gadgetSpecUri);
                }
                catch (RuntimeException e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.warn("Could not determine whether " + provider + " contains " + gadgetSpecUri, e);
                    }
                    else if (log.isWarnEnabled())
                    {
                        log.warn("Could not determine whether " + provider + " contains " + gadgetSpecUri + ": "
                                 + e.getMessage());
                    }
                    return false;
                }
            }
        };
    }
}
