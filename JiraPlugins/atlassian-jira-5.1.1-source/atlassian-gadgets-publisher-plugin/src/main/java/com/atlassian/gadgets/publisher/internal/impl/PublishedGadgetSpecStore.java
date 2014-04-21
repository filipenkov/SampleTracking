package com.atlassian.gadgets.publisher.internal.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.LocalGadgetSpecProvider;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.plugins.PluginGadgetSpec.Key;
import com.atlassian.gadgets.plugins.PluginGadgetSpecEventListener;
import com.atlassian.gadgets.publisher.internal.GadgetProcessor;
import com.atlassian.gadgets.publisher.internal.GadgetSpecValidator;
import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecNotFoundException;
import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecWriter;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.util.GadgetSpecUrlBuilder;
import com.atlassian.sal.api.ApplicationProperties;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * A fairly naive implementation that processes gadget spec file and substitutes values into it on the fly.
 */
public class PublishedGadgetSpecStore
    implements LocalGadgetSpecProvider, PluginGadgetSpecEventListener, PublishedGadgetSpecWriter, LifecycleAware
{
    private final Log log = LogFactory.getLog(getClass());

    private final PluginGadgetSpecStore store;

    // the plugin specs which have not been added to the store
    private final Map<PluginGadgetSpec.Key, PluginGadgetSpec> unprocessedSpecs = new ConcurrentHashMap<PluginGadgetSpec.Key, PluginGadgetSpec>();

    // this is accessed only in synchronized methods, so doesn't need to be volatile
    private boolean pluginSystemStarted = false;

    private final GadgetSpecUrlBuilder urlBuilder;
    private final PluginGadgetSpecProviderPermission permission;

    public PublishedGadgetSpecStore(GadgetSpecUrlBuilder urlBuilder,
        GadgetSpecValidator validator,
        GadgetProcessor gadgetProcessor,
        @Qualifier ("aggregatePermissions") PluginGadgetSpecProviderPermission permission,
        ApplicationProperties applicationProperties)
    {
        this.urlBuilder = checkNotNull(urlBuilder, "urlBuilder");
        this.permission = checkNotNull(permission, "permission");
        
        store = new PluginGadgetSpecStore(
            checkNotNull(gadgetProcessor, "gadgetProcessor"),
            checkNotNull(validator, "validator"),
            checkNotNull(applicationProperties, "applicationProperties")
        );
    }

    /**
     * Once the plugin system is fully started, we can process the gadgets, as all the plugins they depend upon will
     * have been loaded.
     */
    public synchronized void onStart()
    {
        pluginSystemStarted = true;
        for (PluginGadgetSpec spec : unprocessedSpecs.values()) {
            try
            {
                addToStore(spec);
            }
            catch (Exception e)
            {
                warn("Gadget spec " + spec + " could not be added to " + this + ", ignoring", e);
            }
        }
        unprocessedSpecs.clear();
    }

    private void warn(String message, Throwable t)
    {
        if (log.isDebugEnabled())
        {
            log.warn(message, t);
        }
        else
        {
            log.warn(message);
        }
    }

    Collection<PluginGadgetSpec> getAll()
    {
        return store.specs();
    }

    public boolean contains(URI gadgetSpecUri)
    {
        try
        {
            return getIfAllowed(gadgetSpecUri) != null;
        }
        catch (GadgetParsingException e)
        {
            return false;
        }
        catch (GadgetSpecUriNotAllowedException e)
        {
            // throw if the urlBuilder could not parse the uri
            return false;
        }
    }
    
    private PluginGadgetSpec getIfAllowed(URI gadgetSpecUri)
    {
        PluginGadgetSpec gadgetSpec = store.get(urlBuilder.parseGadgetSpecUrl(gadgetSpecUri.toASCIIString()));
        if (gadgetSpec == null || !allowed(gadgetSpec))
        {
            return null;
        }
        return gadgetSpec;
    }

    public Iterable<URI> entries()
    {
        return transform(filter(store.specs(), allowed()), toUri());
    }
    
    private Function<PluginGadgetSpec, URI> toUri()
    {
        return new PluginGadgetSpecToUri();
    }

    private class PluginGadgetSpecToUri implements Function<PluginGadgetSpec, URI>
    {
        public URI apply(PluginGadgetSpec from)
        {
            return getUri(from);
        }
    }
    
    private Predicate<PluginGadgetSpec> allowed()
    {
        return new AllowedPluginGadgetSpec();
    }
    
    private class AllowedPluginGadgetSpec implements Predicate<PluginGadgetSpec>
    {
        public boolean apply(PluginGadgetSpec spec)
        {
            return allowed(spec);
        }
    }

    private boolean allowed(PluginGadgetSpec spec)
    {
        return permission.voteOn(spec) != Vote.DENY;
    }

    private URI getUri(PluginGadgetSpec pluginGadgetSpec)
    {
        URI gadgetUri = URI.create(urlBuilder.buildGadgetSpecUrl(
            pluginGadgetSpec.getPluginKey(),
            pluginGadgetSpec.getModuleKey(),
            pluginGadgetSpec.getLocation())
        ).normalize();
        if (gadgetUri.isAbsolute())
        {
            throw new GadgetParsingException("Expected relative URI but got " + gadgetUri);
        }
        return gadgetUri;
    }

    public void writeGadgetSpecTo(String pluginKey, String location, OutputStream output) throws IOException
    {
        checkNotNull(location, "location");
        checkNotNull(output, "output");
        PluginGadgetSpec.Key key = new PluginGadgetSpec.Key(pluginKey, location);
        writeGadgetSpecTo(key, output);
    }

    public void writeGadgetSpecTo(URI gadgetSpecUri, OutputStream output) throws IOException
    {
        checkNotNull(gadgetSpecUri, "gadgetSpecUri");
        checkNotNull(output, "output");
        PluginGadgetSpec.Key key = urlBuilder.parseGadgetSpecUrl(gadgetSpecUri.toASCIIString());
        writeGadgetSpecTo(key, output);
    }
    
    public Date getLastModified(URI gadgetSpecUri)
    {
        PluginGadgetSpec pluginGadgetSpec = getIfAllowed(gadgetSpecUri);
        if (gadgetSpecUri == null)
        {
            throw new GadgetSpecUriNotAllowedException("Gadget at '" + gadgetSpecUri + "' does not exist or access is not allowed");
        }
        return pluginGadgetSpec.getDateLoaded();
    }

    private void writeGadgetSpecTo(PluginGadgetSpec.Key key, OutputStream output)
        throws IOException
    {
        PluginGadgetSpec pluginGadgetSpec = store.get(key);
        if (pluginGadgetSpec == null)
        {
            throw new PublishedGadgetSpecNotFoundException(
                String.format("Could not find gadget spec: %s", key));
        }
        write(pluginGadgetSpec, output);
    }

    private void write(PluginGadgetSpec pluginGadgetSpec, OutputStream output) throws IOException
    {
        output.write(store.getProcessedGadgetSpec(pluginGadgetSpec));
    }

    public synchronized void pluginGadgetSpecEnabled(PluginGadgetSpec pluginGadgetSpec) throws GadgetParsingException
    {
        checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
        if (pluginSystemStarted)
        {
            addToStore(pluginGadgetSpec);
        }
        else
        {
            unprocessedSpecs.put(pluginGadgetSpec.getKey(), pluginGadgetSpec);
        }
    }

    private void addToStore(PluginGadgetSpec pluginGadgetSpec) throws GadgetParsingException
    {
        if (pluginGadgetSpec.isHostedExternally())
        {
            // handled by PluginExternalGadgetSpecDirectoryEntryProvider in the directory plugin
            return;
        }
        store.put(pluginGadgetSpec.getKey(), pluginGadgetSpec);
    }

    public synchronized void pluginGadgetSpecDisabled(PluginGadgetSpec pluginGadgetSpec)
    {
        checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
        if (pluginSystemStarted)
        {
            if (pluginGadgetSpec.isHostedExternally())
            {
                // handled by PluginExternalGadgetSpecDirectoryEntryProvider in the directory plugin
                return;
            }
            store.remove(pluginGadgetSpec.getKey());
        }
        else
        {
            // I don't expect we'll ever be disabling plugins while starting up, but we'll make this
            // correct anyway
            unprocessedSpecs.remove(pluginGadgetSpec.getKey());
        }
    }
    
    @Override
    public String toString()
    {
        return "plugin-provided gadget spec store";
    }
    
    private static final class PluginGadgetSpecStore
    {
        private final Map<PluginGadgetSpec.Key, Entry> store = new ConcurrentHashMap<Key, Entry>();
        private final GadgetProcessor gadgetProcessor;
        private final GadgetSpecValidator validator;
        private final ApplicationProperties applicationProperties;

        public PluginGadgetSpecStore(GadgetProcessor gadgetProcessor,
                GadgetSpecValidator validator,
                ApplicationProperties applicationProperties)
        {
            this.gadgetProcessor = checkNotNull(gadgetProcessor, "gadgetProcessor");
            this.validator = checkNotNull(validator, "validator");
            this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        }

        public void remove(Key key)
        {
            store.remove(key);
        }

        public void put(Key key, PluginGadgetSpec pluginGadgetSpec)
        {
            Entry entry = new Entry(pluginGadgetSpec, new ProcessedGadgetSpecsCache(gadgetProcessor, pluginGadgetSpec));
            store.put(key, validate(entry));
        }

        public byte[] getProcessedGadgetSpec(PluginGadgetSpec pluginGadgetSpec) throws IOException
        {
            return store.get(pluginGadgetSpec.getKey()).get(applicationProperties.getBaseUrl());
        }

        public Collection<PluginGadgetSpec> specs()
        {
            return Collections2.transform(store.values(), toSpecs());
        }

        private Function<Entry, PluginGadgetSpec> toSpecs()
        {
            return EntryToGadgetSpec.FUNCTION;
        }
        
        private enum EntryToGadgetSpec implements Function<Entry, PluginGadgetSpec>
        {
            FUNCTION;
            
            public PluginGadgetSpec apply(Entry entry)
            {
                return entry.pluginGadgetSpec;
            }            
        }

        public PluginGadgetSpec get(Key key)
        {
            if (key == null)
            {
                return null;
            }
            Entry entry = store.get(key);
            if (entry == null)
            {
                return null;
            }
            return entry.pluginGadgetSpec;
        }
        
        private Entry validate(Entry entry)
        {
            ByteArrayInputStream bais;
            try
            {
                bais = new ByteArrayInputStream(entry.get(applicationProperties.getBaseUrl()));
            }
            catch (IOException e)
            {
                throw new GadgetParsingException(e);
            }
            if (!validator.isValid(bais))
            {
                throw new GadgetParsingException("plugin gadget '" + entry.pluginGadgetSpec.getKey() + "' failed validation");
            }
            return entry;
        }
        
        private static final class Entry
        {
            private final PluginGadgetSpec pluginGadgetSpec;
            private final ProcessedGadgetSpecsCache processedGadgetSpecsCache;

            public Entry(PluginGadgetSpec pluginGadgetSpec, ProcessedGadgetSpecsCache processedGadgetSpecsCache)
            {
                this.pluginGadgetSpec = pluginGadgetSpec;
                this.processedGadgetSpecsCache = processedGadgetSpecsCache;
            }

            public byte[] get(String baseUrl) throws IOException
            {
                try
                {
                    return processedGadgetSpecsCache.get(baseUrl);
                }
                catch (ComputationException ce)
                {
                    // we only expect an IOException from reading the gadget spec or a PublishedGadgetSpecNotFoundException (a
                    // RuntimeException) if the gadget spec could not be found.  If we find either, we rethrow.  Otherwise,
                    // we just rethrow the ComputationException
                    if (ce.getCause() instanceof IOException)
                    {
                        throw (IOException) ce.getCause();
                    }
                    if (ce.getCause() instanceof RuntimeException)
                    {
                        throw (RuntimeException) ce.getCause();
                    }
                    throw ce;
                }
            }
        }
        
        private static final class ProcessedGadgetSpecsCache
        {
            private final Map<String, byte[]> processedGadgetSpecs;
            
            public ProcessedGadgetSpecsCache(GadgetProcessor gadgetProcessor, PluginGadgetSpec pluginGadgetSpec)
            {
                processedGadgetSpecs = new MapMaker()
                    .weakValues()
                    .makeComputingMap(new ProcessedGadgetSpecFunction(gadgetProcessor, pluginGadgetSpec));
            }
    
            public byte[] get(String baseUrl)
            {
                return processedGadgetSpecs.get(baseUrl);
            }
        }
    
        private static final class ProcessedGadgetSpecFunction implements Function<String, byte[]>
        {
            private final GadgetProcessor gadgetProcessor;
            private final PluginGadgetSpec pluginGadgetSpec;
    
            public ProcessedGadgetSpecFunction(GadgetProcessor gadgetProcessor, PluginGadgetSpec pluginGadgetSpec)
            {
                this.gadgetProcessor = checkNotNull(gadgetProcessor, "gadgetProcessor");
                this.pluginGadgetSpec = checkNotNull(pluginGadgetSpec, "pluginGadgetSpec");
            }
    
            public byte[] apply(String baseUrl)
            {            
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    InputStream gadgetSpecStream = pluginGadgetSpec.getInputStream();
                    if (gadgetSpecStream == null)
                    {
                        throw new PublishedGadgetSpecNotFoundException(
                            String.format("Could not write gadget spec: %s because the resource was not found", pluginGadgetSpec));
                    }

                    BufferedInputStream in = new BufferedInputStream(gadgetSpecStream);
                    BufferedOutputStream out = new BufferedOutputStream(baos);
                    try
                    {
                        gadgetProcessor.process(in, out);
                        out.flush();
                    }
                    finally
                    {
                        closeQuietly(gadgetSpecStream);
                    }
                }
                catch (IOException ioe)
                {
                    throw new ComputationException(ioe);
                }
                return baos.toByteArray();
            }
        }
    }
}
