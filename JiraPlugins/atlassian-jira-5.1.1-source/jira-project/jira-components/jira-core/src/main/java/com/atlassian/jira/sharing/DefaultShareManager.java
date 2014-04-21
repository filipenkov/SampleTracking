package com.atlassian.jira.sharing;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.util.UserNames;
import com.atlassian.jira.util.dbc.Assertions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of the {@link ShareManager}.
 *
 * @since v3.13
 */
public class DefaultShareManager implements ShareManager
{
    private final SharePermissionStore store;
    private final ShareTypeFactory shareTypeFactory;
    private final SharePermissionReindexer reindexer;

    public DefaultShareManager(final SharePermissionStore store, final ShareTypeFactory shareTypeFactory, final SharePermissionReindexer reindexer)
    {
        checkNotNull(store, "Can not instantiate a ShareManager with a 'null' SharePermissionStore");
        checkNotNull(shareTypeFactory, "Can not instantiate a ShareManager with a 'null' ShareTypeFactory");
        checkNotNull(reindexer, "Can not instantiate a ShareManager with a 'null' SharePermissionReindexer");

        this.store = store;
        this.shareTypeFactory = shareTypeFactory;
        this.reindexer = reindexer;
    }

    public SharePermissions getSharePermissions(final SharedEntity entity)
    {
        return store.getSharePermissions(entity);
    }

    public void deletePermissions(final SharedEntity entity)
    {
        store.deleteSharePermissions(entity);
    }

    public void deleteSharePermissionsLike(final SharePermission permission)
    {
        store.deleteSharePermissionsLike(permission);
        reindexer.reindex(permission);
    }

    public SharePermissions updateSharePermissions(final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.notNull("entity.sharePermissions", entity.getPermissions());

        if (entity.getPermissions().isPrivate())
        {
            store.deleteSharePermissions(entity);
            return SharePermissions.PRIVATE;
        }
        return store.storeSharePermissions(entity);
    }

    @Deprecated
    public boolean hasPermission(final User user, final SharedEntity entity)
    {
        return isSharedWith(user, entity);
    }

    @Override
    public boolean isSharedWith(User user, SharedEntity sharedEntity)
    {
        Assertions.notNull("entity", sharedEntity);

        if ((sharedEntity.getOwnerUserName() != null) && UserNames.equal(sharedEntity.getOwnerUserName(), user))
        {
            return true;
        }
        final SharePermissions permissions = store.getSharePermissions(sharedEntity);
        if (permissions != null)
        {
            for (final SharePermission sharePermission : permissions)
            {
                final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                if ((type != null) && type.getPermissionsChecker().hasPermission(user, sharePermission))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
