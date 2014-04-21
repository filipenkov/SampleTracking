package com.atlassian.jira.entity;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An entity factory is able to convert a Generic Value for an entity into a domain object and back.
 *
 * It is used by {@link EntityEngine} to run queries that return Domain objects instead of GenericValues.
 *
 * @since v4.4
 */
public interface EntityFactory<E>
{
    /**
     * The name of the Entity as defined in the entitymodel.xml file.
     *
     * @return the name of the Entity.
     */
    String getEntityName();

    /**
     * Builds an instance of this Entity from the given GenericValue.
     *
     * @param genericValue GenericValue for the entity
     * @return the entity Object
     */
    E build(GenericValue genericValue);

    /**
     * Builds a list of instances from a list of GenericValues.
     *
     * This method is included in the interface so that the no-op implementation can take a performance shortcut and
     * simply return the given list.
     *
     * @param gvList List of GenericValues
     * @return List of objects.
     */
    List<E> buildList(Collection<GenericValue> gvList);

    /**
     * Builds a FieldMap from an entity Object.
     * The FieldMap is the data held by a GenericValue, so this basically is the reverse operation of turning an object
     * into a GenericValue.
     *
     * @param value the Entity value
     * @return Map of field values
     */
    Map<String, Object> fieldMapFrom(E value);
}
