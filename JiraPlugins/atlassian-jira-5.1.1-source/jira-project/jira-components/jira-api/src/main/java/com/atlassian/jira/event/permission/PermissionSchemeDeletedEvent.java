package com.atlassian.jira.event.permission;

/**
 * Event indicating a permission scheme has been deleted.
 *
 * @since v5.0
 */
public class PermissionSchemeDeletedEvent
{
    private Long id;

    public PermissionSchemeDeletedEvent(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
}
