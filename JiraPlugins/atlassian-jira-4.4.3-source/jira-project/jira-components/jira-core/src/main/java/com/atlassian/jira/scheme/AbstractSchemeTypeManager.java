package com.atlassian.jira.scheme;

import com.atlassian.jira.util.JiraTypeUtils;

import java.util.Map;

public abstract class AbstractSchemeTypeManager<T> implements SchemeTypeManager<T>
{
    public abstract String getResourceName();

    public abstract Class getTypeClass();

    /**
     * Get a particular permission type based on the id
     * @param id The Id of the permission type
     * @return The permission type object
     */
    public SchemeType getSchemeType(String id)
    {
        return (SchemeType) getTypes().get(id);
    }

    public abstract Map<String, T> getSchemeTypes();

    public abstract void setSchemeTypes(Map<String, T> schemeType);

    /**
     * Get the different types for a scheme.
     * @return Map of scheme types, eg. for permission types:
     *  {"reporter" -> com.atlassian.jira.security.type.CurrentReporter,
     *   "lead" -> com.atlassian.jira.security.type.ProjectLead,
     *   ...
     * }
     */
    public Map<String, T>  getTypes()
    {
        if (getSchemeTypes() == null)
        {
            Map types = JiraTypeUtils.loadTypes(getResourceName(), getTypeClass());
            setSchemeTypes(types);
        }
        return getSchemeTypes();
    }
}
