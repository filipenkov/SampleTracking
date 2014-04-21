package com.atlassian.gadgets.directory.internal;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetSpecProvider;
import com.atlassian.gadgets.event.AddGadgetFeedEvent;
import com.atlassian.gadgets.util.TransactionRunner;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.jdom.Document;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.atlassian.gadgets.directory.internal.GadgetSpecProviderHelper.containsSpecUri;
import static com.atlassian.gadgets.directory.internal.GadgetSpecProviderHelper.toEntries;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.*;
import static org.apache.commons.lang.StringUtils.isBlank;

public class GadgetFeedsSpecProvider implements GadgetSpecProvider
{
    private static final String ATOM = "atom_1.0";

    private final WireFeedInput feedBuilder = new FixedClassLoaderWireFeedInput();

    private final HTTPCache http;
    private final SubscribedGadgetFeedStore store;
    private final TransactionRunner transactionRunner;
    private final EventPublisher eventPublisher;

    public GadgetFeedsSpecProvider(HTTPCache http, SubscribedGadgetFeedStore store, TransactionRunner transactionRunner,
            final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.http = checkNotNull(http, "http");
        this.store = checkNotNull(store, "store");
        this.transactionRunner = checkNotNull(transactionRunner, "transactionRunner");
    }

    public boolean contains(URI gadgetSpecUri)
    {
        return any(getFeedProviders(), feedContains(gadgetSpecUri));
    }

    public Iterable<URI> entries()
    {
        return concat(transform(getFeedProviders(), toEntries()));
    }

    /**
     * Add a new gadget feed to the list of feeds to get gadget specs from.
     *
     * @param feedUri absolute URI of the gadget spec feed
     * @throws GadgetFeedParsingException
     * @throws NonAtomGadgetSpecFeedException
     */
    public FeedSpecProvider addFeed(final URI feedUri)
    {
        final SubscribedGadgetFeed feed = new SubscribedGadgetFeed(UUID.randomUUID().toString(), feedUri);
        FeedSpecProvider applicationProvider = toProvider().apply(feed);

        // try and get the entries, if it fails then we don't want to add the provider
        applicationProvider.entries();

        final URI baseUri = applicationProvider.getBaseUri();

        transactionRunner.execute(new Runnable()
        {
            public void run()
            {
                eventPublisher.publish(new AddGadgetFeedEvent(baseUri));
                store.add(feed);
            }
        });
        return applicationProvider;
    }

    /**
     * Returns the feeds that have been subscribed to.
     *
     * @return the feeds that have been subscribed to
     */
    public Iterable<SubscribedGadgetFeed> getFeeds()
    {
        return transactionRunner.execute(new Callable<Iterable<SubscribedGadgetFeed>>()
        {
            public Iterable<SubscribedGadgetFeed> call()
            {
                return store.getAll();
            }
        });
    }

    /**
     * Returns {@code true} if there is a subscribed feed with the given ID, {@code false} otherwise.
     *
     * @param feedId ID of the feed to check for
     * @return {@code true} if there is a subscribed feed with the given ID, {@code false} otherwise
     */
    public boolean containsFeed(final String feedId)
    {
        return transactionRunner.execute(new Callable<Boolean>()
        {
            public Boolean call()
            {
                return store.contains(feedId);
            }
        });
    }

    /**
     * Returns the feed provider for the subscribed feed if it exists, {@code null} otherwise.
     *
     * @param feedId ID of the feed to get the provider for
     * @return feed provider for the subscribed feed if it exists, {@code null} otherwise.
     */
    public FeedSpecProvider getFeedProvider(final String feedId)
    {
        return transactionRunner.execute(new Callable<FeedSpecProvider>()
        {
            public FeedSpecProvider call()
            {
                if (!store.contains(feedId))
                {
                    return null;
                }
                return toProvider().apply(store.get(feedId));
            }
        });
    }

    /**
     * Remove a feed from the list of feeds subscribed to.
     *
     * @param feedId URI of the feed to remove
     */
    public void removeFeed(final String feedId)
    {
        transactionRunner.execute(new Runnable()
        {
            public void run()
            {
                store.remove(feedId);
            }
        });
    }

    public Iterable<FeedSpecProvider> getFeedProviders()
    {
        return transform(getFeeds(), toProvider());
    }

    private Function<SubscribedGadgetFeed, FeedSpecProvider> toProvider()
    {
        return feedSpecProviderConverter;
    }

    private final Function<SubscribedGadgetFeed, FeedSpecProvider> feedSpecProviderConverter = new Function<SubscribedGadgetFeed, FeedSpecProvider>()
    {
        public FeedSpecProvider apply(SubscribedGadgetFeed feed)
        {
            return new FeedSpecProvider(feed, http, feedBuilder);
        }
    };

