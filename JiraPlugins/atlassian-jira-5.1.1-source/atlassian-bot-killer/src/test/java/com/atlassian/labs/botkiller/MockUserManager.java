package com.atlassian.labs.botkiller;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserResolutionException;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 */
public class MockUserManager implements UserManager
{
    private final String userName;

    public MockUserManager(String userName)
    {
        this.userName = userName;
    }

    @Override
    public String getRemoteUsername()
    {
        return userName;
    }

    @Override
    public String getRemoteUsername(HttpServletRequest request)
    {
        return userName;
    }

    @Override
    public boolean isUserInGroup(String username, String group)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSystemAdmin(String username)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean authenticate(String username, String password)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Principal resolve(String username) throws UserResolutionException
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
