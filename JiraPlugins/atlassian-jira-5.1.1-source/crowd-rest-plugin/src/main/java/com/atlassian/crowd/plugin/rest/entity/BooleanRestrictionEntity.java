package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a boolean restriction entity.
 */
@XmlRootElement (name = "boolean-search-restriction")
@XmlAccessorType (XmlAccessType.FIELD)
public class BooleanRestrictionEntity extends SearchRestrictionEntity
{
    @XmlAttribute (name = "boolean-logic")
    private final String booleanLogic;

    @XmlElementWrapper (name = "restrictions")
    @XmlElements ({
            @XmlElement (name = "boolean-search-restriction", type = BooleanRestrictionEntity.class),
            @XmlElement (name = "property-search-restriction", type = PropertyRestrictionEntity.class),
            @XmlElement (name = "null-search-restriction", type = NullRestrictionEntity.class)
    })
    private final Collection<SearchRestrictionEntity> restrictions;

    /**
     * JAXB requires a no-arg constructor
     */
    private BooleanRestrictionEntity()
    {
        booleanLogic = null;
        restrictions = new ArrayList<SearchRestrictionEntity>();
    }

    /**
     * Creates a new instance of BooleanRestrictionEntity.
     *
     * @param booleanLogic the boolean logic that applies to the <code>restrictions</code>
     * @param restrictions the list of restrictions
     * @return a new instance of BooleanRestrictionEntity
     */
    public BooleanRestrictionEntity(final String booleanLogic, final Collection<SearchRestrictionEntity> restrictions)
    {
        this.booleanLogic = booleanLogic;
        this.restrictions = Collections.unmodifiableCollection(restrictions);
    }

    public Collection<SearchRestrictionEntity> getRestrictions()
    {
        return restrictions;
    }

    public String getBooleanLogic()
    {
        return booleanLogic;
    }
}
