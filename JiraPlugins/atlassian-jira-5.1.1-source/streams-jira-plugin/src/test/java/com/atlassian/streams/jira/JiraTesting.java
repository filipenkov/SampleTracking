package com.atlassian.streams.jira;

import java.net.URI;

import com.atlassian.sal.api.user.UserProfile;

class JiraTesting
{
    static UserProfile mockUserProfile(final String username)
    {
        return new UserProfile()
        {
            public String getUsername()
            {
                return username;
            }

            public String getFullName()
            {
                return null;
            }

            public String getEmail()
            {
                return null;
            }

            public URI getProfilePictureUri(int width, int height)
            {
                return getProfilePictureUri();
            }

            public URI getProfilePictureUri()
            {
                return URI.create("/secure/useravatar?avatarId=0");
            }

            public URI getProfilePageUri()
            {
                return URI.create("/secure/ViewProfile.jspa?name=" + username);
            }
        };
    }
}
