package com.atlassian.gadgets.directory.internal.jaxb;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.directory.Directory;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Provides a JAXB view of Directory.Entry implementations. TODO: AG-428 We should add LINK elements to this
 * representation, as the REST Guidleines recommend.
 */
@XmlRootElement
public final class JAXBDirectoryEntry
{
    @XmlElement
    private final URI self;
    @XmlElement
    private final Boolean isDeletable;
    @XmlElement
    private final String authorEmail;
    @XmlElement
    private final String authorName;
    @XmlElement
    private final Collection<String> categories;
    @XmlElement
    private final String description;
    @XmlElement
    private final URI gadgetSpecUri;
    @XmlElement
    private final URI thumbnailUri;
    @XmlElement
    private final String title;
    @XmlElement
    private final URI titleUri;

    // Provided for JAXB.
    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private JAXBDirectoryEntry()
    {
        self = null;
        isDeletable = null;
        authorEmail = null;
        authorName = null;
        categories = null;
        description = null;
        gadgetSpecUri = null;
        thumbnailUri = null;
        title = null;
        titleUri = null;
    }

    /**
     * Constructor. Maps the {@link com.atlassian.gadgets.directory.Directory.Entry} properties onto the JAXB properties.
     *
     * @param entry the {@link com.atlassian.gadgets.directory.Directory.Entry} implementation to use
     */
    public JAXBDirectoryEntry(Directory.Entry entry)
    {
        self = entry.getSelf();
        isDeletable = entry.isDeletable();
        authorEmail = entry.getAuthorEmail();
        authorName = entry.getAuthorName();
        categories = transformCollectionCategoriesToNameStrings(entry.getCategories());
        description = entry.getDescription();
        gadgetSpecUri = entry.getGadgetSpecUri();
        thumbnailUri = entry.getThumbnailUri();
        title = entry.getTitle();
        titleUri = entry.getTitleUri();
    }

    /**
     * Trade a Collection of Category objects for a Collection of Strings that are the names of those Categories,
     * because the names are more nicely formatted for display, e.g. Confluence instead of CONFLUENCE
     *
     * @param from the collection of categories
     * @return the transformed collection of category name strings
     */
    private Collection<String> transformCollectionCategoriesToNameStrings(Collection<Category> from)
    {
        Collection<String> result = new HashSet<String>();

        for (Category category : from)
        {
            result.add(category.getName());
        }

        return result;
    }

    /**
     * The gadget spec file's resource URI. NOTE: this is NOT the gadget spec file location, rather the URI that
     * represents this gadget's resource
     *
     * @return the gadget spec file's resource URI.
     */
    public URI getSelf()
    {
        return self;
    }

    /**
     * Whehter or not this gadget spec file can be removed from the directory.
     *
     * @return whehter or not this gadget spec file can be removed from the directory.
     */
    public Boolean isDeletable()
    {
        return isDeletable;
    }

    /**
     * Returns the gadget author's email address.
     *
     * @return the gadget author's email address
     */
    public String getAuthorEmail()
    {
        return authorEmail;
    }

    /**
     * Returns the gadget author's name.
     *
     * @return the gadget author's name
     */
    public String getAuthorName()
    {
        return authorName;
    }

    /**
     * Returns the gadget's categories.
     *
     * @return the gadget's categories
     */
    public Collection<String> getCategories()
    {
        return categories;
    }

    /**
     * Returns the entry's description.
     *
     * @return the entry's description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the gadget's spec URI.
     *
     * @return the gadget's spec URI
     */
    public URI getGadgetSpecUri()
    {
        return gadgetSpecUri;
    }

    /**
     * Returns the gadget's thumbnail URI.
     *
     * @return the gadget's thumbnail URI
     */
    public URI getThumbnailUri()
    {
        return thumbnailUri;
    }

    /**
     * Returns the gadget's title as a String.
     *
     * @return the gadget's title as a String
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the gadget's title URI.
     *
     * @return the gadget's title URI
     */
    public URI getTitleUri()
    {
        return titleUri;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 29).
            append(self).
            append(isDeletable).
            append(authorEmail).
            append(authorName).
            append(categories).
            append(description).
            append(gadgetSpecUri).
            append(thumbnailUri).
            append(title).
            append(titleUri).
            toHashCode();
    }

    @Override
    public boolean equals(Object that)
    {
        if (that == null)
        {
            return false;
        }

        if (this == that)
        {
            return true;
        }

        if (that.getClass() != getClass())
        {
            return false;
        }

        JAXBDirectoryEntry other = (JAXBDirectoryEntry) that;
        return new EqualsBuilder().
            append(self, other.self).
            append(isDeletable, other.isDeletable).
            append(authorEmail, other.authorEmail).
            append(authorName, other.authorName).
            append(categories, other.categories).
            append(description, other.description).
            append(gadgetSpecUri, other.gadgetSpecUri).
            append(thumbnailUri, other.thumbnailUri).
            append(title, other.title).
            append(titleUri, other.titleUri).
            isEquals();
    }
}
