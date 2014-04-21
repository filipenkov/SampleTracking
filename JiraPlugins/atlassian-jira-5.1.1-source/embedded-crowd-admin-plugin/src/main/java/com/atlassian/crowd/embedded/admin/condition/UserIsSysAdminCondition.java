package com.atlassian.crowd.embedded.admin.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.user.UserManager;

import java.util.Map;

/**
 * Web Fragment condition for seeing if a user is a System Admin
 *
 * @since v1.0
 */
public class UserIsSysAdminCondition implements Condition
{
    private final UserManager userManager;

    public UserIsSysAdminCondition(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    public boolean shouldDisplay(final Map<String, Object> context)
    {
        String userName = userManager.getRemoteUsername();
        if (userName == null)
        {
            return false;
        }
        return userManager.isSystemAdmin(userName);
    }
}
