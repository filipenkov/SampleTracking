package com.atlassian.streams.jira.search;

import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsException;

import com.google.common.base.Predicate;

import static com.atlassian.streams.jira.JiraFilterOptionProvider.ISSUE_TYPE;
import static com.atlassian.streams.spi.Filters.caseInsensitive;
import static com.atlassian.streams.spi.Filters.inIssueKeys;
import static com.atlassian.streams.spi.Filters.isAndNot;
import static com.atlassian.streams.spi.Filters.isIn;
import static com.atlassian.streams.spi.Filters.notIn;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Sets.filter;
import static com.google.common.collect.Sets.union;

/**
 * Finds recently updated/created issues that should be put in the activity stream.
 */
public class IssueFinder
{
    private final UserHistory userHistory;
    private final IssueSearch issueSearch;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    public IssueFinder(UserHistory userHistory,
            IssueSearch issueSearch,
            PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext)
    {
        this.userHistory = checkNotNull(userHistory, "userHistory");
        this.issueSearch = checkNotNull(issueSearch, "issueSearch");
        this.permissionManager = checkNotNull(permissionManager, "permissionManager");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
    }

    public Set<Issue> find(final ActivityRequest request) throws StreamsException
    {
        Set<Issue> issues = union(issueSearch.search(request), userHistory.find(request));
        // STRM-1354 STRM-1410 must filter again by issue type and project key (both already done once in JQL)
        // because userHistory needs to be filtered
        return filter(issues, and(issueKey(inIssueKeys(request, caseInsensitive(isIn()), caseInsensitive(notIn()))),
                                  hasBrowsePermission, hasIssueType(request), hasProjectKey(request)));
    }

    private Predicate<Issue> issueKey(Predicate<String> issueKeyPredicate)
    {
        return new IssueKeyPredicateWrapper(issueKeyPredicate);
    }

    private static final class IssueKeyPredicateWrapper implements Predicate<Issue>
    {
        private final Predicate<String> issueKeyPredicate;

        public IssueKeyPredicateWrapper(Predicate<String> issueKeyPredicate)
        {
            this.issueKeyPredicate = issueKeyPredicate;
        }

        public boolean apply(Issue issue)
        {
            return issueKeyPredicate.apply(issue.getKey());
        }
    };

    private final Predicate<Issue> hasBrowsePermission = new Predicate<Issue>()
    {
        public boolean apply(Issue issue)
        {
            return issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, authenticationContext.getLoggedInUser());
        }
    };

    private Predicate<Issue> hasIssueType(ActivityRequest request)
    {
        return new HasIssueType(request);
    }

    private class HasIssueType implements Predicate<Issue>
    {
        private final ActivityRequest request;

        public HasIssueType(ActivityRequest request)
        {
            this.request = request;
        }

        public boolean apply(Issue issue)
        {
            return issue != null && isAndNot(request.getProviderFilters().get(ISSUE_TYPE)).apply(issue.getIssueTypeObject().getId());
        }
    };

    private Predicate<Issue> hasProjectKey(ActivityRequest request)
    {
        return new HasProjectKey(request);
    }

    private class HasProjectKey implements Predicate<Issue>
    {
        private final ActivityRequest request;

        public HasProjectKey(ActivityRequest request)
        {
            this.request = request;
        }

        public boolean apply(Issue issue)
        {
            return issue != null && isAndNot(request.getStandardFilters().get(PROJECT_KEY)).apply(issue.getProjectObject().getKey());
        }
    };
}
