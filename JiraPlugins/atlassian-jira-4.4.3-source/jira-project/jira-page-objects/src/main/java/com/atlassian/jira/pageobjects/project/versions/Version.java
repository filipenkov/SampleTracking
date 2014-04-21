package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.jira.pageobjects.project.versions.operations.VersionOperationDropdown;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import java.util.Date;

/**
 * Representation of a Version.
 *
 * @since v4.4
 */
public interface Version
{
    String getName();

    String getDescription();

    Date getReleaseDate();

    EditVersionForm edit(String name);

    boolean isOverdue();

    boolean isArchived();

    boolean isReleased();

    VersionOperationDropdown openOperationsCog();

    TimedQuery<Boolean> hasFinishedVersionOperation();
}
