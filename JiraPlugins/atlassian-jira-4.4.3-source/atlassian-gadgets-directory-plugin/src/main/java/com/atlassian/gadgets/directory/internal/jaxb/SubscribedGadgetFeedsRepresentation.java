package com.atlassian.gadgets.directory.internal.jaxb;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider.FeedSpecProvider;
import com.atlassian.plugins.rest.common.Link;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.LocaleServiceProviderPool;

import static com.google.common.base.Preconditions.checkNotNull;

@XmlRootElement(name="subscribed-gadget-feeds")
public class SubscribedGadgetFeedsRepresentation
{
    @XmlElement private final Link self;
    @XmlElement(name="feeds") private final List<BriefSubscribedGadgetFeedRepresentation> feeds;

    public SubscribedGadgetFeedsRepresentation(Iterable<FeedSpecProvider> feedProviders, DirectoryUrlBuilder urlBuilder)
    {
        checkNotNull(feedProviders);
        checkNotNull(urlBuilder);
        
        this.self = Link.self(URI.create(urlBuilder.buildSubscribedGadgetFeedsUrl()));
        
        ImmutableList.Builder<BriefSubscribedGadgetFeedRepresentation> builder = ImmutableList.builder();
        for (FeedSpecProvider feedProvider : feedProviders)
        {
            builder.add(new BriefSubscribedGadgetFeedRepresentation(feedProvider, urlBuilder));
        }
        this.feeds = builder.build();
    }
    
    // no-arg ctor for jaxb
    @SuppressWarnings("unused")
    private SubscribedGadgetFeedsRepresentation()
    {
        this.self = null;
        this.feeds = null;
    }
    
    public Link getSelf()
    {
        return self;
    }
    
    public List<BriefSubscribedGadgetFeedRepresentation> getSubscribedGadgetFeeds()
    {
        return feeds;
    }
    
    public static final class BriefSubscribedGadgetFeedRepresentation
    {
        @XmlTransient
        private static final Logger LOG = LoggerFactory.getLogger(SubscribedGadgetFeedsRepresentation.class);

        @XmlElement private final Link self;
        @XmlElement private final Link feed;
        @XmlElement private final String id;
        @XmlElement private String name;
        @XmlElement private URI icon;
        @XmlElement private String title;
        @XmlElement private URI baseUri;
        @XmlElement private boolean invalid;

        public BriefSubscribedGadgetFeedRepresentation(FeedSpecProvider feedProvider, DirectoryUrlBuilder urlBuilder)
        {
            this.self = Link.self(URI.create(urlBuilder.buildSubscribedGadgetFeedUrl(feedProvider.getId())));
            this.feed = Link.link(feedProvider.getUri(), "alternate");
            this.id = feedProvider.getId();
            try
            {
                this.name = feedProvider.getApplicationName();
                this.icon = feedProvider.getIcon();
                this.title = feedProvider.getTitle();
                this.baseUri = feedProvider.getBaseUri();
                this.invalid = false;
            }
            catch(RuntimeException e)
            {
                LOG.info("Subscribed Gadget Feed is invalid because of exception", e);
                this.invalid = true;
            }
        }
        
        // no-arg ctor for jaxb
        @SuppressWarnings("unused")
        private BriefSubscribedGadgetFeedRepresentation()
        {
            this.self = null;
            this.feed = null;
            this.id = null;
            this.name = null;
            this.icon = null;
            this.title = null;
            this.baseUri = null;
        }
        
        public String getId()
        {
            return id;
        }
        
        public Link getSelf()
        {
            return self;
        }
        
        public Link getFeed()
        {
            return feed;
        }
        
        public String getName()
        {
            return name;
        }
        
        public URI getIcon()
        {
            return icon;
        }
        
        public String getTitle()
        {
            return title;
        }
        
        public URI getBaseUri()
        {
            return baseUri;
        }
    }
}
