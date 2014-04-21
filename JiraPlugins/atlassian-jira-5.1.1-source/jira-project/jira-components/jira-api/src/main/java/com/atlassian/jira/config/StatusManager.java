package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.status.Status;

import java.util.Collection;

/**
 *
 * Manager for {@link Status}es.
 *
 * @since v5.0
 */
@PublicApi
public interface StatusManager
{
    /**
     * Creates a new status.
     *
     * @param name name of the status. Cannot be blank or null and has to be unique.
     * @param description description of the status.
     * @param iconUrl icon url for this status. Cannot be blank or null.
     *
     * @return the new {@link Status}.
     */
    Status createStatus(String name, String description, String iconUrl);

    /**
     * Edit an existing status.
     *
     * @param status status to edit.
     * @param name  new name. Has to be unique.
     * @param description new description
     * @param iconUrl new icon url
     */
    void editStatus(Status status, String name, String description, String iconUrl);

    /**
     * @return all {@link Status}es
     */
    Collection<Status> getStatuses();

    /**
     * Removes a status.
     *
     * @param id status id
     * @throws IllegalArgumentException if this status is associated with any workflow.
     */
    void removeStatus(String id);

    /**
     * Get a status by id.
     *
     * @param id status id
     * @return the {@link Status}, or null if no status with this id exists.
     */
    Status getStatus(String id);

}
