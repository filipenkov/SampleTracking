package com.atlassian.crowd.embedded.api;

import java.util.Set;

/**
 * Represents attributes that can be associated to users and groups. One attribute key can have multiple values
 * associated to it.
 */
public interface Attributes
{
    /**
     * Get all the values associated with a given key. Duplicate values are not allowed, and this
     * should be enforced case-insensitively to match the behaviour of LDAP servers.
     * Will return null if the key does not exist.
     *
     * @param key the key to retrieve the values for
     * @return the values associated with the given key, or null if the key does not exist.
     */
    Set<String> getValues(String key);

    /**
     * Returns any value associated with the given key, returns {@code null} if there is no value.
     *
     * @param key the key to retrieve the value for
     * @return any value associated with the given key, or {@code null} if there is no value
     */
    String getValue(String key);

    /**
     * Gets all the keys of the attributes.
     * Warning: case-insensitive keys are currently no enforced, however this is the case for LDAP, so this may be
     * implemented in the future.
     *
     * @return a set of all the keys.
     */
    Set<String> getKeys();

    /**
     * @return {@code true} if there are no attributes
     */
    boolean isEmpty();
}
