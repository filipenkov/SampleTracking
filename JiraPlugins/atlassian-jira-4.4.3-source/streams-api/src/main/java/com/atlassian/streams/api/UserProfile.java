package com.atlassian.streams.api;

import java.net.URI;

import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Preconditions;

import static com.atlassian.streams.api.common.Option.none;
import static com.google.common.base.Preconditions.checkNotNull;

public class UserProfile
{
    private final String username;
    private final String fullName;
    private final Option<String> email;
    private final Option<URI> profilePageUri;
    private final Option<URI> profilePictureUri;

    UserProfile(Builder builder)
    {
        this.username = builder.username;
        if (builder.fullName != null)
        {
            this.fullName = builder.fullName;
        }
        else
        {
            this.fullName = builder.username;
        }
        this.email = builder.email;
        this.profilePageUri = builder.profilePageUri;
        this.profilePictureUri = builder.profilePictureUri;
    }

    public String getUsername()
    {
        return username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public Option<String> getEmail()
    {
        return email;
    }

    public Option<URI> getProfilePageUri()
    {
        return profilePageUri;
    }

    public Option<URI> getProfilePictureUri()
    {
        return profilePictureUri;
    }

    public static class Builder
    {
        private final String username;
        private String fullName;
        private Option<String> email = none();
        private Option<URI> profilePageUri = none();
        private Option<URI> profilePictureUri = none();

        public Builder(String username)
        {
            this.username = checkNotNull(username, "username");
        }

        public UserProfile build()
        {
            return new UserProfile(this);
        }

        public Builder fullName(String fullName)
        {
            this.fullName = fullName;
            return this;
        }

        public Builder email(Option<String> email)
        {
            this.email = email;
            return this;
        }

        public Builder profilePageUri(Option<URI> profilePageUri)
        {
            this.profilePageUri = checkAbsolute(profilePageUri, "profilePageUri");
            return this;
        }

        public Builder profilePictureUri(Option<URI> profilePictureUri)
        {
            this.profilePictureUri = checkAbsolute(profilePictureUri, "profilePictureUri");
            return this;
        }

        private Option<URI> checkAbsolute(Option<URI> o, String message)
        {
            for (URI uri : o)
            {
                Preconditions.checkAbsolute(uri, message);
            }
            return o;
        }
    }
}
