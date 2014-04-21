package com.atlassian.crowd.dao.property;

import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.model.property.Property;

import java.util.List;

/**
 * Persist {@link com.atlassian.crowd.model.property.Property property} objects.
 */
public interface PropertyDAO
{
    /**
     * Finds property by key and name.
     *
     * @param key Key.
     * @param name Name.
     * @return Property.
     * @throws ObjectNotFoundException If the property cannot be found.
     */
    Property find(String key, String name) throws ObjectNotFoundException;

    /**
     * Finds properties by key.
     *
     * @param key Key.
     * @return List of properties
     */
    List<Property> findAll(String key);

    /**
     * Adds a new property.
     *
     * @param property Property.
     * @return The saved property.
     */
    Property add(Property property);

    /**
     * Updates a property.
     *
     * @param property Property.
     * @return The updated property.
     */
    Property update(Property property);

    /**
     * Removes a property.
     *
     * @param key Key.
     * @param name Name.
     */
    void remove(String key, String name);

    /**
     * Retrieves all properties.
     *
     * @return the list of properties.
     */
    List<Property> findAll();
}