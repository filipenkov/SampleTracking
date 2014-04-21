package com.atlassian.jira.portal.gadgets;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Eagerly caching implementation on the external Gadget store.  The contains() method needs to be especially
 * performant, since every gadget will be checked when displaying a dashboard.
 *
 * @since v4.0
 */
public class CachingExternalGadgetStore implements ExternalGadgetStore, Startable
{
    public final Map<ExternalGadgetSpecId, ExternalGadgetSpec> specCache = new ConcurrentHashMap<ExternalGadgetSpecId, ExternalGadgetSpec>();
    public final Set<URI> uriCache = new CopyOnWriteArraySet<URI>();
    private final ExternalGadgetStore delegateStore;
    private final EventPublisher eventPublisher;

    public CachingExternalGadgetStore(ExternalGadgetStore delegateStore, final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.delegateStore = notNull("delegateStore", delegateStore);
        init(delegateStore);
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        specCache.clear();
        uriCache.clear();
        init(delegateStore);
    }

    public Set<ExternalGadgetSpec> getAllGadgetSpecUris()
    {
        return Collections.unmodifiableSet(new HashSet<ExternalGadgetSpec>(specCache.values()));
    }

    public ExternalGadgetSpec addGadgetSpecUri(final URI uri)
    {
        final ExternalGadgetSpec addedSpec = delegateStore.addGadgetSpecUri(uri);
        specCache.put(addedSpec.getId(), addedSpec);
        uriCache.add(addedSpec.getSpecUri());
        return addedSpec;
    }

    public void removeGadgetSpecUri(final ExternalGadgetSpecId id)
    {
        delegateStore.removeGadgetSpecUri(id);
        final ExternalGadgetSpec removedSpec = specCache.remove(id);
        uriCache.remove(removedSpec.getSpecUri());
    }

    public boolean containsSpecUri(final URI uri)
    {
        return uriCache.contains(uri);
    }

    private void init(ExternalGadgetStore delegateStore)
    {
        final Set<ExternalGadgetSpec> specs = delegateStore.getAllGadgetSpecUris();
        for (ExternalGadgetSpec spec : specs)
        {
            specCache.put(spec.getId(), spec);
            uriCache.add(spec.getSpecUri());
        }
    }
}
