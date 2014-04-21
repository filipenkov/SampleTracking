package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;

/**
 * Map-backed implementation of {@code ExternalGadgetSpecStore} useful for testing.
 */
public class MapBackedExternalGadgetSpecStore implements ExternalGadgetSpecStore
{
    private final Map<ExternalGadgetSpecId, ExternalGadgetSpec> entries =
        new HashMap<ExternalGadgetSpecId, ExternalGadgetSpec>();
    private AtomicLong serial = new AtomicLong(1);

    public Iterable<ExternalGadgetSpec> entries()
    {
        return Collections.unmodifiableCollection(entries.values());
    }

    public ExternalGadgetSpec add(URI gadgetSpecUri)
    {
        ExternalGadgetSpecId specId = ExternalGadgetSpecId.valueOf(Long.toString(serial.getAndIncrement()));
        ExternalGadgetSpec gadgetSpec = new ExternalGadgetSpec(specId, gadgetSpecUri);
        entries.put(specId, gadgetSpec);
        return gadgetSpec;
    }

    public void remove(ExternalGadgetSpecId specId)
    {
        entries.remove(specId);
    }

    public boolean contains(URI gadgetSpecUri)
    {
        for (ExternalGadgetSpec spec : entries.values())
        {
            if (spec.getSpecUri().equals(gadgetSpecUri))
            {
                return true;
            }
        }
        return false;
    }
}
