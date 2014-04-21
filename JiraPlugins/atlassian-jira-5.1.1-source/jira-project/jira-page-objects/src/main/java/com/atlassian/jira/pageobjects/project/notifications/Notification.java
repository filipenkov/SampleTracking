package com.atlassian.jira.pageobjects.project.notifications;

import java.util.List;

/**
 * @since v4.4
 */
public class Notification
{
    private String name;
    private List<String> entities;

    public String getName()
    {
        return name;
    }

    public Notification setName(String name)
    {
        this.name = name;
        return this;
    }

    public List<String> getEntities()
    {
        return entities;
    }

    public Notification setEntities(List<String> entities)
    {
        this.entities = entities;
        return this;
    }
}
