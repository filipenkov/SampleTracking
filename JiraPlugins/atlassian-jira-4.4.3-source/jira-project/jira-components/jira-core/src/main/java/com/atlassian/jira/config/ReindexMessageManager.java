package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;

/**
 * Manages messages for JIRA's administration section that inform users of the need to reindex due to configuration
 * changes.
 *
 * @since v4.0
 */
public interface ReindexMessageManager
{
    /**
     * Pushes a new message. This will replace any existing messages.
     *
     * @param user the user performing the task. May be <code>null</code>, in which case a more generic
     * notification will be displayed
     * @param i18nTask the i18n key of the task being performed.
     */
    void pushMessage(final User user, final String i18nTask);


    /**
     * Clears any current message.
     */
    void clear();

    /**
     * Get current message localised for given <tt>user</tt>.
     *
     * @param user the current user
     * @return the current message, localised for the current user, presented in HTML (and already escaped).
     * null if there is no current message.
     */
    String getMessage(final User user);
}
