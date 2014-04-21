package com.atlassian.crowd.model;

import com.atlassian.crowd.embedded.api.Attributes;

import java.io.Serializable;

public interface LDAPDirectoryEntity extends DirectoryEntity, Attributes, Serializable
{
    /**
     * Gets the <b>standardised</b> distinguished name for the LDAP entity.
     *
     * @return distinguished name.
     */
    String getDn();
}
