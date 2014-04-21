package com.atlassian.jira.plugin.report.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Responsible for getting SubTasks for parent Issues.
 *
 * @since v3.11
 */
class SubTaskFetcher
{
    private static final Logger log = Logger.getLogger(SubTaskFetcher.class);

    private final SearchProvider searchProvider;

    SubTaskFetcher(SearchProvider searchProvider)
    {
        this.searchProvider = searchProvider;
    }

    /**
     * Get all subtasks for the List of Issues that the user can see subject to the subtask inclusion (for fixfor
     * version) policy.
     * <p/>
     * Will not return null but an Empty List if nothing found.
     *
     * @param user             for permission checks
     * @param parentIssues     a List of Issues
     * @param subtaskInclusion a String that is one of the {@link com.atlassian.jira.plugin.report.impl.SubTaskIncludeValuesGenerator.Options}
     * @param onlyIncludeUnresolved whether to only include unresolved, or to include both resolved and unresolved issues
     * @return a List of Issues that are subtasks.
     *
     * @throws SearchException if the search subsystem fails.
     */
    List<Issue> getSubTasks(User user, List<Issue> parentIssues, String subtaskInclusion, final boolean onlyIncludeUnresolved) throws SearchException
    {
        if (SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED.equals(subtaskInclusion))
        {
            return Collections.emptyList();
        }
        if (parentIssues == null || parentIssues.isEmpty())
        {
            return Collections.emptyList();
        }

        final JqlClauseBuilder queryBuilder = getSearchForSubTasks(parentIssues, onlyIncludeUnresolved);

        if (SubTaskIncludeValuesGenerator.Options.SELECTED_AND_BLANK.equals(subtaskInclusion))
        {
            queryBuilder.and().fixVersionIsEmpty();
        }
        final SearchResults subtaskSearchResults = searchProvider.search(queryBuilder.buildQuery(), user, new PagerFilter(Integer.MAX_VALUE));
        return subtaskSearchResults.getIssues();
    }

    /**
     * Get all subtasks for the List of Issues that the user can see subject to the subtask inclusion (for assigned
     * user) policy.
     * <p/>
     * Will not return null but an Empty List if nothing found.
     *
     * @param user             for permission checks
     * @param parentIssues     a List of Issues
     * @param subtaskInclusion a String that is one of the {@link com.atlassian.jira.plugin.report.impl.UserSubTaskIncludeValuesGenerator.Options}
     * @param onlyIncludeUnresolved whether to only include unresolved, or to include both resolved and unresolved issues
     * @return a List of Issues that are subtasks.
     *
     * @throws SearchException if the search subsystem fails.
     */
    List<Issue> getSubTasksForUser(User user, List<Issue> parentIssues, String subtaskInclusion, final boolean onlyIncludeUnresolved) throws SearchException
    {
        if (UserSubTaskIncludeValuesGenerator.Options.ONLY_ASSIGNED.equals(subtaskInclusion))
        {
            return Collections.emptyList();
        }
        if (!UserSubTaskIncludeValuesGenerator.Options.ASSIGNED_AND_UNASSIGNED.equals(subtaskInclusion))
        {
            if (log.isInfoEnabled())
            {
                log.info("Unknown Subtask Inclusion parameter: " + subtaskInclusion);
            }
            return Collections.emptyList();
        }
        if (parentIssues == null || parentIssues.isEmpty())
        {
            return Collections.emptyList();
        }

        final JqlClauseBuilder whereClauseBuilder = getSearchForSubTasks(parentIssues, onlyIncludeUnresolved);
        whereClauseBuilder.and().assigneeIsEmpty();
        final SearchResults subtaskSearchResults = searchProvider.search(whereClauseBuilder.buildQuery(), user, new PagerFilter(Integer.MAX_VALUE));
        return subtaskSearchResults.getIssues();
    }

    private JqlClauseBuilder getSearchForSubTasks(List<Issue> parentIssues, final boolean onlyIncludeUnresolved)
    {
        final List<Long> parentIssueIds = CollectionUtil.transform(parentIssues, new Function<Issue, Long>()
        {
            public Long get(final Issue input)
            {
                return input.getId();
            }
        });
        JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().issueParent().inNumbers(parentIssueIds);
        if (onlyIncludeUnresolved)
        {
            builder = builder.and().unresolved();
        }
        return builder;
    }
}
