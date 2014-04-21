package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a set of {@link RemoteAddressEntity}.
 *
 * @since 2.2
 */
@XmlRootElement (name = "remote-addresses")
@XmlAccessorType (XmlAccessType.FIELD)
public class RemoteAddressEntitySet implements Iterable<RemoteAddressEntity>
{
    @XmlElements (@XmlElement (name = "remote-address"))
    private final Set<RemoteAddressEntity> remoteAddresses;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private RemoteAddressEntitySet()
    {
        remoteAddresses = Sets.newHashSet();
        link = null;
    }

    public RemoteAddressEntitySet(final Set<RemoteAddressEntity> remoteAddresses, final Link link)
    {
        this.remoteAddresses = Sets.newHashSet(remoteAddresses);
        this.link = link;
    }

    public void addRemoteAddress(RemoteAddressEntity addressEntity)
    {
        remoteAddresses.add(addressEntity);
    }

    public Iterator<RemoteAddressEntity> iterator()
    {
        return remoteAddresses.iterator();
    }

    public int size()
    {
        return remoteAddresses.size();
    }

    public boolean isEmpty()
    {
        return remoteAddresses.isEmpty();
    }

    public Link getLink()
    {
        return link;
    }
}
