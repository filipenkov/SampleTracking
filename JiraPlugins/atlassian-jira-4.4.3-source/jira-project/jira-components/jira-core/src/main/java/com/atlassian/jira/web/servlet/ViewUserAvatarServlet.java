package com.atlassian.jira.web.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves avatar images for users.
 *
 * @since v4.2
 */
public class ViewUserAvatarServlet extends AbstractAvatarServlet
{
    protected Long validateInput(String ownerId, Long avatarId, final HttpServletResponse response) throws IOException
    {
        if (StringUtils.isBlank(ownerId) && avatarId == null)
        {
            // no owner id or avatarId
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No avatar requested");
            return null;
        }
        else if (StringUtils.isNotBlank(ownerId))
        {
            final User user = getUserUtil().getUser(ownerId);
            if (user == null)
            {
                //return the anonymous avatar if the user can't be found! Maybe the case for deleted users/anonymous users
                return Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_ANONYMOUS_USER_AVATAR_ID));
            }
            if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getUser(), Avatar.Type.USER, ownerId))
            {
                // no permission to see any avatar for this user. Simply return the default!
                avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
            }

            if (avatarId == null)
            {
                final PropertySet userPropertySet = user.getPropertySet();
                if (userPropertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
                {
                    avatarId =  userPropertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY);
                }
                else
                {
                    avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
                }
            }
        }
        return avatarId;
    }


    @Override
    protected String getOwnerIdParamName()
    {
        return "ownerId";
    }

    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }
}
