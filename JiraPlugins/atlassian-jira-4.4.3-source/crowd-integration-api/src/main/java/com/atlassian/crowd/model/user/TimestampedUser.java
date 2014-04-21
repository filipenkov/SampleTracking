package com.atlassian.crowd.model.user;

import com.atlassian.crowd.model.TimestampedEntity;

/**
 * Extends the user interface with "updated date" and "created date".
 * This is used for working with Internal Users, but is not supported for LDAP.
 * <p>
 * The "updated date" is used during our DB backed cache refresh to check if the cached user is newer than the incoming LDAP user.
 */
public interface TimestampedUser extends User, TimestampedEntity
{
}
