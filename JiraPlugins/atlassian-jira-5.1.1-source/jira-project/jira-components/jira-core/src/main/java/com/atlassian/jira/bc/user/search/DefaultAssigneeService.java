package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.util.concurrent.Nullable;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main implementation of {@link AssigneeService}.
 *
 * @since v5.0
 */
public class DefaultAssigneeService implements AssigneeService
{
    private final PermissionContextFactory permissionContextFactory;
    private final PermissionSchemeManager permissionSchemeManager;
    private final UserManager userManager;
    private final UserHistoryManager userHistoryManager;
    private ChangeHistoryManager changeHistoryManager;
    private final FeatureManager featureManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultAssigneeService(PermissionContextFactory permissionContextFactory,
            PermissionSchemeManager permissionSchemeManager, UserManager userManager, UserHistoryManager userHistoryManager,
            FeatureManager featureManager, final JiraAuthenticationContext authenticationContext) {
        this.permissionContextFactory = permissionContextFactory;
        this.permissionSchemeManager = permissionSchemeManager;
        this.userManager = userManager;
        this.userHistoryManager = userHistoryManager;
        this.featureManager = featureManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public List<User> getSuggestedAssignees(Issue issue, @Nullable User loggedInUser, @Nullable ActionDescriptor actionDescriptor)
    {
        List<User> assignableUsers = new AssignableUsers(issue, actionDescriptor).findAll();
        List<User> suggestedAssignees = getSuggestedAssignees(issue, loggedInUser, assignableUsers);

        Collections.sort(suggestedAssignees, new UserBestNameComparator(authenticationContext.getLocale()));
        return suggestedAssignees;
    }

    @Override
    public List<User> getSuggestedAssignees(Issue issue, User loggedInUser, List<User> assignableUsers)
    {
        Set<String> suggestedAssigneeNames = getSuggestedAssigneeNames(issue, loggedInUser);
        return getSuggestedAssignees(suggestedAssigneeNames, assignableUsers);
    }

    @Override
    public Collection<User> findAssignableUsers(String query, Issue issue, @Nullable ActionDescriptor actionDescriptor)
    {
        return new AssignableUsers(issue, actionDescriptor).matchingUsername(query).findAllAndSort();
    }

    @Override
    public Collection<User> findAssignableUsers(String query, Project project)
    {
        return new AssignableUsers(project).matchingUsername(query).findAllAndSort();
    }
    
    private List<User> findAssignableUsers(final String query, final Collection<User> assignableUsers)
    {
        List<User> returnUsers = new ArrayList<User>();
        final String convertedQuery = (query == null) ? "" : query.trim().toLowerCase();
        if (StringUtils.isNotBlank(query))
        {
            final Predicate<User> userMatcher = new UserMatcherPredicate(convertedQuery, true);
            for (final User user : assignableUsers)
            {
                if (userMatcher.apply(user))
                {
                    returnUsers.add(user);
                }
            }
        }
        else
        {
            returnUsers = new ArrayList<User>(assignableUsers);
        }

        return returnUsers;
    }

    private Set<String> getSuggestedAssigneeNames(Issue issue, final User loggedInUser)
    {
        Set<String> suggestedAssignees = new HashSet<String>();

        if (loggedInUser != null)
        {
            // HACK - temp only to make old tests pass - only add the logged in user in Frother mode
            if (useFrotherControl())
                suggestedAssignees.add(loggedInUser.getName());
        }
        suggestedAssignees.addAll(getRecentAssigneeNamesForIssue(issue));
        suggestedAssignees.addAll(getRecentAssigneeNamesForUser(loggedInUser));

        // Reporter may be null for new issues.
        User reporter = issue.getReporter();
        if (reporter != null)
        {
            suggestedAssignees.add(reporter.getName());
        }

        return suggestedAssignees;
    }

    private boolean useFrotherControl()
    {
        // The Frother Assignee field breaks some old tests expecting the select element to exist with all user
        // options - allow these tests to run without it in the short term by setting the 'off' flag.
        boolean on = featureManager.isEnabled("frother.assignee.field");
        boolean off = featureManager.isEnabled("no.frother.assignee.field");
        return on && !off;
    }

    /**
     * Given a set of suggested names and an ordered list of assignable users, returns an order list of suggested users.
     */
    public List<User> getSuggestedAssignees(final Set<String> suggestedAssigneeNames, List<User> assignableUsers)
    {
        List<User> suggestedAssignees = new ArrayList<User>();

        if (!suggestedAssigneeNames.isEmpty())
        {
            for (User user : assignableUsers)
            {
                // Whittle away at the suggest collection so that each run of the (potentially-long) loop goes a little faster
                if (suggestedAssigneeNames.remove(user.getName()))
                {
                    suggestedAssignees.add(user);
                }
            }
        }

        return suggestedAssignees;
    }

    public List<User> getAssignableUsers(Project project)
    {
        return new AssignableUsers(project).findAllAndSort();
    }
    
    public List<User> getAssignableUsers(Issue issue, ActionDescriptor actionDescriptor)
    {
        return new AssignableUsers(issue, actionDescriptor).findAllAndSort();
    }
    
    // Removes duplicate users, i.e. users who occur in 2 directories
    private Collection<User> getUniqueUsers(Collection<User> users)
    {
        Map<String,User> uniqueUsers = new HashMap<String, User>();
        for (User user : users)
        {
            User realUser = user;
            if (uniqueUsers.containsKey(user.getName()))
            {
                // Get the real user with this name and add him to the list.
                realUser = userManager.getUser(user.getName());
            }
            uniqueUsers.put(user.getName(), realUser);
        }

        return uniqueUsers.values();
    }

    /**
     * Gets ids of this issue's recent assignees, including the current assignee.
     *
     * @param issue and issue to get the change history of
     * @return a set of ids
     */
    public Set<String> getRecentAssigneeNamesForIssue(Issue issue)
    {
        Set<String> recentAssignees = new HashSet<String>();

        List<ChangeItemBean> assigneeHistory = getChangeHistoryManager().getChangeItemsForField(issue, "assignee");

        // Sort by descending date - for the most recent assignees
        Collections.sort(assigneeHistory, new Comparator<ChangeItemBean>()
        {
            @Override
            public int compare(ChangeItemBean changeItemBean1, ChangeItemBean changeItemBean2)
            {
                return changeItemBean2.getCreated().compareTo(changeItemBean1.getCreated());
            }
        });
        for (ChangeItemBean changeItemBean : assigneeHistory)
        {
            // Could be reverse-sorted on date assigned and return a list?
            recentAssignees.add(changeItemBean.getTo());
            if (recentAssignees.size() >= 5)
            {
                break;
            }
        }

        // Assignee may be null for new issues.
        String assigneeId = issue.getAssigneeId();
        if (assigneeId != null)
        {
            recentAssignees.add(assigneeId);
        }

        return recentAssignees;
    }



    // JRA-14128: make a map of the counts of the Full Names of the users,
    // so that we can detect which users have duplicate Full Names
    public Map<String, Boolean> makeUniqueFullNamesMap(Collection<User> users)
    {
        Map<String, Boolean> uniqueFullNames = new HashMap<String, Boolean>();

        for (User user : users)
        {
            String fullName = user.getDisplayName();
            Boolean isUnique = uniqueFullNames.get(fullName);
            if (isUnique == null)
            {
                uniqueFullNames.put(fullName, Boolean.TRUE);
            }
            else
            {
                uniqueFullNames.put(fullName, Boolean.FALSE);
            }
        }
        return uniqueFullNames;
    }

    // Get users that the given user has recently assigned issues to.
    public Set<String> getRecentAssigneeNamesForUser(User remoteUser)
    {
        List<UserHistoryItem> recentUserHistory = new ArrayList<UserHistoryItem>(userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, remoteUser));

        // Sort user history in descending date order
        Collections.sort(recentUserHistory, new Comparator<UserHistoryItem>()
        {
            @Override
            public int compare(UserHistoryItem userHistoryItem1, UserHistoryItem userHistoryItem2)
            {
                return (int)(userHistoryItem2.getLastViewed() - userHistoryItem1.getLastViewed());
            }
        });
        Set<String> recentHistoryAssignees = new HashSet<String>();

        for (UserHistoryItem userHistoryItem : recentUserHistory)
        {
            recentHistoryAssignees.add(userHistoryItem.getEntityId());
            if (recentHistoryAssignees.size() >= 5)
            {
                break;
            }
        }
        return recentHistoryAssignees;
    }

