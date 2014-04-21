package com.atlassian.jira.event.permission;

/**
 * Event indicating a permission entity has been removed from a permission scheme.
 *
 * @since v5.0
 */
public class PermissionDeletedEvent
{
    private Long id;

    public PermissionDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
