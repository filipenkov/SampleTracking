package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.plugins.PluginGadgetSpecEventListener;
import com.atlassian.gadgets.spec.GadgetSpecFactory;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link com.atlassian.gadgets.directory.internal.DirectoryEntryProvider} for external gadget specs provided by
 * plugins.
 */
public class PluginExternalGadgetSpecDirectoryEntryProvider
    extends AbstractDirectoryEntryProvider<URI> implements PluginGadgetSpecEventListener
{
    private final Set<URI> entries = new HashSet<URI>();
    private final ReadWriteLock entriesLock = new ReentrantReadWriteLock();

    /**
     * Creates a new instance that uses the specified {@code GadgetSpecFactory} to retrieve and parse gadget specs.
     *
     * @param gadgetSpecFactory the {@code GadgetSpecFactory} to use to retrieve and parse gadget specs.  Must not be
     *                          {@code null} or a {@code NullPointerException} will be thrown.
     */
    public PluginExternalGadgetSpecDirectoryEntryProvider(GadgetSpecFactory gadgetSpecFactory, final ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory)
    {
        super(checkNotNull(gadgetSpecFactory, "gadgetSpecFactory"), checkNotNull(threadLocalDelegateExecutorFactory, "threadLocalDelegateExecutorFactory"));
    }

    public boolean contains(URI gadgetSpecUri)
    {
        entriesLock.readLock().lock();
        try
        {
            return entries.contains(gadgetSpecUri);
        }
        finally
        {
            entriesLock.readLock().unlock();
        }
    }

    public void pluginGadgetSpecEnabled(PluginGadgetSpec pluginGadgetSpec) throws GadgetParsingException
    {
        if (pluginGadgetSpec.isHostedExternally())
        {
            add(URI.create(pluginGadgetSpec.getLocation()));
        }
    }

    public void pluginGadgetSpecDisabled(PluginGadgetSpec pluginGadgetSpec)
    {
        if (pluginGadgetSpec.isHostedExternally())
        {
            remove(URI.create(pluginGadgetSpec.getLocation()));
        }
    }

    @Override
    protected Iterable<URI> internalEntries()
    {
        entriesLock.readLock().lock();
        try
        {
            return ImmutableSet.copyOf(entries);
        }
        finally
        {
            entriesLock.readLock().unlock();
        }
    }

    @Override
    protected Function<URI, Directory.Entry> convertToLocalizedDirectoryEntry(
            final GadgetRequestContext gadgetRequestContext)
    {
        return new Function<URI, Directory.Entry>()
        {
            public Directory.Entry apply(final URI gadgetSpecUri)
            {
                try
                {
                    return new GadgetSpecDirectoryEntry(
                        getGadgetSpec(gadgetSpecUri, gadgetRequestContext), false, null);
                }
                catch (GadgetParsingException e)
                {
                    return null;
                }
            }
        };
    }

    private void add(URI gadgetSpecUri) throws GadgetParsingException
    {
        entriesLock.writeLock().lock();
        try
        {
            if (!entries.contains(gadgetSpecUri))
            {
                validateGadgetSpec(gadgetSpecUri);
                entries.add(gadgetSpecUri);
            }
        }
        finally
        {
            entriesLock.writeLock().unlock();
        }
    }

    private void remove(URI gadgetSpecUri)
    {
        entriesLock.writeLock().lock();
        try
        {
            entries.remove(gadgetSpecUri);
        }
        finally
        {
            entriesLock.writeLock().unlock();
        }
    }

    private void validateGadgetSpec(URI gadgetSpecUri) throws GadgetParsingException
    {
        // fetch it and parse it to validate it. there will never be a request that we could get a locale and cache setting from here
        getGadgetSpec(gadgetSpecUri, GadgetRequestContext.NO_CURRENT_REQUEST);
    }

    @Override
    public String toString()
    {
        return "plugin-provided external gadget specs";
    }
}
