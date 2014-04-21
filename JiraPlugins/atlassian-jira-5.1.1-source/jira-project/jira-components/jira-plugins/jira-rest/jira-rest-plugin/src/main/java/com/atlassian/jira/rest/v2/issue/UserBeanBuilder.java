package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.TimeZone;

/**
 * Builder for UserBean instances.
 *
 * @since v4.2
 */
public class UserBeanBuilder
{
    /**
     * The context.
     */
    private UriInfo context;

    /**
     * The User.
     */
    private User user;
    private String username;

    private List<String> groups;
    /**
     * The currently logged in user.
     */
    private User loggedInUser;

    /**
     * Indicates whether the logged in user has been set.
     */
    private boolean loggedInUserIsSet;

    /**
     * The EmailFormatter.
     */
    private EmailFormatter emailFormatter;

    /**
     * The time zone of the user.
     */
    private TimeZone timeZone;

    /**
     * The AvatarService.
     */
    private AvatarService avatarService;

    /**
     * No-arg constructor.
     */
    public UserBeanBuilder()
    {
    }

    /**
     * Sets the User.
     *
     * @param user a User
     * @return this
     */
    public UserBeanBuilder user(User user)
    {
        this.user = user;
        return this;
    }

    /**
     * Sets the user using a username and UserManager.
     * If the given User no longer exists, we still create a UserBean with the given username
     *
     * @param username The username
     * @param userManager The UserManager
     * @return this Builder
     */
    public UserBeanBuilder user(final String username, final UserManager userManager)
    {
        this.username = username;
        this.user = userManager.getUser(username);
        return this;
    }

    /**
     * Sets the groups that this user belongs to.
     *
     * @param groups the groups that this user belongs to.
     * @return this
     */
    public UserBeanBuilder groups(List<String> groups)
    {
        this.groups = groups;
        return this;
    }

    /**
     * Sets the currently logged in user.
     *
     * @param loggedInUser a User
     * @return this
     */
    public UserBeanBuilder loggedInUser(User loggedInUser)
    {
        this.loggedInUser = loggedInUser;
        loggedInUserIsSet = true;
        return this;
    }

    /**
     * Sets the context.
     *
     * @param context a UriInfo
     * @return this
     */
    public UserBeanBuilder context(UriInfo context)
    {
        this.context = context;
        return this;
    }

    public UserBeanBuilder timeZone(TimeZone timeZone)
    {
        if (timeZone != null)
        {
            this.timeZone = timeZone;
        }

        return this;
    }

    /**
     * Sets the EmailFormatter to use for users' email addresses.
     *
     * @param emailFormatter an EmailFormatter
     * @return this
     */
    public UserBeanBuilder emailFormatter(EmailFormatter emailFormatter)
    {
        this.emailFormatter = emailFormatter;
        return this;
    }

    /**
     * Sets the AvatarService to use for constructing the avatar URL.
     *
     * @param avatarService an AvatarService
     * @return this
     */
    public UserBeanBuilder avatarService(AvatarService avatarService)
    {
        this.avatarService = avatarService;
        return this;
    }

    /**
     * Returns a new UserBean with the name, self, and author properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildShort()
    {
        if (user != null)
        {
            if (context == null) { throw new IllegalStateException("context not set"); }

            return new UserBean(
                    createSelfLink(),
                    user.getName(),
                    user.getDisplayName(),
                    user.isActive(),
                    getAvatarURLs()
            );
        }

        return buildSimple();
    }

    /**
     * Returns a new UserBean with all properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildFull()
    {
        if (user == null)
        {
            return buildMid();
        }

        if (context == null) { throw new IllegalStateException("context not set"); }
        if (groups == null) { throw new IllegalStateException("groups not set"); }
        if (emailFormatter == null) { throw new IllegalStateException("emailFormatter not set"); }
        if (!loggedInUserIsSet) { throw new IllegalStateException("loggedInUser not set"); }
        if (timeZone == null) { throw new IllegalStateException("timeZone not set"); }
        if (avatarService == null) { throw new IllegalStateException("avatarService not set"); }

        return new UserBean(
                    createSelfLink(),
                    user.getName(), 
                    user.getDisplayName(),
                    user.isActive(),
                    emailFormatter.formatEmail(user.getEmailAddress(), loggedInUser),
                    Lists.transform(groups, new ToGroupBean()),
                    getAvatarURLs(),
                    timeZone);
    }

    /**
     * Returns a new UserBean with all properties set.
     *
     * @return a new UserBean
     */
    public UserBean buildMid()
    {
        if (user == null)
        {
            return buildSimple();
        }

        if (context == null) { throw new IllegalStateException("context not set"); }
        if (!loggedInUserIsSet) { throw new IllegalStateException("loggedInUser not set"); }
        if (emailFormatter == null) { throw new IllegalStateException("emailFormatter not set"); }
        if (timeZone == null) { throw new IllegalStateException("timeZone not set"); }

        return new UserBean(
                    createSelfLink(),
                    user.getName(),
                    user.getDisplayName(),
                    user.isActive(),
                    emailFormatter.formatEmail(user.getEmailAddress(), loggedInUser),
                    null,
                    getAvatarURLs(),
                    timeZone);
    }

    private UserBean buildSimple()
    {
        if (username == null)
        {
            return null;
        }
        return new UserBean(null, username, null, false, null);
    }

    protected URI createSelfLink()
    {
        return context.getBaseUriBuilder()
                .path(UserResource.class)
                .queryParam("username", "{0}") // JRADEV-3622. Workaround for percent encoding problem.
                .build(user.getName());
    }

    private Map<String, URI> getAvatarURLs()
    {
        final AvatarService avatarService = ComponentAccessor.getAvatarService();

        return MapBuilder.<String, URI>newBuilder()
                .add("16x16", avatarService.getAvatarAbsoluteURL(user, user.getName(), Avatar.Size.SMALL))
                .add("48x48", avatarService.getAvatarAbsoluteURL(user, user.getName(), Avatar.Size.LARGE))
                .toMap();
    }

    /**
     * Functor that converts a group name to a GroupBean.
     */
    class ToGroupBean implements Function<String, GroupBean>
    {
        public GroupBean apply(@Nullable String groupName)
        {
            return new GroupBean(groupName);
        }
    }
}
