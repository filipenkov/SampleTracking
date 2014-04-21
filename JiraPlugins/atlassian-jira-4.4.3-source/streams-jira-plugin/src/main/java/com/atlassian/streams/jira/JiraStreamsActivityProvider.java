package com.atlassian.streams.jira;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsException;
import com.atlassian.streams.api.StreamsFeed;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.jira.search.IssueFinder;
import com.atlassian.streams.jira.util.RenderingUtilities;
import com.atlassian.streams.spi.StreamsActivityProvider;
import com.atlassian.streams.spi.StreamsInterruptedException;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.DESCRIPTION;
import static com.atlassian.jira.issue.IssueFieldConstants.SUMMARY;
import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.common.Iterables.take;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Options.isDefined;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.spi.Filters.inDateRange;
import static com.atlassian.streams.spi.Filters.inOptionActivities;
import static com.atlassian.streams.spi.Filters.inUsers;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.filter;
import static org.apache.commons.lang.StringUtils.isBlank;

public class JiraStreamsActivityProvider implements StreamsActivityProvider
{
    // Amount of time to check to consider if action and comment is related
    private static final long CLOSE_ENOUGH_TIME_LIMIT_MS = 200;
    public static final String PROVIDER_KEY = "issues";
    public static final String ISSUE_VOTE_REL = "http://streams.atlassian.com/syndication/issue-vote";

    private final JiraAuthenticationContext authenticationContext;
    private final ChangeHistoryManager changeHistoryManager;
    private final CommentManager commentManager;
    private final JiraHelper helper;
    private final JiraActivityItemAggregator jiraActivityItemAggregator;
    private final JiraEntryFactory jiraEntryFactory;
    private final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties;

    private final IssueFinder issueFinder;

    public JiraStreamsActivityProvider(final JiraAuthenticationContext authenticationContext,
            final ChangeHistoryManager changeHistoryManager,
            final CommentManager commentManager,
            final JiraHelper helper,
            JiraActivityItemAggregator jiraActivityItemAggregator,
            final JiraEntryFactory jiraSyndEntryFactory,
            final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties,
            final IssueFinder issueFinder)
    {
        this.authenticationContext = authenticationContext;
        this.changeHistoryManager = changeHistoryManager;
        this.commentManager = commentManager;
        this.helper = checkNotNull(helper, "helper");
        this.jiraActivityItemAggregator = checkNotNull(jiraActivityItemAggregator, "jiraActivityItemAggregator");
        this.jiraEntryFactory = jiraSyndEntryFactory;
        this.jiraApplicationProperties = jiraApplicationProperties;
        this.issueFinder = checkNotNull(issueFinder, "issueFinder");
    }

    /**
     * Get the activity feed for the given request
     *
     * @param request The request
     * @return The ATOM feed
     */
    public StreamsFeed getActivityFeed(final ActivityRequest request) throws StreamsException
    {
        final Iterable<Issue> issues = issueFinder.find(request);
        Iterable<JiraActivityItem> activityItems = orderByDate.sortedCopy(extractActivity(issues, request));
        Iterable<AggregatedJiraActivityItem> activities = jiraActivityItemAggregator.aggregate(activityItems);

        final Iterable<StreamsEntry> entries = jiraEntryFactory.getEntries(activities);

        final String title = RenderingUtilities.htmlEncode(
            jiraApplicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE) + " - "
            + authenticationContext.getI18nHelper().getText("portlet.activityfeed.name"));

