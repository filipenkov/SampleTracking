package com.atlassian.gadgets.directory.internal.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.directory.Category;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Provides a JAXB view of {@code Category} values.
 * TODO: AG-428 We should add LINK elements to this representation, as the REST Guidleines recommend.
 */
@XmlRootElement
public final class JAXBCategory
{
    @XmlElement
    private final String name;

    // Provided for JAXB.
    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private JAXBCategory()
    {
        name = null;
    }

    /**
     * Constructor. Maps the {@link Category} properties onto the JAXB
     * properties.
     * @param category the {@link Category} value to map from
     */
    public JAXBCategory(Category category)
    {
        name = category.getName();
    }

    /**
     * Returns the name of the category.
     * @return the name of the category
     */
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
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

        JAXBCategory other = (JAXBCategory) that;

        return new EqualsBuilder().append(name, other.name).isEquals();
    }
}
