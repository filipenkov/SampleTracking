package com.atlassian.jira.webtest.framework.model.admin;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents custom field. Custom field is a field created by the JIRA Administrator/User with and arbitrary name.
 *
 * @since v4.3
 */
public class CustomAdminField implements AdminField
{
    private static final String CUSTOMFIELD_PREFIX = "customfield_";
    
    private final long id;
    private final String name;
    private final String idString;

    public CustomAdminField(long id, String name)
    {
        this.id = notNull("id", id);
        this.name = notNull("name", name);
        this.idString = CUSTOMFIELD_PREFIX + id;
    }

    /**
     * Custom field's ID follows the convention 'customfield_<i>id</i>', where <i>id</i> is a numeric database ID
     * of the field instance.
     *
     * @return id of this custom field
     */
    @Override
    public String id()
    {
        return idString;
    }

    @Override
    public String fieldName()
    {
        return name;
    }
}
