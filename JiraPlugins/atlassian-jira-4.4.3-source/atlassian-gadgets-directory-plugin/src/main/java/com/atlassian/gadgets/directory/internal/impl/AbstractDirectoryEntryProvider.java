package com.atlassian.gadgets.directory.internal.impl;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.DirectoryEntryProvider;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractDirectoryEntryProvider<T> implements DirectoryEntryProvider, DisposableBean
{
    private final Log log = LogFactory.getLog(getClass());

    private final GadgetSpecFactory gadgetSpecFactory;
    private final ExecutorService executor;

    public AbstractDirectoryEntryProvider(GadgetSpecFactory gadgetSpecFactory, final ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory)
    {
        this.gadgetSpecFactory = gadgetSpecFactory;
        this.executor = threadLocalDelegateExecutorFactory.createExecutorService(
                Executors.newFixedThreadPool(15, new DirectoryEntryThreadFactory()));
    }

    public final Iterable<Directory.Entry> entries(final GadgetRequestContext gadgetRequestContext)
    {
        //TODO: We may want to refactor this with an AsyncCompleter in future when version 1.0 of atlassian-concurrent is available in the refapp/JIRA (AG-1345)
        final List<T> internalEntries = ImmutableList.copyOf(internalEntries());
        final CompletionService<Directory.Entry> completionService = new ExecutorCompletionService<Directory.Entry>(executor);
        for (final T internalEntry : internalEntries)
        {
            completionService.submit(new Callable<Directory.Entry>()
            {
                public Directory.Entry call() throws Exception
                {
                    return convertToLocalizedDirectoryEntry(gadgetRequestContext).apply(internalEntry);
                }
            });
        }

        final List<Directory.Entry> ret = new ArrayList<Directory.Entry>();
        for (int i = 0; i < internalEntries.size(); i++)
        {
            try
            {
                final Directory.Entry entry = completionService.take().get();
                if (entry != null)
                {
                    ret.add(entry);
                }
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                //convertToLocalized shouldn't throw any exceptions.  If this happened something weird must have gotten
                //through when trying to resolve a gadget!
                log.error("Unknown error resolving directory entry.", e);
            }
        }
        return ret;
    }

    protected abstract Iterable<T> internalEntries();

    protected abstract Function<T, Directory.Entry> convertToLocalizedDirectoryEntry(
            GadgetRequestContext gadgetRequestContext);

    protected final GadgetSpec getGadgetSpec(URI gadgetSpecUri, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException
    {
        return gadgetSpecFactory.getGadgetSpec(gadgetSpecUri, gadgetRequestContext);
    }

    //TODO: We should also replace this with a ThreadFactories.namedThreadFactory() (AG-1345)
    private static class DirectoryEntryThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);

        public Thread newThread(final Runnable runnable)
        {
            final Thread t = new Thread(runnable, "DirectoryEntryResolverThread-" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }

    public void destroy() throws Exception
    {
        executor.shutdown();
    }
}
