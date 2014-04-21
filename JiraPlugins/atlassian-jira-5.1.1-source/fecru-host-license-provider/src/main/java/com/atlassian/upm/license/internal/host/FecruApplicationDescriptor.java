package com.atlassian.upm.license.internal.host;

import com.atlassian.crucible.spi.services.UserService;
import com.atlassian.upm.license.internal.HostApplicationDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

public class FecruApplicationDescriptor implements HostApplicationDescriptor
{
    private final UserService userService;
    
    public FecruApplicationDescriptor(UserService userService)
    {
        this.userService = checkNotNull(userService, "userService");
    }
    
    @Override
    public int getActiveUserCount()
    {
        // Unfortunately, Fe/Cru doesn't have a way to count active users - all we can do is get a
        // list of all the users and count them ourselves, and the active/inactive property doesn't
        // seem to be available through the API.
        try
        {
            return userService.getAllUsers().size();
        }
        catch (Exception e)
        {
            return 0;
        }
    }
}
