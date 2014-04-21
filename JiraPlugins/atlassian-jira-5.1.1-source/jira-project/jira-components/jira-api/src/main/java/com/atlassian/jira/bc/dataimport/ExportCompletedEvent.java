package com.atlassian.jira.bc.dataimport;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;

/**
* Event fired when an export is complete.
* @since v5.0
*/
public class ExportCompletedEvent
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

    /**
     * The outcome of the export. Will contain success or failure and possibly some error messages.
     */
    public final ServiceOutcome<Void> outcome;

    public ExportCompletedEvent(final User user, final String filename, final ServiceOutcome<Void> outcome)
    {
        this.loggedInUser = user;
        this.filename = filename;
        this.outcome = outcome;
    }
}
