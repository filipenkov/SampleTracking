package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

/**
 * Converts between User objects and Strings for storage and retrieval of Custom Field values.
 */
public interface UserConverter
{

    public String getString(User user);

    public String getString(com.opensymphony.user.User user);

    /**
     * Get the User Object from the user name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     * @deprecated Use {@link #getUserObject(String stringValue)}. Since 4.3
     */
    public com.opensymphony.user.User getUser(String stringValue) throws FieldValidationException;

    /**
     * Get the User Object from the user name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     */
    public User getUserObject(String stringValue) throws FieldValidationException;

}
