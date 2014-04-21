package com.atlassian.crowd.model.group;

/**
 * Extends the Group interface with "isLocal".
 * This is used for working with Local Groups, but is not supported for LDAP.
 */
public interface InternalDirectoryGroup extends TimestampedGroup
{
    /**
     * Returns true if this is a Local Group.
     * @return true if this is a Local Group.
     */
    boolean isLocal();
}
