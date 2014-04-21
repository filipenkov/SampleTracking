package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a property restriction entity.
 */
@XmlRootElement (name = "property-search-restriction")
@XmlAccessorType (XmlAccessType.FIELD)
public class PropertyRestrictionEntity extends SearchRestrictionEntity
{
    @XmlElement (name = "property")
    private final PropertyEntity property;
    @XmlElement (name = "match-mode")
    private final String matchMode;
    @XmlElement (name = "value")
    private final String value;

    private PropertyRestrictionEntity()
    {
        property = null;
        matchMode = null;
        value = null;
    }

    /**
     * Creates a new instance of RestPropertyRestriction.
     *
     * @param property property to restrict on
     * @param matchMode property match mode
     * @param value value to match against
     * @return new instance of RestPropertyRestriction
     */
    public PropertyRestrictionEntity(final PropertyEntity property, final String matchMode, final String value)
    {
        this.property = property;
        this.matchMode = matchMode;
        this.value = value;
    }

    public PropertyEntity getProperty()
    {
        return property;
    }

    public String getMatchMode()
    {
        return matchMode;
    }

    public String getValue()
    {
        return value;
    }
}
