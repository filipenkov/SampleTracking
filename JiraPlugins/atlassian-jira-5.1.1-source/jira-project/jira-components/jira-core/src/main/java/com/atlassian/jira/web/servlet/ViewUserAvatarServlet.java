package com.atlassian.jira.web.servlet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static com.atlassian.jira.avatar.Avatar.Size.LARGE;
import static com.atlassian.jira.avatar.Avatar.Size.SMALL;
import static com.atlassian.jira.avatar.Avatar.Type;
import static org.tuckey.web.filters.urlrewrite.utils.StringUtils.isBlank;

/**
 * Serves avatar images for users.
 *
 * @since v4.2
 */
public class ViewUserAvatarServlet extends AbstractAvatarServlet
{
    @Override
    protected void defaultDoGet(HttpServletRequest request, HttpServletResponse response, String ownerId, Long avatarId, AvatarManager.ImageSize size)
            throws IOException, ServletException
    {
        AvatarService avatarService = ComponentAccessor.getAvatarService();
        if (avatarService != null && avatarService.isGravatarEnabled() && !isBlank(ownerId))
        {
            doGetGravatar(response, ownerId, size, avatarService);
            return;
        }

        super.defaultDoGet(request, response, ownerId, avatarId, size);
    }

    @Override
    protected Long validateInput(String ownerId, Long avatarId, final HttpServletResponse response) throws IOException
    {
        UserPropertyManager userPropertyManager = ComponentManager.getComponent(UserPropertyManager.class);
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
            if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getLoggedInUser(), Type.USER, ownerId))
            {
                // no permission to see any avatar for this user. Simply return the default!
                avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
            }

            if (avatarId == null)
            {
                final PropertySet userPropertySet = userPropertyManager.getPropertySet(user);
                if (userPropertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
                {
                    avatarId = userPropertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY);
                }
                else
                {
                    avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
                }
            }
        }
        return avatarId;
    }

    private void doGetGravatar(HttpServletResponse response, String ownerId, AvatarManager.ImageSize size, AvatarService avatarService)
            throws IOException
    {
        if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getLoggedInUser(), Type.USER, ownerId))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        User loggedInUser = getAuthenticationContext().getLoggedInUser();
        Avatar.Size gravatarSize = size == AvatarManager.ImageSize.SMALL ? SMALL : LARGE;
        URI gravatarURL = avatarService.getAvatarURL(loggedInUser, ownerId, gravatarSize);

        response.sendRedirect(gravatarURL.toString());
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
