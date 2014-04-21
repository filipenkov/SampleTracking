package com.atlassian.jira.webtest.selenium.harness.util;

/**
 * Provides methods for carring out administration tasks in JIRA.
 */
public interface Administration
{
    void addPermissionForUser(String username, String permissionScheme, String permission);

    void addUserToGroup(String username, String group);

    /**
     * Set JIRA's profiling on or off.
     *
     * @param on or off
     */
    void setProfiling(boolean on);

    void enableAttachments();

    /**
     * Enables attachmenets, setting the max attachment size to the input provided.
     * @param maxSize The max size for attachments
     */
    void enableAttachments(String maxSize);

    void disableAttachments();

    /**
     * Navigates to the admin section and enables time tracking
     */
    void activateTimeTracking();

       /**
     * Obtains the current attachment path configured for JIRA.
     *
     * <p> This method does not check whether Attachments are enabled or not.
     * If JIRA is configured to use the "default" attachment path, then this method still returns the ACTUAL path that is used.
     *
     * <p> The implementation navigates to the Admin Attachments Settings page and screenscrapes, so don't expect to remain on the same page as when you called the method.
     *
     * @return the current attachment path configured for JIRA.
     *
     */
    String getCurrentAttachmentPath();

    /**
     * Set the mime sniffing policy for attachments in the admin section to the setting provided.
     *
     * @param mimeSniffingPolicy the policy to set to
     */
    void setMimeSniffingPolicy(String mimeSniffingPolicy);

    /**
     * Enables GZIP compression in the admin section
     */
    void enableGzipCompression();

    /**
     * Toggles group visibility in the security level dropdown for comments.
     * @param enable To enable groups in the dropdown.
     */
    void toogleCommentGroupVisibility(boolean enable);

    /**
     * Removes a role permission in the default permission scheme
     * @param permissionId The id of the permission
     * @param roleId The role to remove
     */
    void removeRolePermission(int permissionId, int roleId);

    /**
     * Sets the renderer for the field provided in the default field configuration.
     *
     * @param fieldId The fieldid to set the renderer for
     * @param renderer The new renderer to set
     */
    void setRendererForField(String fieldId, String renderer);

    /**
     * Disabled a plugin module via the UPM
     * @param pluginId the upm plugin id
     * @param moduleName the module to disable
     */
    void disablePluginModule(String pluginId, String moduleName);

    /**
     * Enable a plugin module via the UPM
     * @param pluginId the upm plugin id
     * @param moduleName the module to enable
     */
    void enablePluginModule(String pluginId, String moduleName);

    /**
     * Creates a project through the add project page
     * @param name project name
     * @param key project key
     * @param lead project lead
     */
    void createProject(String name, String key, String lead);
}