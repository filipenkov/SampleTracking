package com.atlassian.crowd.event.user;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.embedded.api.Directory;

/**
 * An Event that is fired when a user (principal) changes their password (credential).
 */
public class UserCredentialUpdatedEvent extends DirectoryEvent
{
    private final String username;
    private final PasswordCredential newCredential;

    public UserCredentialUpdatedEvent(Object source, Directory directory, String username, PasswordCredential newCredential)
    {
        super(source, directory);

        this.username = username;
        this.newCredential = newCredential;
    }

    public String getUsername()
    {
        return username;
    }

    public PasswordCredential getNewCredential()
    {
        return newCredential;
    }
}
