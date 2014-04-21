package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Permissions, including global permissions
 * and permission schemes.
 *
 * See PermissionsBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class PermissionsControl extends BackdoorControl<PermissionsControl>
{
    public PermissionsControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void addGlobalPermission(final int permissionType, final String group)
    {
        get(createResource().path("permissions/global/add")
                .queryParam("type", "" + permissionType)
                .queryParam("group", group));
    }

    public void removeGlobalPermission(final int permissionType, final String group)
    {
        get(createResource().path("permissions/global/remove")
                .queryParam("type", "" + permissionType)
                .queryParam("group", group));
    }
}
