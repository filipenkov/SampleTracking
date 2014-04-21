package com.atlassian.jira.bc.dataimport;

import com.atlassian.crowd.embedded.api.User;

/**
* Event raised when a data export begins.
* @since v5.0
*/
public class ExportStartedEvent
{
    /**
     * The user that instigated the export. May be null if, for instance, it is
     * triggered by a scheduled job and not a user.
     */
    public final User loggedInUser;

    /**
     * The filename the data is being saved to.
     */
    public final String filename;

    public ExportStartedEvent(final User user, final String filename)
    {
        this.loggedInUser = user;
        this.filename = filename;
    }
}
