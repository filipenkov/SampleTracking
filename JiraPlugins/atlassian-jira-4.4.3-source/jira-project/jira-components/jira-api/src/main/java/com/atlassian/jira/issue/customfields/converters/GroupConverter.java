package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

public interface GroupConverter
{
    public String getString(Group group);

    public String getString(com.opensymphony.user.Group group);

    /**
     * Get the Group Object from the group name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     * @deprecated Use {@link #getGroupObject(String stringValue)}. Since 4.3
     */
    public com.opensymphony.user.Group getGroup(String stringValue) throws FieldValidationException;

    /**
     * Get the Group Object from the group name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     */
    public Group getGroupObject(String stringValue) throws FieldValidationException;
}
