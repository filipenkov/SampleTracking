package com.atlassian.mail;

/**
 * Used for maintaining thread information in sent emails.
 */
public interface MailThreader
{
    /** Set the In-Reply-To header for an email, so it will appear threaded.
     * @param email The unsent mail to alter
     */
    void threadEmail(Email email);

    /** Store the (MTA-allocated) Message-Id of a <em>sent</em> email, so later
     * emails 'in reply to' this can be threaded.
     * @param email The sent mail whose Message-Id we should record
     */
    void storeSentEmail(Email email);
}
