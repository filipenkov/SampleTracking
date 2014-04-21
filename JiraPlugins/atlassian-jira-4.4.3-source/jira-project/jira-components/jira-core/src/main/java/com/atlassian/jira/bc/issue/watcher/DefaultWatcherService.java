package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Implementation of WatcherService.
 *
 * @since v4.2
 */
public class DefaultWatcherService implements WatcherService
{
    /**
     * Logger for this DefaultWatcherService instance.
     */
    private final Logger log = LoggerFactory.getLogger(DefaultWatcherService.class);

    /**
     * The ApplicationProperties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * The I18nBean.
     */
    private final I18nBean.BeanFactory i18n;

    /**
     * The PermissionSchemeManager.
     */
    private final PermissionManager permissionManager;

    /**
     * The WatcherManager instance.
     */
    private final WatcherManager watcherManager;

    /**
     * The UserManager instance.
     */
    private final UserManager userManager;

    /**
     * Creates a new DefaultWatcherService with the given dependencies.
     *
     * @param applicationProperties an ApplicationProperties
     * @param i18n a I18nBean
     * @param permissionManager a PermissionManager
     * @param watcherManager a WatcherManager
     * @param userManager a UserManager
     */
    public DefaultWatcherService(ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18n, PermissionManager permissionManager, WatcherManager watcherManager, UserManager userManager)
    {
        this.watcherManager = watcherManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.i18n = i18n;
    }

    /*
     * This method is deprecated and should be removed in 4.4.
     */
    @Override
    public ServiceOutcome<Pair<Integer, List<com.opensymphony.user.User>>> getWatchers(Issue issue, com.opensymphony.user.User remoteUser)
            throws WatchingDisabledException
    {
        Pair<Integer, List<String>> watchers = getWatcherUsernames(issue, remoteUser);

        return ServiceOutcomeImpl.ok(convertUsers(watchers, new OSUserFromName()));
    }

    @Override
    public ServiceOutcome<Pair<Integer, List<User>>> getWatchers(Issue issue, @Nullable User remoteUser)
            throws WatchingDisabledException
    {
        Pair<Integer, List<String>> watchers = getWatcherUsernames(issue, remoteUser);

        return ServiceOutcomeImpl.ok(convertUsers(watchers, new UserFromName()));
    }

    /*
     * This method is deprecated and should be removed in 4.4.
     */
    @Override
    public ServiceOutcome<List<com.opensymphony.user.User>> addWatcher(final Issue issue, com.opensymphony.user.User remoteUser, final com.opensymphony.user.User watcher)
            throws WatchingDisabledException
    {
        ServiceOutcome<List<User>> currentUsers = addWatcher(issue, (User) remoteUser, watcher);

        return ServiceOutcomeImpl.from(currentUsers.getErrorCollection(), newListFrom(currentUsers.getReturnedValue(), new UserToOSUser()));
    }

    @Override
    public ServiceOutcome<List<User>> addWatcher(Issue issue, User remoteUser, User watcher)
            throws WatchingDisabledException
    {
        try
        {
            checkModifyWatchersPermission(issue, remoteUser, watcher);

            watcherManager.startWatching(watcher, issue.getGenericValue());
            return ServiceOutcomeImpl.ok(getCurrentWatchersFor(issue));
        }
        catch (PermissionException e)
        {
            return ServiceOutcomeImpl.error(buildAddWatcherNotAllowedString(issue, remoteUser));
        }
    }

    /*
     * This method is deprecated and should be removed in 4.4.
     */
    @Override
    public ServiceOutcome<List<com.opensymphony.user.User>> removeWatcher(Issue issue, com.opensymphony.user.User remoteUser, com.opensymphony.user.User watcher)
            throws WatchingDisabledException
    {
        ServiceOutcome<List<User>> currentUsers = removeWatcher(issue, (User) remoteUser, watcher);

        return ServiceOutcomeImpl.from(currentUsers.getErrorCollection(), newListFrom(currentUsers.getReturnedValue(), new UserToOSUser()));
    }

    @Override
    public ServiceOutcome<List<User>> removeWatcher(Issue issue, User remoteUser, User watcher)
            throws WatchingDisabledException
    {
        try
        {
            checkModifyWatchersPermission(issue, remoteUser, watcher);

            watcherManager.stopWatching(watcher, issue.getGenericValue());
            return ServiceOutcomeImpl.ok(getCurrentWatchersFor(issue));
        }
        catch (PermissionException e)
        {
            return ServiceOutcomeImpl.error(buildRemoveUserNotAllowedString(issue, remoteUser));
        }
    }

    /**
     * Returns true iff watching is enabled.
     *
     * @return true iff watching is enabled
     */
    @Override
    public boolean isWatchingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    @Override
    public boolean hasViewWatcherListPermission(Issue issue, com.opensymphony.user.User remoteUser)
    {
        return hasViewWatcherListPermission(issue, (User) remoteUser);
    }

