package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.StopWatch;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultUserPickerSearchService implements UserPickerSearchService
{
    private static final Logger log = Logger.getLogger(DefaultUserPickerSearchService.class);

    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;

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

    @Override
    public List<User> findUsers(final JiraServiceContext jiraServiceContext, final String query)
    {
        if (StringUtils.isBlank(query))
        {
            return Collections.emptyList();
        }

        return findUsersAllowEmptyQuery(jiraServiceContext, query);
    }

    @Override
    public List<User> findUsersAllowEmptyQuery(final JiraServiceContext jiraServiceContext, final String query)
    {
        // is it allowed?  How did they get here anyway??
        if (!canPerformAjaxSearch(jiraServiceContext))
        {
            return Collections.emptyList();
        }

        StopWatch stopWatch = new StopWatch();
        final String convertedQuery = (query == null) ? "" : query.toLowerCase().trim();
        if (log.isDebugEnabled())
            log.debug("Running user-picker search: '" + convertedQuery + "'");
        List<User> returnUsers = new ArrayList<User>();
        final boolean canShowEmailAddresses = canShowEmailAddresses(jiraServiceContext);
        final Collection<User> allUsers = userManager.getUsers();
        if (log.isDebugEnabled())
            log.debug("Found all " + allUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");

        final Predicate<User> userMatcher = new UserMatcherPredicate(convertedQuery, canShowEmailAddresses);
        for (final User user : allUsers)
        {
            if (user.isActive() && userMatcher.apply(user))
            {
                returnUsers.add(user);
            }
        }
        if (log.isDebugEnabled())
            log.debug("Matched " + returnUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");
        Collections.sort(returnUsers, new UserBestNameComparator(jiraServiceContext.getI18nBean().getLocale()));
        if (log.isDebugEnabled())
        {
            log.debug("Sorted top " + returnUsers.size() + " users in " + stopWatch.getIntervalTime() + "ms");
            log.debug("User-picker search completed in " + stopWatch.getTotalTime() + "ms");
        }
        return returnUsers;
    }

    private static final String VISIBILITY_PUBLIC = "show";
    private static final String VISIBILITY_USER = "user";
    private static final String VISIBILITY_MASKED = "mask";

    /**
     * @see UserPickerSearchService#canShowEmailAddresses(com.atlassian.jira.bc.JiraServiceContext)
     */
    @Override
    public boolean canShowEmailAddresses(final JiraServiceContext jiraServiceContext)
    {
        if (canPerformAjaxSearch(jiraServiceContext))
        {
            final String emailVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_EMAIL_VISIBLE);
            if (VISIBILITY_PUBLIC.equals(emailVisibility) || (VISIBILITY_MASKED.equals(emailVisibility)) || (VISIBILITY_USER.equals(emailVisibility) && (jiraServiceContext.getLoggedInUser() != null)))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPerformAjaxSearch(final JiraServiceContext jiraServiceContext)
    {
        User loggedInUser = (jiraServiceContext != null) ? jiraServiceContext.getLoggedInUser() : null;
        return canPerformAjaxSearch(loggedInUser);
    }

    /**
     * @see UserPickerSearchService#canPerformAjaxSearch(com.atlassian.jira.bc.JiraServiceContext)
     */
    @Override
    public boolean canPerformAjaxSearch(final User user)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, user);
    }
}
