package com.atlassian.upm.permission;

/**
 * Used to determine visible UPM tabs based on a users permissions.
 */
public interface UpmVisibility
{
    /**
     * @return true if you can see the manage existing tab, false otherwise
     */
    boolean isManageExistingVisible();

    /**
     * @return true if you can see the update tab, false otherwise
     */
    boolean isUpdateVisible();

    /**
     * @return true if you can see the install tab, false otherwise
     */
    boolean isInstallVisible();

    /**
     * @return true if you can see the compatibility tab, false otherwise
     */
    boolean isCompatibilityVisible();

    /**
     * @return true if you can see the OSGi tab, false otherwise
     */
    boolean isOsgiVisible();

    /**
     * @return true if you can see the audit log tab, false otherwise
     */
    boolean isAuditLogVisible();

    /**
     * @return true if dev mode is enabled, false otherwise
     */
    boolean isDevModeEnabled();
}
