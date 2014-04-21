package com.atlassian.streams.spi;

import com.atlassian.streams.api.UserProfile;

public interface UserProfileAccessor
{
    UserProfile getUserProfile(String username);
    UserProfile getAnonymousUserProfile();
}
