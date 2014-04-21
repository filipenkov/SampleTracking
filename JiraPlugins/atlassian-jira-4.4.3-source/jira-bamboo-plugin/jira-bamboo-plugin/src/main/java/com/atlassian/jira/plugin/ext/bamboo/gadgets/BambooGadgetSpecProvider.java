package com.atlassian.jira.plugin.ext.bamboo.gadgets;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.gadgets.GadgetSpecProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

@net.jcip.annotations.ThreadSafe
public class BambooGadgetSpecProvider implements GadgetSpecProvider
{
    private static final Logger log = Logger.getLogger(BambooGadgetSpecProvider.class);

    private final Map<ApplicationId, Set<URI>> bambooGadgets = new HashMap<ApplicationId, Set<URI>>();

    /**
     * Get all gadget URIs for all Bamboo application links.
     */
    public Iterable<URI> entries()
    {
        ImmutableList.Builder<URI> uris = ImmutableList.builder();
        for (ApplicationId appId : bambooGadgets.keySet())
        {
            uris.addAll(bambooGadgets.get(appId));
        }
        return uris.build();
    }

    public boolean contains(final URI uri)
    {
        return Iterables.contains(entries(), uri);
    }

    /**
     * Add the specified gadget URI for the specified application id
     *
     * @param appId the application id
     * @param gadgetUri the gadget URI
     */
    public void addBambooGadget(ApplicationId appId, URI gadgetUri)
    {
        if (!bambooGadgets.containsKey(appId))
        {
            bambooGadgets.put(appId, new CopyOnWriteArraySet<URI>());
        }

        bambooGadgets.get(appId).add(gadgetUri);
    }

    /**
     * Remove all Bamboo gadgets for the specified application id
     */
    public void removeBambooGadgets(ApplicationId appId)
    {
        bambooGadgets.remove(appId);
    }

    /**
     * Migrate an application id's Bamboo gadgets to be registered to a different application id.
     *
     */
    public void migrateBambooGadgets(ApplicationId oldId, ApplicationId newId)
    {
        Set<URI> gadgets = bambooGadgets.remove(oldId);
        if (gadgets != null)
        {
            bambooGadgets.put(newId, gadgets);
        }
    }

    public void clear()
    {
        bambooGadgets.clear();
    }
}
