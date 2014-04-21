package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.util.concurrent.NotNull;

import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * A general purpose immutable implementation of the User interface.
 *
 * <b>Note</b>: This object does not allow null username or directoryId.
 */
public class ImmutableUser implements User, Serializable
{
    private static final long serialVersionUID = -4472223017071267465L;

    private final long directoryId;
    private final String name;
    private final boolean active;
    private final String emailAddress;
    private final String displayName;

    public ImmutableUser(final long directoryId, @NotNull final String name, final String displayName, final String emailAddress, final boolean active)
    {
        this.directoryId = directoryId;
        this.name = Preconditions.checkNotNull(name);
        this.displayName = displayName == null ? "" : displayName;
        this.emailAddress = emailAddress;
        this.active = active;
    }

    public boolean isActive()
    {
        return active;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public boolean equals(final Object o)
    {
        return UserComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return UserComparator.hashCode(this);
    }

    public int compareTo(final User other)
    {
        return UserComparator.compareTo(this, other);
    }

    public static Builder newUser()
    {
        return new Builder();
    }

    /**
     * Creates a new User Builder by cloning the values from the supplied User.
     *
     * @param user user to be cloned.
     * @return  a User Builder containing the values from the supplied User.
     */
    public static Builder newUser(final User user)
    {
        final Builder builder = newUser().directoryId(user.getDirectoryId());
        builder.name(user.getName());
        builder.active(user.isActive());
        builder.displayName(user.getDisplayName());
        builder.emailAddress(user.getEmailAddress());

        return builder;
    }

    /**
     * Used to aid in the construction of an Immutable User object.
     */
    public static final class Builder
    {
        private long directoryId = -1;
        private String name;
        private String displayName;
        private String emailAddress;
        private boolean active = true;

        /**
         * Returns an immutable User object with the properties set in this builder.
         * @return an immutable User object with the properties set in this builder.
         */
        public User toUser()
        {
            return new ImmutableUser(directoryId, name, displayName, emailAddress, active);
        }

        //------------------------------
        // Getters and Setters
        //------------------------------
        public Builder directoryId(final long directoryId)
        {
            this.directoryId = directoryId;
            return this;
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder displayName(final String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        public Builder emailAddress(final String emailAddress)
        {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder active(final boolean active)
        {
            this.active = active;
            return this;
        }
    }
}
