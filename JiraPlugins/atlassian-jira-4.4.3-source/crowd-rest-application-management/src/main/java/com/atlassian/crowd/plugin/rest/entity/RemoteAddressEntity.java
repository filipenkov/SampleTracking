package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a RemoteAddress entity
 *
 * @since 2.2
 */
@XmlRootElement (name = "remote-address")
@XmlAccessorType (XmlAccessType.FIELD)
public class RemoteAddressEntity
{
    @XmlElement (name = "value")
    private final String value;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private RemoteAddressEntity()
    {
        this.value = null;
        this.link = null;
    }

    public RemoteAddressEntity(final String value, final Link link)
    {
        this.value = value;
        this.link = link;
    }

    /**
     * Returns the value of the remote address.
     *
     * @return value of the remote address
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Returns the link to the remote address entity.
     *
     * @return link to the remote address entity
     */
    public Link getLink()
    {
        return link;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final RemoteAddressEntity that = (RemoteAddressEntity) o;

        if (value != null ? !value.equals(that.value) : that.value != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return value != null ? value.hashCode() : 0;
    }
}