    /**
     * Returns true iff the given User has permission to view the watcher list of the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user can view the watch list
     */
    @Override
    public boolean hasViewWatcherListPermission(Issue issue, @Nullable User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, remoteUser) || canEditWatcherList(issue, remoteUser);
    }

    /**
     * Returns a pair containing the watcher count and the watcher usernames for a given issue.
     *
     * @param issue the Issue
     * @param remoteUser the calling User
     * @return a Pair containing the watcher count and the watcher usernames for a given issue
     * @throws WatchingDisabledException if watching is disabled
     */
    protected Pair<Integer, List<String>> getWatcherUsernames(Issue issue, User remoteUser)
            throws WatchingDisabledException
    {
        if (!isWatchingEnabled())
        {
            throw new WatchingDisabledException();
        }

        List<String> watcherNames = watcherManager.getCurrentWatcherUsernames(issue);
        int watcherCount = watcherNames.size();

        // filter out any watchers that the caller is not supposed to see
        if (!hasViewWatcherListPermission(issue, remoteUser))
        {
            if (remoteUser == null)
            {
                watcherNames.clear();
            }
            else
            {
                watcherNames.retainAll(singletonList(remoteUser.getName()));
            }
        }
        log.trace("Visible watchers on issue '{}': {}", issue.getKey(), watcherNames);

        // always return the actual number of watchers, regardless of permissions. this is necessary to remain
        // consistent with the web UI.
        return Pair.of(watcherCount, watcherNames);
    }

    /**
     * Returns a List containing the users that are currently watching an issue.
     *
     * @param issue the Issue to get the watcher list for
     * @return a List of users that are watching the issue
     */
    protected <T> List<User> getCurrentWatchersFor(Issue issue)
    {
        List<String> watcherNames = watcherManager.getCurrentWatcherUsernames(issue.getGenericValue());

        return newListFrom(watcherNames, new UserFromName());
    }

    /**
     * Returns true iff the given User has permission to edit the watcher list of the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a boolean indicating whether the user can edit the watch list
     */
    protected boolean canEditWatcherList(Issue issue, @Nullable User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, remoteUser);
    }

    /**
     * Converts the usernames into User objects using the given function.
     *
     * @param watchers a Pair of watcher count and watcher usernames
     * @param function a Function used for conversion
     * @return a Pair of watcher count and User object
     */
    protected <T extends User> Pair<Integer, List<T>> convertUsers(Pair<Integer, List<String>> watchers, Function<String, T> function)
    {
        return Pair.<Integer, List<T>>of(
                watchers.first(),
                Lists.newArrayList(Lists.transform(watchers.second(), function))
        );
    }

    /**
     * Ensures that the given remoteUser has permission to add or remove the given watcher to/from the issue. Throws an
     * exception if the user does not have permission.
     *
     * @param issue an Issue
     * @param remoteUser a User representing the caller
     * @param watcher a User representing the watcher to add or remove
     * @throws PermissionException if the caller does not have permission to manage watchers, or cannot see the issue
     * @throws WatchingDisabledException if watching is disabled
     */
    protected void checkModifyWatchersPermission(Issue issue, User remoteUser, User watcher)
            throws PermissionException, WatchingDisabledException
    {
        if (!isWatchingEnabled())
        {
            throw new WatchingDisabledException();
        }

        final boolean canView = permissionManager.hasPermission(Permissions.BROWSE, issue, watcher);
        if (!canView)
        {
            throw new PermissionException();
        }

        final boolean canManage = permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, remoteUser);
        if (!canManage && !remoteUser.equals(watcher))
        {
            throw new PermissionException();
        }
    }

    private static class UserToOSUser implements Function<User, com.opensymphony.user.User>
    {
        @Override
        public com.opensymphony.user.User apply(@Nullable User from)
        {
            return OSUserConverter.convertToOSUser(from);
        }
    }

    /**
     * Function object to get a User object from the user name.
     */
    class UserFromName implements Function<String, User>
    {
        public User apply(String username)
        {
            return userManager.getUser(username);
        }
    }

    /**
     * Function object to get a User object from the user name.
     */
    class OSUserFromName implements Function<String, com.opensymphony.user.User>
    {
        public com.opensymphony.user.User apply(String username)
        {
            return userManager.getUser(username);
        }
    }

    /**
     * Thrown if a user does not have permission to manage watchers.
     */
    static class PermissionException extends Exception
    {
        // empty
    }

    /**
     * Returns a localised error message indicating that the caller is not allowed to add a watcher to the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a String containing an error message
     */
    private String buildAddWatcherNotAllowedString(Issue issue, User remoteUser)
    {
        return i18n.getInstance(remoteUser).getText("watcher.service.error.add.watcher.not.allowed", remoteUser.getName(), issue.getKey());
    }

    /**
     * Returns a localised error message indicating that the caller is not allowed to remove a watcher from the issue.
     *
     * @param issue an Issue
     * @param remoteUser a User
     * @return a String containing an error message
     */
    private String buildRemoveUserNotAllowedString(Issue issue, User remoteUser)
    {
        return i18n.getInstance(remoteUser).getText("watcher.service.error.remove.watcher.not.allowed", remoteUser.getName(), issue.getKey());
    }

    /**
     * Creates a new List from another using a Function.
     *
     * @param from the from list
     * @param fn the function
     * @param <F> the from type
     * @param <T> the to type
     * @return a new List
     */
    static <F, T> List<T> newListFrom(List<F> from, Function<F, T> fn)
    {
        return Lists.newArrayList(Lists.transform(from, fn));
    }
}
