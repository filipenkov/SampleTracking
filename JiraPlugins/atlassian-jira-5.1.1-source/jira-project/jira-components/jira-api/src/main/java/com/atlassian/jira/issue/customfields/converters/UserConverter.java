package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

/**
 * Converts between User objects and Strings for storage and retrieval of Custom Field values.
 */
@Internal
public interface UserConverter
{

    public String getString(User user);

    /**
     * Get the User Object from the user name.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     */
    public User getUser(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from the user name even when the user is unknown.
     * This is usefull in places where the user needs to be shown, even though they may have disappeared remotely, say from LDAP.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     * @since v4.4.5
     */
    public User getUserEvenWhenUnknown(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from the user name.
     * This will return null if the stringValue is empty.
     * @param stringValue User name
     * @return A User or null if the input parameter is empty
     * @throws FieldValidationException if the input parameter is null
     * @deprecated Use {@link #getUser(String)} instead. Since v5.0.
     */
    public User getUserObject(String stringValue) throws FieldValidationException;

}
