package com.atlassian.upm.license.internal.host;

import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.upm.license.internal.HostApplicationDescriptor;

import com.google.common.collect.Iterators;

import static com.google.common.base.Preconditions.checkNotNull;

public class BambooApplicationDescriptor implements HostApplicationDescriptor
{
    private final BambooUserManager userManager;
    
    public BambooApplicationDescriptor(BambooUserManager userManager)
    {
        this.userManager = checkNotNull(userManager, "userManager");
    }
    
    @Override
    public int getActiveUserCount()
    {
        // The Bamboo API doesn't allow us to get a user count without retrieving the full list.  Also
        // note that there's no such thing as an "inactive" user in Bamboo - per the Bamboo 3.3 docs,
        // the only way to deactivate a user is to change their password to something they don't know.
        //return Iterators.size(userManager.getUserNames().iterator());

        //Due to UPM-1906 the usermanager is not usable from a plugin when Bamboo is connected to LDAP.
        // this is a temporary workaround until we have time to come up with a better solution.
        return 0;
    }
}
