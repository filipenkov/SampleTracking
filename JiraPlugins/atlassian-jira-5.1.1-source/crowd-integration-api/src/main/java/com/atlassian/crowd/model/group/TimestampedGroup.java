package com.atlassian.crowd.model.group;

import com.atlassian.crowd.model.TimestampedEntity;

/**
 * Extends the Group interface with "updated date" and "created date".
 * This is used for working with Internal Groups, but is not supported for LDAP.
 * <p>
 * The "updated date" is used during our DB backed cache refresh to check if the cached Group is newer than the incoming LDAP Group.
 */
public interface TimestampedGroup extends Group, TimestampedEntity
{
}