    void setChangeHistoryManager(ChangeHistoryManager changeHistoryManager)
    {
        this.changeHistoryManager = changeHistoryManager;
    }

    private ChangeHistoryManager getChangeHistoryManager()
    {
        if (changeHistoryManager == null)
        {
            setChangeHistoryManager(ComponentAccessor.getChangeHistoryManager());
        }
        return changeHistoryManager;
    }

    /**
     * An "assignable users" query. The results can be obtained in sorted or unsorted form.
     */
    final class AssignableUsers
    {
        private final Issue issue;
        private final PermissionContext ctx;
        private final String matchingUsername;

        AssignableUsers(Project project)
        {
            this.issue = null;
            this.ctx = permissionContextFactory.getPermissionContext(project);
            this.matchingUsername = null;
        }

        AssignableUsers(Issue issue, ActionDescriptor actionDescriptor)
        {
            this.issue = issue;
            this.ctx = permissionContextFactory.getPermissionContext(issue, actionDescriptor);
            this.matchingUsername = null;
        }

        AssignableUsers(Issue issue, PermissionContext ctx, String matchingUsername)
        {
            this.issue = issue;
            this.ctx = ctx;
            this.matchingUsername = matchingUsername;
        }

        public AssignableUsers matchingUsername(String matchingUsername)
        {
            return new AssignableUsers(issue, ctx, matchingUsername);
        }

        public List<User> findAll()
        {
            Collection<User> users = permissionSchemeManager.getUsers((long) Permissions.ASSIGNABLE_USER, ctx);
            Collection<User> assignableUsers = Lists.newArrayList(getUniqueUsers(users));

            // Current assignee can always stay assigned , even if they are now inactive or no longer in the right group
            if (issue != null && issue.getAssignee() != null && !assignableUsers.contains(issue.getAssignee()))
            {
                assignableUsers.add(issue.getAssignee());
            }

            // Optionally filter by the username query
            if (matchingUsername != null)
            {
                assignableUsers.retainAll(findAssignableUsers(matchingUsername, assignableUsers));
            }

            return Lists.newArrayList(assignableUsers);
        }

        public List<User> findAllAndSort()
        {
            List<User> users = findAll();

            // Sort on Full Name (the compareTo in User is on username so we need our own Comparator)
            Collections.sort(users, new UserBestNameComparator(authenticationContext.getLocale()));
            return users;
        }
    }
}
