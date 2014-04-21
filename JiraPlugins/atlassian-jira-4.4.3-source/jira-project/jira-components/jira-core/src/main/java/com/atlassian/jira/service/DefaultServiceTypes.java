package com.atlassian.jira.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

public class DefaultServiceTypes implements ServiceTypes
{
    private final InBuiltServiceTypes inBuiltServiceTypes;

    private final PermissionManager permissionManager;

    public DefaultServiceTypes(final InBuiltServiceTypes inBuiltServiceTypes, PermissionManager permissionManager)
    {
        this.inBuiltServiceTypes = inBuiltServiceTypes;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean isCustom(final String serviceClassName)
    {
        return !isInBuilt(serviceClassName);
    }

    @Override
    public boolean isInBuilt(final String serviceClassName)
    {
        return Iterables.any(inBuiltServiceTypes.all(), new MatchByServiceClassNamePredicate(serviceClassName));

    }

    @Override
    public boolean isManageableBy(final User user, final String serviceClassName)
    {
        if (isCustom(serviceClassName))
        {
            if (isAnonymous(user))
            {
                return false;
            }
            else if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if (isInBuilt(serviceClassName))
        {
             return Iterables.any(inBuiltServiceTypes.manageableBy(user), new MatchByServiceClassNamePredicate(serviceClassName));
        }

        return false;
    }

    private boolean isAnonymous(User user) {return user == null;}

    private static class MatchByServiceClassNamePredicate implements Predicate<InBuiltServiceTypes.InBuiltServiceType>
    {
        private final String serviceClassName;

        public MatchByServiceClassNamePredicate(String serviceClassName)
        {
            this.serviceClassName = serviceClassName;
        }

        @Override
        public boolean apply(@Nullable InBuiltServiceTypes.InBuiltServiceType anInBuiltServiceType)
        {
            return anInBuiltServiceType.getType().getName().equals(serviceClassName);
        }
    }

}
