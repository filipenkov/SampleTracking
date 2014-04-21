package com.atlassian.plugins.rest.common;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Represents a link to a given entity. The {@link #href}
 *
 * @since 1.0
 */
@XmlRootElement
public class Link
{
    @XmlAttribute
    private final URI href;

    @XmlAttribute
    private final String type;

    @XmlAttribute
    private final String rel;

    // For JAXB's usage

    private Link()
    {
        this.href = null;
        this.rel = null;
        this.type = null;
    }

    private Link(URI href, String rel, String type)
    {
        this.href = Preconditions.checkNotNull(href);
        this.rel = Preconditions.checkNotNull(rel);
        this.type = type;
    }

    /**
     * Creates a link using the specified uri and rel values
     *
     * @param uri the {@link URI} to use for the link URI,
     * @param rel the rel attribute of the link.
     * @return a link
     */
    public static Link link(URI uri, String rel)
    {
        return new Link(uri, rel, null);
    }

    /**
     * Creates a link using the given URI builder to build the URI.
     *
     * @param uri  the {@link URI} to use for the link URI,
     * @param rel  the rel attribute of the link.
     * @param type the type attribute of the link.
     * @return a link
     */
    public static Link link(URI uri, String rel, String type)
    {
        return new Link(uri, rel, type);
    }

    /**
     * Creates a link using the given URI builder to build the URI.
     * The {@code rel} attribute of the link is set to {@code self}.
     *
     * @param uri the {@link URI} to use for the link URI.
     * @return a link
     */
    public static Link self(URI uri)
    {
        return link(uri, "self");
    }

    /**
     * Creates a link using the given URI builder to build the URI.
     * The {@code rel} attribute of the link is set to {@code edit}.
     *
     * @param uri the {@link URI} to use for the link URI.
     * @return a link
     */
    public static Link edit(URI uri)
    {
        return link(uri, "edit");
    }

    /**
     * Creates a link using the given URI builder to build the URI.
     * The {@code rel} attribute of the link is set to {@code add}.
     *
     * @param uri the {@link URI} to use for the link URI.
     * @return a link
     */
    public static Link add(URI uri)
    {
        return link(uri, "add");
    }

    /**
     * Creates a link using the given URI builder to build the URI.
     * The {@code rel} attribute of the link is set to {@code delete}.
     *
     * @param uri the {@link URI} to use for the link URI.
     * @return a link
     */
    public static Link delete(URI uri)
    {
        return link(uri, "delete");
    }

    public URI getHref()
    {
        return href;
    }

    public String getRel()
    {
        return rel;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 7).append(href).append(rel).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }
        final Link link = (Link) obj;
        return new EqualsBuilder().append(href, link.href).append(rel, link.rel).isEquals();
    }
}
