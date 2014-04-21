package com.atlassian.gadgets.directory.internal;

import java.net.URI;

import com.atlassian.gadgets.GadgetSpecProvider;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class GadgetSpecProviderHelper
{
    static Predicate<GadgetSpecProvider> containsSpecUri(final URI gadgetSpecUri)
    {
        return new GadgetSpecProviderContainsPredicate(gadgetSpecUri);
    }

    private static final class GadgetSpecProviderContainsPredicate implements Predicate<GadgetSpecProvider>
    {
        private final Log logger = LogFactory.getLog(getClass());
        
        private final URI gadgetSpecUri;

        private GadgetSpecProviderContainsPredicate(URI gadgetSpecUri)
        {
            this.gadgetSpecUri = gadgetSpecUri;
        }

        public boolean apply(GadgetSpecProvider provider)
        {
            try
            {
                return provider.contains(gadgetSpecUri);
            }
            catch (NonAtomGadgetSpecFeedException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri().toASCIIString() + "' is not an Atom feed", e);
                }
                else
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri().toASCIIString() + "' is not an Atom feed");
                }
                return false;
            }
            catch (GadgetFeedParsingException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri() + "' could not be parsed as an Atom feed", e);
                }
                else
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri() + "' could not be parsed as an Atom feed");
                }
                return false;
            }
            catch (RuntimeException e)
            {
                logger.debug("Unable to determine if GadgetSpecProvider contains the URI '" + gadgetSpecUri, e);
                return false;
            }
        }
    }

    static Function<GadgetSpecProvider, Iterable<URI>> toEntries()
    {
        return new GadgetSpecProviderToEntriesFunction();
    }

    private static final class GadgetSpecProviderToEntriesFunction implements Function<GadgetSpecProvider, Iterable<URI>>
    {
        private final Log logger = LogFactory.getLog(getClass());

        public Iterable<URI> apply(GadgetSpecProvider provider)
        {
            try
            {
                return provider.entries();
            }
            catch (NonAtomGadgetSpecFeedException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri().toASCIIString() + "' is not an Atom feed", e);
                }
                else
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri().toASCIIString() + "' is not an Atom feed");
                }
                return ImmutableSet.of();
            }
            catch (GadgetFeedParsingException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri() + "' could not be parsed as an Atom feed", e);
                }
                else
                {
                    logger.warn("Gadget spec feed at '" + e.getFeedUri() + "' could not be parsed as an Atom feed");
                }
                return ImmutableSet.of();
            }
            catch (RuntimeException e)
            {
                logger.debug("Unable to get the contents of the GadgetSpecProvider", e);
                return ImmutableSet.of();
            }
        }
    }
}
