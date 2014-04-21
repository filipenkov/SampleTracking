package com.atlassian.upm.license.internal.host;

import java.lang.reflect.Method;

import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.upm.license.internal.HostApplicationDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfluenceApplicationDescriptor implements HostApplicationDescriptor
{
    private final UserAccessor userAccessor;
    
    public ConfluenceApplicationDescriptor(UserAccessor userAccessor)
    {
        this.userAccessor = checkNotNull(userAccessor, "userAccessor");
    }
    
    @Override
    public int getActiveUserCount()
    {
        // The method UserAccessor.countUsersWithConfluenceAccess is more efficient than querying all
        // the users and then counting them, but it only exists in Confluence >= 3.5.1.  This reflection
        // hack was borrowed from Team Calendars.

        Class<?> userAccessorClass = userAccessor.getClass();
        try
        {
            Method countUsersWithConfluenceAccessMethod = userAccessorClass.getMethod("countUsersWithConfluenceAccess");
            return (Integer) countUsersWithConfluenceAccessMethod.invoke(userAccessor);
        }
        catch (Exception e)
        {
            return getActiveUserCountFromConfluenceDeprecated();
        }
    }
    
    private int getActiveUserCountFromConfluenceDeprecated()
    {
        return userAccessor.getUsersWithConfluenceAccessAsList().size();
    }
}
