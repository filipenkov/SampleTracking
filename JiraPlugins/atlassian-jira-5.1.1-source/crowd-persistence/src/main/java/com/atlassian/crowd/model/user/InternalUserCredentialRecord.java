package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.PasswordCredential;

import java.io.Serializable;

/**
 * Encapsulates the concept of user credential record.
 */
public class InternalUserCredentialRecord implements Serializable
{
    private Long id;
    private InternalUser user;
    private String passwordHash;

    protected InternalUserCredentialRecord()
    {
    }

    /**
     * Only use this constructor for cloning / importing when the id is known.
     *
     * @param id           id of the object.
     * @param user         user reference.
     * @param passwordHash String password hash.
     */
    public InternalUserCredentialRecord(final Long id, final InternalUser user, final String passwordHash)
    {
        this.id = id;
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public InternalUserCredentialRecord(final InternalUser user, final String passwordHash)
    {
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public Long getId()
    {
        return id;
    }

    private void setId(final Long id)
    {
        this.id = id;
    }

    public InternalUser getUser()
    {
        return user;
    }

    private void setUser(final InternalUser user)
    {
        this.user = user;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    private void setPasswordHash(final String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public PasswordCredential getCredential()
    {
        return new PasswordCredential(passwordHash, true);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof InternalUserCredentialRecord))
        {
            return false;
        }

        InternalUserCredentialRecord that = (InternalUserCredentialRecord) o;

        if (getPasswordHash() != null ? !getPasswordHash().equals(that.getPasswordHash()) : that.getPasswordHash() != null)
        {
            return false;
        }
        if (getUser().getId() != null ? !getUser().getId().equals(that.getUser().getId()) : that.getUser().getId() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getUser().getId() != null ? getUser().getId().hashCode() : 0;
        result = 31 * result + (getPasswordHash() != null ? getPasswordHash().hashCode() : 0);
        return result;
    }
}