        return new StreamsFeed(title, take(request.getMaxResults(), entries), none(String.class));
    }

    /**
     * Extracts activity from the given list of issues
     */
    private List<JiraActivityItem> extractActivity(final Iterable<Issue> issues, final ActivityRequest request)
    {
        final User user = authenticationContext.getLoggedInUser();
        Predicate<String> inUsers = inUsers(request);
        Predicate<Date> containsDate = inDateRange(request);
        Predicate<Option<Pair<ActivityObjectType, ActivityVerb>>> hasActivity = isDefined();
        Predicate<Option<Pair<ActivityObjectType, ActivityVerb>>> inJiraActivities =
                and(hasActivity, inOptionActivities(request));

        ImmutableList.Builder<JiraActivityItem> builder = ImmutableList.builder();

        for (final Issue issue : issues)
        {
            // Check for interrupts each time through this loop, since we may be pulling
            // quite a bit of change history for each issue
            StreamsInterruptedException.throwIfInterrupted();
            
            final Iterable<ChangeHistory> histories =
                    orderChangeHistoryByDate.sortedCopy(changeHistoryManager.getChangeHistoriesForUser(issue, user));
            String issueSummary = getInitialSummary(issue, histories);

            // Put the issue in the list, it's created
            String reporter = issue.getReporterId();
            if (reporter != null && inUsers.apply(reporter) &&
                    containsDate.apply(issue.getCreated()) &&
                    inJiraActivities.apply(some(pair(issue(), post()))))
            {
                builder.add(new JiraActivityItem(issue, issueSummary, pair(issue(), post()), getInitialDescription(issue, histories)));
            }

            // Put comments into a new list, so that we can remove them if they are associated with a change
            final List<Comment> comments = new ArrayList<Comment>();
            comments.addAll(commentManager.getCommentsForUser(issue, user));
            // Iterate through history
            for (final ChangeHistory changeHistory : histories)
            {
                Option<Pair<ActivityObjectType, ActivityVerb>> activity = helper.jiraActivity(changeHistory);
                if (inUsers.apply(changeHistory.getUsername()) &&
                        containsDate.apply(changeHistory.getTimePerformed()) &&
                        inJiraActivities.apply(activity))
                {
                    // STRM-1363 - use the issue summary at the time of the change history
                    for (String newSummary : getNewSummaryIfChanged(changeHistory))
                    {
                        issueSummary = newSummary;
                    }

                    // See if there is an associated comment
                    final Iterator<Comment> iter = comments.iterator();
                    Option<Comment> comment = none();
                    while (iter.hasNext())
                    {
                        final Comment c = iter.next();
                        if (areRelated(changeHistory, c))
                        {
                            iter.remove();
                            comment = some(c);
                            break;
                        }
                    }
                    if (comment.isDefined())
                    {
                        builder.add(new JiraActivityItem(issue, issueSummary, activity.get(), comment.get(), changeHistory));
                    }
                    else
                    {
                        builder.add(new JiraActivityItem(issue, issueSummary, activity.get(), changeHistory));
                    }
                }
            }
            // Iterate through the remaining comments
            for (final Comment comment : comments)
            {
                if (inUsers.apply(comment.getAuthor()) &&
                        containsDate.apply(comment.getCreated()) &&
                        inJiraActivities.apply(some(pair(comment(), post()))))
                {
                    builder.add(new JiraActivityItem(issue, issueSummaryAtTimeOfComment(comment, issue, histories),
                            pair(comment(), post()), comment));
                }
            }
        }
        return builder.build();
    }

    private String issueSummaryAtTimeOfComment(Comment comment, Issue issue, Iterable<ChangeHistory> histories)
    {
        String issueSummary = null;
        long commentCreateTime = comment.getCreated().getTime();

        for (final ChangeHistory changeHistory : histories)
        {
            long changeHistoryCreateTime = changeHistory.getTimePerformed().getTime();
            if (changeHistoryCreateTime < commentCreateTime || isCloseEnough(changeHistoryCreateTime, commentCreateTime))
            {
                for (String newSummary : getNewSummaryIfChanged(changeHistory))
                {
                    issueSummary = newSummary;
                }
            }
        }

        if (!isBlank(issueSummary))
        {
            return issueSummary;
        }

        // summary was not changed before or during the the of comment
        return getInitialSummary(issue, histories);
    }

    private String getInitialSummary(Issue issue, Iterable<ChangeHistory> histories)
    {
        for (final ChangeHistory changeHistory : histories)
        {
            for (String oldSummary : getSummaryIfChanged(changeHistory, true))
            {
                return oldSummary;
            }
        }
        return issue.getSummary();
    }

    private Option<String> getNewSummaryIfChanged(ChangeHistory changeHistory)
    {
        return getSummaryIfChanged(changeHistory, false);
    }

    private Option<String> getSummaryIfChanged(ChangeHistory changeHistory, boolean old)
    {
        for (GenericValue changeItem : filter(getChangeItems(changeHistory), summary))
        {
            String summary = changeItem.getString(old ? "oldstring" : "newstring");
            if (!isBlank(summary))
            {
                return some(summary);
            }
        }
        return none();
    }

    private Option<String> getInitialDescription(Issue issue, Iterable<ChangeHistory> histories)
    {
        for (final ChangeHistory changeHistory : histories)
        {
            for (GenericValue changeItem : filter(getChangeItems(changeHistory), description))
            {
                return option(changeItem.getString("oldstring"));
            }
        }
        return option(issue.getDescription());
    }

    private final Predicate<GenericValue> summary = new Predicate<GenericValue>()
    {
        public boolean apply(GenericValue item)
        {
            return SUMMARY.equals(item.getString("field").toLowerCase());
        }
    };

    private final Predicate<GenericValue> description = new Predicate<GenericValue>()
    {
        public boolean apply(GenericValue item)
        {
            return DESCRIPTION.equals(item.getString("field").toLowerCase());
        }
    };

    private static final Ordering<ChangeHistory> orderChangeHistoryByDate = new Ordering<ChangeHistory>()
    {
        public int compare(ChangeHistory item1, ChangeHistory item2)
        {
            return item1.getTimePerformed().compareTo(item2.getTimePerformed());
        }
    };

    private static final Ordering<JiraActivityItem> orderByDate = new Ordering<JiraActivityItem>()
    {
        public int compare(JiraActivityItem item1, JiraActivityItem item2)
        {
            return item2.getDate().compareTo(item1.getDate());
        }
    };

    private static boolean areRelated(final ChangeHistory change, final Comment comment)
    {
        return StringUtils.equals(comment.getAuthor(), change.getUsername()) &&
                isCloseEnough(comment.getCreated().getTime(), change.getTimePerformed().getTime());
    }

    private static boolean isCloseEnough(long time1, long time2)
    {
        return Math.abs(time1 - time2) < CLOSE_ENOUGH_TIME_LIMIT_MS;
    }
}
