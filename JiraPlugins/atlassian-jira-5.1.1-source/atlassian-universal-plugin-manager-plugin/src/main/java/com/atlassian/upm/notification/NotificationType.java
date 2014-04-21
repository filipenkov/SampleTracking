package com.atlassian.upm.notification;

import com.atlassian.upm.permission.Permission;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the different kinds of notifications.
 */
public enum NotificationType
{
    PLUGIN_UPDATE_AVAILABLE("update", Permission.MANAGE_PLUGIN_INSTALL),
    EXPIRED_EVALUATION_PLUGIN_LICENSE("evaluation.expired", Permission.MANAGE_PLUGIN_LICENSE),
    NEARLY_EXPIRED_EVALUATION_PLUGIN_LICENSE("evaluation.nearlyexpired", Permission.MANAGE_PLUGIN_LICENSE),
    USER_MISMATCH_PLUGIN_LICENSE("user.mismatch", Permission.MANAGE_PLUGIN_LICENSE),
    MAINTENANCE_EXPIRED_PLUGIN_LICENSE("maintenance.expired", Permission.MANAGE_PLUGIN_LICENSE),
    MAINTENANCE_NEARLY_EXPIRED_PLUGIN_LICENSE("maintenance.nearlyexpired", Permission.MANAGE_PLUGIN_LICENSE);

    private final String key;
    private final Permission permission;

    private NotificationType(String key, Permission permission)
    {
        this.key = checkNotNull(key, "key");
        this.permission = checkNotNull(permission, "permission");
    }

    public String getKey()
    {
        return key;
    }

    public String getTitleI18nKey()
    {
        return "upm.notification." + getKey() + ".title";
    }

    public String getSingularMessageI18nKey()
    {
        return "upm.notification." + getKey() + ".body.singular";
    }

    public String getPluralMessageI18nKey()
    {
        return "upm.notification." + getKey() + ".body.plural";
    }

    public String getIndividualNotificationI18nKey()
    {
        return "upm.notification." + getKey() + ".body.individual";
    }

    public Permission getRequiredPermission()
    {
        return permission;
    }

    public static NotificationType fromKey(String key)
    {
        for (NotificationType type : values())
        {
            if (type.getKey().equals(key))
            {
                return type;
            }
        }
        return null;
    }
}
