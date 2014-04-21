package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a null (no) restriction entity.
 */
@XmlRootElement (name = "null-search-restriction")
@XmlAccessorType (XmlAccessType.FIELD)
public final class NullRestrictionEntity extends SearchRestrictionEntity
{
    public static final NullRestrictionEntity INSTANCE = new NullRestrictionEntity();

    private NullRestrictionEntity()
    {
    }
}
