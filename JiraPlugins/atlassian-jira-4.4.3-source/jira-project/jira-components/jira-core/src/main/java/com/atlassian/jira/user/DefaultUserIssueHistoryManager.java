package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.NotNull;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A convenience wrapper around the {@link com.atlassian.jira.user.UserHistoryManager} to work directly with issues and
 * perform permission checks
 *
 * @since v4.0
 */
public class DefaultUserIssueHistoryManager implements UserIssueHistoryManager
{
    private static final Logger log = Logger.getLogger(DefaultUserIssueHistoryManager.class);

    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final UserHistoryManager userHistoryManager;
    private final ApplicationProperties applicationProperties;

    public DefaultUserIssueHistoryManager(final UserHistoryManager userHistoryManager, final PermissionManager permissionManager, final IssueManager issueManager, final ApplicationProperties applicationProperties)
    {
        this.userHistoryManager = userHistoryManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.applicationProperties = applicationProperties;
    }

    public void addIssueToHistory(@NotNull final User user, @NotNull final Issue issue)
    {
        notNull("issue", issue);
        userHistoryManager.addItemToHistory(UserHistoryItem.ISSUE, user, issue.getId().toString());
    }

    @Override
    public void addIssueToHistory(com.opensymphony.user.User user, Issue issue)
    {
        addIssueToHistory((User) user, issue);
    }

    public boolean hasIssueHistory(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.ISSUE, user);
        if (history != null)
        {
            for (final UserHistoryItem historyItem : history)
            {
                final Issue issue = issueManager.getIssueObject(Long.valueOf(historyItem.getEntityId()));
                if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasIssueHistory(com.opensymphony.user.User user)
    {
        return hasIssueHistory((User) user);
    }

    @NotNull
    public List<UserHistoryItem> getFullIssueHistoryWithoutPermissionChecks(final User user)
    {
        return userHistoryManager.getHistory(UserHistoryItem.ISSUE, user);
    }

    @Override
    public List<UserHistoryItem> getFullIssueHistoryWithoutPermissionChecks(com.opensymphony.user.User user)
    {
        return getFullIssueHistoryWithoutPermissionChecks((User) user);
    }

    public List<UserHistoryItem> getFullIssueHistoryWithPermissionChecks(final User user)
    {
        return getViewableIssueHistory(user, null);
    }

    @Override
    public List<UserHistoryItem> getFullIssueHistoryWithPermissionChecks(com.opensymphony.user.User user)
    {
        return getFullIssueHistoryWithPermissionChecks((User) user);
    }

    @NotNull
    public List<Issue> getShortIssueHistory(final User user)
    {
        int maxItems = UserIssueHistoryManager.DEFAULT_ISSUE_HISTORY_DROPDOWN_ITEMS;
        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS));
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.dropdown.items'.  Should be a number.");
        }
        return getViewableIssuesFromHistory(user, maxItems);
    }

    @Override
    public List<Issue> getShortIssueHistory(com.opensymphony.user.User user)
    {
        return getShortIssueHistory((User) user);
    }

    /**
     * @param user     the user to retrieve the history of
     * @param maxItems the maximum number of issues to return; use null for no limit
     * @return the user history containing only the issues which the user has permission to view, limited maxItems if specified
     */
    private List<Issue> getViewableIssuesFromHistory(final User user, final Integer maxItems)
    {
        final List<UserHistoryItem> history = getFullIssueHistoryWithoutPermissionChecks(user);
        final List<Issue> returnList = new ArrayList<Issue>();

        if (history != null)
        {
            for (final UserHistoryItem userHistoryItem : history)
            {
                final Issue issue = issueManager.getIssueObject(Long.valueOf(userHistoryItem.getEntityId()));

                if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
                {
                    returnList.add(issue);
                    if ((maxItems != null) && (returnList.size() >= maxItems))
                    {
                        return returnList;
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * @param user     the user to retrieve the history of
     * @param maxItems the maximum number of items to return; use null for no limit
     * @return the user history containing only the issues which the user has permission to view, limited maxItems if specified
     */
    private List<UserHistoryItem> getViewableIssueHistory(final User user, final Integer maxItems)
    {
        final List<UserHistoryItem> history = getFullIssueHistoryWithoutPermissionChecks(user);
        final List<UserHistoryItem> returnList = new ArrayList<UserHistoryItem>();

        if (history != null)
        {
            for (final UserHistoryItem userHistoryItem : history)
            {
                final Issue issue = issueManager.getIssueObject(Long.valueOf(userHistoryItem.getEntityId()));

                if ((issue != null) && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
                {
                    returnList.add(userHistoryItem);
                    if ((maxItems != null) && (returnList.size() >= maxItems))
                    {
                        return returnList;
                    }
                }
            }
        }
        return returnList;
    }
}
