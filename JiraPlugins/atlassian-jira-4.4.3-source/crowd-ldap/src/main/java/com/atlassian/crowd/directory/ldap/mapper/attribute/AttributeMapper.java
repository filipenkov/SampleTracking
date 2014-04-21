package com.atlassian.crowd.directory.ldap.mapper.attribute;

import org.springframework.ldap.core.DirContextAdapter;

import java.util.Set;

/**
 * Maps a single attribute for an entity. This is used for CUSTOM attributes.
 * <p/>
 * Mandatory or field-level attributes are mapped via the {@link com.atlassian.crowd.directory.ldap.mapper.entity.LDAPUserAttributesMapper}
 * and the {@link com.atlassian.crowd.directory.ldap.mapper.entity.LDAPGroupAttributesMapper}.
 */
public interface AttributeMapper
{
    /**
     * Get the key to use when storing the attribute on an entity.
     *
     * @return non-null key.
     */
    public String getKey();

    /**
     * Map the value of the key from the directory context.
     *
     * @param ctx directory context containing attributes.
     * @return the set of attribute values associated with the key. If no values are present an empty set will be returned
     * @throws Exception error retrieving value. The attribute will not be set.
     */
    public Set<String> getValues(DirContextAdapter ctx) throws Exception;
}
