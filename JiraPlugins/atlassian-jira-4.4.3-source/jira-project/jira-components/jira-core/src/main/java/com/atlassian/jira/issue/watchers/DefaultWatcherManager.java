package com.atlassian.jira.issue.watchers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.dbc.Null;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DefaultWatcherManager implements WatcherManager
{
    private static final Logger log = Logger.getLogger(DefaultWatcherManager.class);
    public static final String ASSOCIATION_TYPE = "WatchIssue";

    private final UserAssociationStore userAssociationStore;
    private final ApplicationProperties applicationProperties;
    private final IssueIndexManager indexManager;
    private final UserUtil userUtil;

    public DefaultWatcherManager(final UserAssociationStore userAssociationStore, final ApplicationProperties applicationProperties, final IssueIndexManager indexManager, UserUtil userUtil)
    {
        this.userAssociationStore = userAssociationStore;
        this.applicationProperties = applicationProperties;
        this.indexManager = indexManager;
        this.userUtil = userUtil;
    }

    @Override
    public void startWatching(com.opensymphony.user.User user, GenericValue issue)
    {
        updateWatch(true, user, issue);
    }

    public void startWatching(final User user, final GenericValue issue)
    {
        updateWatch(true, user, issue);
    }

    public void stopWatching(final String username, final GenericValue issue)
    {
        updateWatch(false, userUtil.getUserObject(username), issue);
    }

    public void stopWatching(final User user, final Issue issue)
    {
        updateWatch(false, user, issue.getGenericValue());
    }

    @Override
    public void stopWatching(com.opensymphony.user.User user, GenericValue issue)
    {
        updateWatch(false, user, issue);
    }

    public void stopWatching(final User user, final GenericValue issue)
    {
        updateWatch(false, user, issue);
    }

    @Override
    public void stopWatching(com.opensymphony.user.User user, Issue issue)
    {
        updateWatch(false, user, issue.getGenericValue());
    }

    @Override
    public void removeAllWatchesForUser(com.opensymphony.user.User user)
    {
        // Old OSUser Object
        removeAllWatchesForUser((User) user);
    }

    public List<String> getCurrentWatcherUsernames(final Issue issue) throws DataAccessException
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue.getGenericValue());
    }

    public boolean isWatchingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    @Override
    public boolean isWatching(com.opensymphony.user.User user, Issue issue)
    {
        // Old OSUser Object
        return isWatching((User) user, issue);
    }

    public boolean isWatching(final User user, final Issue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no watches for the issue then this dude isn't watching it.
        if (issue.getWatches() == null || issue.getWatches().longValue() == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, issue.getGenericValue());
    }

    @Override
    public boolean isWatching(com.opensymphony.user.User user, GenericValue issue)
    {
        // Old OSUser Object
        return isWatching((User) user, issue);
    }

    // Determine whether the current user is already watching the issue or not
    public boolean isWatching(final User user, final GenericValue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no watches for the issue then this dude isn't watching it.
        if (issue.getLong("watches") == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, issue);
    }

    public Collection<User> getCurrentWatchList(final Issue issue, final Locale userLocale)
    {
        return getCurrentWatchList(issue.getGenericValue(), userLocale);
    }

    // Retrieve collection of users that are currently watching this issue (including the current user)
    public Collection<com.opensymphony.user.User> getCurrentWatchList(final Locale userLocale, final GenericValue issue)
    {
        return OSUserConverter.convertToOSUserList(getCurrentWatchList(issue, userLocale));
    }

    private List<User> getCurrentWatchList(final GenericValue issue, final Locale userLocale)
    {
        final List<User> watchers = userAssociationStore.getUsersFromSink(ASSOCIATION_TYPE, issue);
        Collections.sort(watchers, new UserBestNameComparator(userLocale));
        return watchers;
    }

    public List<String> getCurrentWatcherUsernames(final GenericValue issue) throws DataAccessException
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue);
    }

    private boolean updateWatch(final boolean addWatch, final User user, final GenericValue issue)
    {
        if (validateUpdate(user, issue))
        {
            try
            {
                if (addWatch)
                {
                    if (!isWatching(user, issue))
                    {
                        userAssociationStore.createAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustWatchCount(issue, 1);
                        return true;
                    }
                }
                else
                {
                    if (isWatching(user, issue))
                    {
                        userAssociationStore.removeAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustWatchCount(issue, -1);
                        return true;
                    }
                }
            }
            catch (final GenericEntityException e)
            {
                log.error("Error changing watch association", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Validates that the params andd the system are in a correct state to change a watch
     *
     * @param user  The user who is watching
     * @param issue the issue the user is voting for
     * @return whether or not to go ahead with the watch.
     */
    private boolean validateUpdate(final User user, final GenericValue issue)
    {
        if (issue == null)
        {
            log.error("You must specify an issue.");
            return false;
        }

        if (user == null)
        {
            log.error("You must specify a user.");
            return false;
        }
        return true;
    }

    /**
     * Adjusts the watch count for an issue.
     *
     * @param issue       the issue to change count for
     * @param adjustValue the value to change it by
     * @throws GenericEntityException If there wasa persitence problem
     */

    private void adjustWatchCount(final GenericValue issue, final int adjustValue) throws GenericEntityException
    {
        Long watches = issue.getLong("watches");

        if (watches == null)
        {
            watches = 0L;
        }
        watches = watches + adjustValue;

        if (watches < 0)
        {
            watches = 0L;
        }

        issue.set("watches", watches);
        issue.store();

        try
        {
            indexManager.reIndex(issue);
        }
        catch (final IndexException e)
        {
            log.error("Exception re-indexing issue " + e, e);
        }
    }

    public void removeAllWatchesForUser(final User user)
    {
        Null.not("User", user);
        // Find the Issues that this User watches - we need to reindex them later
        final List<GenericValue> watchedIssues = userAssociationStore.getSinksFromUser(ASSOCIATION_TYPE, user, "Issue");

        userAssociationStore.removeUserAssociationsFromUser(ASSOCIATION_TYPE, user, "Issue");

        for (final GenericValue issue : watchedIssues)
        {
            reindex(issue);
        }
    }

    private void reindex(final GenericValue issue)
    {
        try
        {
            indexManager.reIndex(issue);
        }
        catch (final IndexException e)
        {
            throw new RuntimeException(e);
        }
    }
}
