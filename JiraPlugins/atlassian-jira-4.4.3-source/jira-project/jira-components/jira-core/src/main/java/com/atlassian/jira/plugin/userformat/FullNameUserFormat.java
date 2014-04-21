package com.atlassian.jira.plugin.userformat;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Very simple implementation that only renders the users full name.
 *
 * @since v3.13
 */
public class FullNameUserFormat implements UserFormat
{
    public static final String TYPE = "fullName";

    private UserUtil userUtil;

    public FullNameUserFormat(final UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    public String format(final String username, final String id)
    {
        if (StringUtils.isBlank(username))
        {
            return null;
        }
        final User user = userUtil.getUserObject(username);
        final String formattedUser = user == null ? username : userUtil.getDisplayableNameSafely(user);
        return TextUtils.htmlEncode(formattedUser);
    }

    public String format(final String username, final String id, final Map<String, Object> params)
    {
        return format(username, id);
    }
}
