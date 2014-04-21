package com.atlassian.jira.user;

import com.opensymphony.module.propertyset.PropertySet;
import com.atlassian.crowd.embedded.api.User;

/**
 * The manager allows the caller to get the {@link com.opensymphony.module.propertyset.PropertySet} associated with a user.
 * Property sets are live objects and changes to the property set are persisted when they occur.
 *
 * @since v4.3
 */

public interface UserPropertyManager
{

    /**
     * Get the property set associated with a user.
     * @param user the property set is associated with.
     * @return Property set.
     */
    PropertySet getPropertySet(User user);

}
