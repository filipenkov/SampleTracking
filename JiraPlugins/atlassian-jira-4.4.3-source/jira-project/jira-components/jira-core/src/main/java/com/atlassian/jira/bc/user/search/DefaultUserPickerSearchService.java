package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class DefaultUserPickerSearchService implements UserPickerSearchService
{
    private static final Logger log = Logger.getLogger(DefaultUserPickerSearchService.class);
    private static final String RUNNING_USER_PICKER_SEARCH = "Running user-picker search: ";

    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;

    /**
     * We lazy load the count of users only once.  Its not calculated each call for performance reasons.
     */
    private final LazyReference<Integer> allUsersSize = new LazyReference<Integer>()
    {
        @Override
        protected Integer create() throws Exception
        {
            return userManager.getTotalUserCount();
        }
    };

    /**
     * Constructs a DefaultUserPickerSearchService
     *
     * @param userManager              the UserUtil needed
     * @param applicationProperties the ApplicationProperties
     * @param permissionManager     needed to resolve permissions
     */
    public DefaultUserPickerSearchService(final UserManager userManager, final ApplicationProperties applicationProperties, final PermissionManager permissionManager)
    {
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
    }

    public List<User> findUsers(final JiraServiceContext jiraServiceContext, final String query)
    {
        if (StringUtils.isBlank(query))
        {
            return Collections.emptyList();
        }

        return findUsersAllowEmptyQuery(jiraServiceContext, query);
    }

    public Collection<com.opensymphony.user.User> getResults(final JiraServiceContext jiraServiceContext, final String query)
    {
        if (StringUtils.isBlank(query))
        {
            return Collections.emptyList();
        }

        return getResultsSearchForEmptyQuery(jiraServiceContext, query);
    }

    public List<User> findUsersAllowEmptyQuery(final JiraServiceContext jiraServiceContext, final String query)
    {
        // is it allowed?  How did they get here anyway??
        if (!canPerformAjaxSearch(jiraServiceContext))
        {
            return Collections.emptyList();
        }

        final String convertedQuery = (query == null) ? "" : query;
        UtilTimerStack.push(RUNNING_USER_PICKER_SEARCH + convertedQuery);
        try
        {
            final List<User> returnUsers = new ArrayList<User>();
            final boolean canShowEmailAddresses = canShowEmailAddresses(jiraServiceContext);
            final Collection<User> allUsers = userManager.getUsers();
            for (final User user : allUsers)
            {
                if (userMatches(user, convertedQuery, canShowEmailAddresses))
                {
                    returnUsers.add(user);
                }
            }
            Collections.sort(returnUsers, new UserBestNameComparator(jiraServiceContext.getI18nBean().getLocale()));
            return returnUsers;
        }
        finally
        {
            UtilTimerStack.pop(RUNNING_USER_PICKER_SEARCH + convertedQuery);
        }
    }

    public Collection<com.opensymphony.user.User> getResultsSearchForEmptyQuery(final JiraServiceContext jiraServiceContext, final String query)
    {
        // is it allowed?  How did they get here anyway??
        if (!canPerformAjaxSearch(jiraServiceContext))
        {
            return Collections.emptyList();
        }

        final String actualQuery = (query == null) ? "" : query;
        UtilTimerStack.push(RUNNING_USER_PICKER_SEARCH + actualQuery);
        try
        {
            final List<com.opensymphony.user.User> returnUsers = new ArrayList<com.opensymphony.user.User>();
            final boolean canShowEmailAddresses = canShowEmailAddresses(jiraServiceContext);
            final Collection<com.opensymphony.user.User> users = getAllUsers();
            for (final com.opensymphony.user.User user : users)
            {
                if (userMatches(user, actualQuery, canShowEmailAddresses))
                {
                    returnUsers.add(user);
                }
            }
            Collections.sort(returnUsers, new UserBestNameComparator(jiraServiceContext.getI18nBean().getLocale()));
            return returnUsers;
        }
        finally
        {
            UtilTimerStack.pop(RUNNING_USER_PICKER_SEARCH + actualQuery);
        }
    }

    /**
     * Method to compare User parts (username, Full Name and email) with a query string
     *
     * @param user                  The user to test. User Cannot be null.
     * @param query                 The query to compare. Query can not be null.  Empty string will return true for all.
     * @param canShowEmailAddresses Whether email should be searched
     * @return true if any part matches the query string
     */
    boolean userMatches(final User user, String query, final boolean canShowEmailAddresses)
    {
        query = query.toLowerCase().trim();
        String userPart = user.getName();
        if (StringUtils.isNotBlank(userPart) && userPart.toLowerCase().startsWith(query))
        {
            return true;
        }

        if (canShowEmailAddresses)
        {
            userPart = user.getEmailAddress();
            if (StringUtils.isNotBlank(userPart) && userPart.toLowerCase().startsWith(query))
            {
                return true;
            }
        }

        userPart = user.getDisplayName();
        if (StringUtils.isNotBlank(userPart))
        {
            if (userPart.toLowerCase().startsWith(query))
            {
                return true;
            }
            final StringTokenizer tokenizer = new StringTokenizer(userPart.toLowerCase());
            while (tokenizer.hasMoreElements())
            {
                userPart = tokenizer.nextToken();
                if (userPart.startsWith(query))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return returns all users available
     */
    Collection<com.opensymphony.user.User> getAllUsers()
    {
        return userManager.getAllUsers();
    }

    private static final String VISIBILITY_PUBLIC = "show";
    private static final String VISIBILITY_USER = "user";
    private static final String VISIBILITY_MASKED = "mask";

    /**
     * @see UserPickerSearchService#canShowEmailAddresses(com.atlassian.jira.bc.JiraServiceContext)
     */
    public boolean canShowEmailAddresses(final JiraServiceContext jiraServiceContext)
    {
        if (canPerformAjaxSearch(jiraServiceContext))
        {
            final String emailVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
            if (VISIBILITY_PUBLIC.equals(emailVisibility) || (VISIBILITY_MASKED.equals(emailVisibility)) || (VISIBILITY_USER.equals(emailVisibility) && (jiraServiceContext.getUser() != null)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see UserPickerSearchService#canPerformAjaxSearch(com.atlassian.jira.bc.JiraServiceContext)
     */
    public boolean canPerformAjaxSearch(final JiraServiceContext jiraServiceContext)
    {
        if (isAjaxSearchEnabled())
        {
            final User user = jiraServiceContext.getUser();
            return permissionManager.hasPermission(Permissions.USER_PICKER, user);
        }
        else
        {
            return false;
        }
    }

    public boolean isAjaxSearchEnabled()
    {
        int limit = 0;
        try
        {
            limit = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT));
        }
        catch (final NumberFormatException nfe)
        {
            log.warn("'" + APKeys.JIRA_AJAX_USER_PICKER_LIMIT + "' is not a integer value.", nfe);
        }

        try
        {
            // there is a cost in getting the count of users so we short cut if we can
            if (limit > 0)
            {
                final int userCount = allUsersSize.get();
                return limit >= userCount;
            }
        }
        catch (final Exception e)
        {
            log.error("Error retrieving user count.", e);
        }
        return false;
    }
}