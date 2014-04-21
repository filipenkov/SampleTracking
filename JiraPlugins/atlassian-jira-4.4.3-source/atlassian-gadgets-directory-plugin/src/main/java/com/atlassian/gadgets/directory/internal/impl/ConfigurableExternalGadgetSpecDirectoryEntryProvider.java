package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.ConfigurableExternalGadgetSpecStore;
import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;
import com.atlassian.gadgets.event.AddGadgetEvent;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * This implementation delegates spec storage to {@link ExternalGadgetSpecStore} to enforce the contract of that
 * interface (normalizing URIs on add, preventing duplicate storage, etc.) and so that user-added gadgets may be
 * persisted in between application sessions.
 */
// TODO: AG-456 more Javadocs?
public class ConfigurableExternalGadgetSpecDirectoryEntryProvider
    extends AbstractDirectoryEntryProvider<ExternalGadgetSpec> implements ConfigurableExternalGadgetSpecStore
{
    private final ExternalGadgetSpecStore externalGadgetSpecStore;
    private final DirectoryUrlBuilder directoryUrlBuilder;
    private final TransactionTemplate txTemplate;
    private final EventPublisher eventPublisher;

    public ConfigurableExternalGadgetSpecDirectoryEntryProvider(
            GadgetSpecFactory gadgetSpecFactory,
            ExternalGadgetSpecStore externalGadgetSpecStore,
            DirectoryUrlBuilder directoryUrlBuilder,
            TransactionTemplate txTemplate,
            final ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory,
            final EventPublisher eventPublisher)
    {
        super(gadgetSpecFactory, threadLocalDelegateExecutorFactory);
        this.externalGadgetSpecStore = externalGadgetSpecStore;
        this.directoryUrlBuilder = directoryUrlBuilder;
        this.txTemplate = txTemplate;
        this.eventPublisher = eventPublisher;
    }

    public boolean contains(URI gadgetSpecUri)
    {
        return externalGadgetSpecStore.contains(gadgetSpecUri);
    }

    @Override
    protected Iterable<ExternalGadgetSpec> internalEntries()
    {
        return externalGadgetSpecStore.entries();
    }

    public void add(final URI gadgetSpecUri) throws GadgetParsingException
    {
        txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                if (!contains(gadgetSpecUri))
                {
                    //have to publish the event first before the spec is validated since otherwise
                    //the request to retrieve the spec might be blocked making it impossible to validate it.
                    eventPublisher.publish(new AddGadgetEvent(gadgetSpecUri));
                    validateGadgetSpec(gadgetSpecUri);
                    externalGadgetSpecStore.add(gadgetSpecUri);
                }
                return null;
            }
        });
    }

    public void remove(final ExternalGadgetSpecId gadgetSpecId)
    {
        txTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                externalGadgetSpecStore.remove(gadgetSpecId);
                return null;
            }
        });
    }

    @Override
    protected Function<ExternalGadgetSpec, Directory.Entry> convertToLocalizedDirectoryEntry(
            final GadgetRequestContext gadgetRequestContext)
    {
        return new Function<ExternalGadgetSpec, Directory.Entry>()
        {
            public Directory.Entry apply(ExternalGadgetSpec externalGadgetSpec)
            {
                try
                {
                    return new GadgetSpecDirectoryEntry(
                        getGadgetSpec(externalGadgetSpec.getSpecUri(), gadgetRequestContext),
                        true,
                        getDirectoryEntryUri(externalGadgetSpec));
                }
                catch (GadgetParsingException e)
                {
                    return null;
                }
                catch (URISyntaxException e)
                {
                    return null;
                }
            }
        };
    }

    private void validateGadgetSpec(URI gadgetSpecUri) throws GadgetParsingException
    {
        try
        {
            // fetch it and parse it to validate it. there will never be a request that we could get a locale, cache
            // setting, or viewer from here
            GadgetSpec gadgetSpec = getGadgetSpec(gadgetSpecUri,  GadgetRequestContext.NO_CURRENT_REQUEST);

            // make sure all required features are available
            if (!Iterables.isEmpty(gadgetSpec.getUnsupportedFeatureNames()))
            {
                throw new UnavailableFeatureException(gadgetSpec.getUnsupportedFeatureNames().toString());
            }
        }
        catch (GadgetParsingException e)
        {
            throw new GadgetParsingException(e);
        }
    }

    private URI getDirectoryEntryUri(ExternalGadgetSpec externalGadgetSpec) throws URISyntaxException
    {
        return new URI(directoryUrlBuilder.buildDirectoryGadgetResourceUrl(externalGadgetSpec.getId()));
    }

    @Override
    public String toString()
    {
        return "configured external gadget specs";
    }
}
