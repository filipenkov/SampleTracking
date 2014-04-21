package com.atlassian.jira.scheme;

import java.util.Map;

public interface SchemeTypeManager<T>
{
    String getResourceName();

    /**
     * Gets the type of the Manager for the type T. e.g. NotificationTypeManager.class or IssueSecurityTypeManager.class
     */
    Class getTypeClass(); // TODO generify the managers of scheme types so we can say Class<Manager<T>>

    SchemeType getSchemeType(String id);

    Map<String, T> getSchemeTypes();

    void setSchemeTypes(Map<String, T> schemeType);

    Map<String, T> getTypes();
}
