package com.atlassian.gadgets.directory.internal.jaxb;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider.FeedSpecProvider;
import com.atlassian.plugins.rest.common.Link;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

@XmlRootElement(name = "subscribed-gadget-feed")
public class SubscribedGadgetFeedRepresentation
{
    @XmlElement private final Link feed;
    @XmlElement private final Link self;
    @XmlElementWrapper @XmlElement(name="gadget") final List<GadgetSpecRepresentation> gadgets;
    
    public SubscribedGadgetFeedRepresentation(FeedSpecProvider provider, DirectoryUrlBuilder urlBuilder)
    {
        checkNotNull(provider, "provider");
        this.feed = Link.link(provider.getUri(), "alternate");
        this.self = Link.self(URI.create(urlBuilder.buildSubscribedGadgetFeedUrl(provider.getId())));
        
        ImmutableList.Builder<GadgetSpecRepresentation> builder = ImmutableList.builder();
        for (URI uri : provider.entries())
        {
            builder.add(new GadgetSpecRepresentation(uri));
        }
        this.gadgets = builder.build(); 
    }
    
    // no-arg ctor for JAXB
    @SuppressWarnings("unused")
    private SubscribedGadgetFeedRepresentation()
    {
        this.feed = null;
        this.self = null;
        this.gadgets = null;
    }
    
    public Link getFeed()
    {
        return feed;
    }
    
    public Link getSelf()
    {
        return self;
    }
    
    public List<GadgetSpecRepresentation> getGadgets()
    {
        return gadgets;
    }
    
    public static final class GadgetSpecRepresentation
    {
        @XmlAttribute private final URI uri;
        
        public GadgetSpecRepresentation(URI uri)
        {
            this.uri = uri;
        }
        
        // no-arg ctor for JAXB
        @SuppressWarnings("unused")
        private GadgetSpecRepresentation()
        {
            this.uri = null;
        }
        
        public URI getUri()
        {
            return uri;
        }
    }
}