    private static Predicate<GadgetSpecProvider> feedContains(URI gadgetSpecUri)
    {
        return containsSpecUri(gadgetSpecUri);
    }

    public static class FeedSpecProvider implements GadgetSpecProvider
    {
        private final Log logger = LogFactory.getLog(getClass());

        private final SubscribedGadgetFeed gadgetFeed;
        private final LazyReference<Feed> feedRef;

        public FeedSpecProvider(SubscribedGadgetFeed feed, HTTPCache http, WireFeedInput feedBuilder)
        {
            this.gadgetFeed = feed;
            this.feedRef = new LazyFeedReference(feed, http, feedBuilder);
        }

        public String getId()
        {
            return gadgetFeed.getId();
        }

        public URI getUri()
        {
            return gadgetFeed.getUri();
        }

        public String getApplicationName()
        {
            List<Person> authors = getAuthors();
            if (authors.size() == 0)
            {
                return "";
            }
            return authors.get(0).getName();
        }

        public String getTitle()
        {
            return getFeed().getTitle();
        }

        public URI getIcon()
        {
            String icon = getFeed().getIcon();
            if (isBlank(icon))
            {
                return null;
            }
            return URI.create(icon);
        }

        public URI getBaseUri()
        {
            Feed feed = getFeed();
            for (Link link : getOtherLinks(feed))
            {
                if ("base".equals(link.getRel()))
                {
                    try
                    {
                        return new URI(link.getHref());
                    }
                    catch (URISyntaxException e)
                    {
                        return null;
                    }
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<Link> getOtherLinks(Feed feed)
        {
            return feed.getOtherLinks();
        }

        public boolean contains(URI gadgetSpecUri)
        {
            return getGadgetUris().contains(gadgetSpecUri);
        }

        public Iterable<URI> entries()
        {
            return getGadgetUris();
        }

        private Set<URI> getGadgetUris()
        {
            ImmutableSet.Builder<URI> uris = ImmutableSet.builder();
            for (Entry entry : getEntries())
            {
                String href = ((Link) entry.getAlternateLinks().get(0)).getHref();
                try
                {
                    uris.add(new URI(href));
                }
                catch (URISyntaxException e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.info("URI '" + href + "' of gadget feed is invalid", e);
                    }
                    else
                    {
                        logger.info("URI '" + href + "' of gadget directory feed is invalid: " + e.getMessage());
                    }
                }
            }
            return uris.build();
        }

        @SuppressWarnings("unchecked")
        private List<Entry> getEntries()
        {
            return (List<Entry>) getFeed().getEntries();
        }

        private Feed getFeed()
        {
            try
            {
                return feedRef.get();
            }
            catch (LazyReference.InitializationException e)
            {
                // only runtime exceptions should be thrown, but better safe than sorry
                if (e.getCause() instanceof RuntimeException)
                {
                    throw (RuntimeException) e.getCause();
                }
                else
                {
                    throw e;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private List<Person> getAuthors()
        {
            return getFeed().getAuthors();
        }

        private static final class LazyFeedReference extends LazyReference<Feed>
        {
            private final SubscribedGadgetFeed gadgetFeed;
            private final HTTPCache http;
            private final WireFeedInput feedBuilder;

            public LazyFeedReference(SubscribedGadgetFeed feed, HTTPCache http, WireFeedInput feedBuilder)
            {
                this.gadgetFeed = feed;
                this.http = http;
                this.feedBuilder = feedBuilder;
            }

            @Override
            protected Feed create() throws Exception
            {
                HTTPRequest request = new HTTPRequest(gadgetFeed.getUri());
                HTTPResponse response = http.doCachedRequest(request);
                InputStream is = response.getPayload().getInputStream();
                try
                {
                    return parseFeed(new InputStreamReader(is));
                }
                finally
                {
                    IOUtils.closeQuietly(is);
                }
            }

            private Feed parseFeed(Reader reader)
            {
                try
                {
                    WireFeed feed = feedBuilder.build(reader);
                    if (!ATOM.equals(feed.getFeedType()))
                    {
                        throw new NonAtomGadgetSpecFeedException(gadgetFeed.getUri());
                    }
                    return (Feed) feed;
                }
                catch (IllegalArgumentException e)
                {
                    throw new NonAtomGadgetSpecFeedException(gadgetFeed.getUri());
                }
                catch (FeedException e)
                {
                    throw new GadgetFeedParsingException("Unable to parse the feed, it is not validly formed", gadgetFeed.getUri(), e);
                }
            }
        }
    }

    private static class FixedClassLoaderWireFeedInput extends WireFeedInput
    {
        /**
         * We only need to override this method to wrap the parent implementation because it is called by all the other
         * build methods.
         */
        @Override
        public WireFeed build(Document document) throws IllegalArgumentException, FeedException
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(WireFeedInput.class.getClassLoader());
            try
            {
                return super.build(document);
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }
}
