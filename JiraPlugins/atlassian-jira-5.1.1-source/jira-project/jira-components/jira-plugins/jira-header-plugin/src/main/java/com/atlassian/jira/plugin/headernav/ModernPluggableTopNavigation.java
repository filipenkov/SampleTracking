package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.navigation.PluggableTopNavigation;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.google.common.collect.ImmutableMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class ModernPluggableTopNavigation implements PluggableTopNavigation
{
    private TopNavigationModuleDescriptor descriptor;
    private final AvatarService avatarService;
    private final SimpleLinkManager simpleLinkManager;

    public ModernPluggableTopNavigation(AvatarService avatarService, SimpleLinkManager simpleLinkManager)
    {
        this.avatarService = avatarService;
        this.simpleLinkManager = simpleLinkManager;
    }

    public void init(TopNavigationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    @Override
    public String getHtml(HttpServletRequest request)
    {
        Map<String, Object> params =
                ImmutableMap.<String, Object>builder().
                        put("avatars", new Avatars(avatarService)).
                        put("linkManager", simpleLinkManager).
                        build();
        return descriptor.getTopNavigationHtml(request, params);
    }

    public static class Avatars
    {
        private final AvatarService avatarService;

        public Avatars(AvatarService avatarService)
        {
            this.avatarService = avatarService;
        }

        public String getUrl(final User user, final String size)
        {
            return avatarService.getAvatarURL(user, user.getName(), Avatar.Size.valueOf(size)).toString();
        }
    }
}
