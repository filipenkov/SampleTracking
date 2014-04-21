package com.atlassian.jira.permission;

import com.atlassian.jira.AbstractSimpleI18NBean;

/**
 * Default implementation fo Permission
 */
public class PermissionImpl extends AbstractSimpleI18NBean implements Permission
{

    public PermissionImpl(String id, String name, String description, String nameKey, String descriptionKey)
    {
        super(id, name, description, nameKey, descriptionKey);
    }

    public String getName()
    {
        return name;
    }

    public String getNameKey()
    {
        return nameKey;
    }
}
