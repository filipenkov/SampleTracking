package com.atlassian.jira.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jelly.service.JellyService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.services.DebugService;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.atlassian.jira.user.util.Users.isAnonymous;

public class DefaultInBuiltServiceTypes implements InBuiltServiceTypes
{
    private final static Iterable<InBuiltServiceType> IN_BUILT_SERVICE_TYPES =
            ImmutableSet.of
                    (
                            new InBuiltServiceType(MailFetcherService.class, "admin.services.create.issues.from.mail"),
                            new InBuiltServiceType(FileService.class, "admin.services.create.issues.from.local.files"),
                            new InBuiltServiceType(DebugService.class, "admin.services.debugging.service"),
                            new InBuiltServiceType(JellyService.class, "admin.services.jelly.service"),
                            new InBuiltServiceType(ExportService.class, "admin.services.backup.service")
                    );

    private final static Iterable<InBuiltServiceType> ADMIN_MANAGEABLE_SERVICE_TYPES =
            Iterables.filter(IN_BUILT_SERVICE_TYPES, new Predicate<InBuiltServiceType>()
            {
                @Override
                public boolean apply(@Nullable InBuiltServiceType input)
                {
                    return input != null && input.getType() != null && MailFetcherService.class.isAssignableFrom(input.getType());
                }
            });

    private final PermissionManager permissionManager;

    public DefaultInBuiltServiceTypes(final PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    @Override
    public Iterable<InBuiltServiceType> all()
    {
        return IN_BUILT_SERVICE_TYPES;
    }

    @Override
    public Iterable<InBuiltServiceType> manageableBy(User user)
    {
        if (isAnonymous(user))
        {
            Collections.emptySet();
        }
        if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
        {
            return all();
        }
        else if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return ADMIN_MANAGEABLE_SERVICE_TYPES;
        }
        return Collections.emptySet();
    }
}
