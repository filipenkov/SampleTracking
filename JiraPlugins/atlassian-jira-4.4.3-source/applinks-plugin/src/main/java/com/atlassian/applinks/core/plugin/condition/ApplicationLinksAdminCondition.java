package com.atlassian.applinks.core.plugin.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import java.util.Map;

/**
 * Condition which checks if the user has the permission to administrate application links.
 * The user has to be a sysadmin to be able to administrate application links.
 *
 * @since v3.3
 */
public class ApplicationLinksAdminCondition implements Condition
{
    private UserManager userManager;

    public ApplicationLinksAdminCondition(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void init(final Map<String, String> stringStringMap) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> stringObjectMap)
    {
        return userManager.isSystemAdmin(userManager.getRemoteUsername());
    }
}
