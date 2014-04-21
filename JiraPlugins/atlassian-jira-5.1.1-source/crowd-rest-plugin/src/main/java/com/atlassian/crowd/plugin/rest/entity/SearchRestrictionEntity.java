package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Search restriction entity classes should extend this abstract class.
 *
 * SearchRestrictionEntity is an abstract class instead of an interface because JAXB doesn't play nice with interfaces.
 */
@XmlRootElement (name = "search-restriction")
@XmlSeeAlso ({ BooleanRestrictionEntity.class, PropertyRestrictionEntity.class, NullRestrictionEntity.class})
public abstract class SearchRestrictionEntity
{
}
