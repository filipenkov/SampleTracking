package com.atlassian.streams.jira;

import java.util.Date;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Do not use in TreeSet or TreeMap, the compareTo() method does not obey the equals() contract, it is only
 * based on the dates.
 */
public class JiraActivityItem
{
    private final Issue issue;
    private final String displaySummary;
    private final Option<String> initialDescription;
    private final Option<ChangeHistory> changeHistory;
    private final Option<Comment> comment;
    private final Date date;
    private final Pair<ActivityObjectType, ActivityVerb> activity;


    /**
     * Used for issue creation items
     *
     * @param issue The issue
     */
    public JiraActivityItem(final Issue issue, final String displaySummary,
            final Pair<ActivityObjectType, ActivityVerb> activity, final Option<String> initialDescription)
    {
        this.issue = checkNotNull(issue, "issue");
        this.displaySummary = checkNotNull(displaySummary, "initialSummary");
        this.activity = checkNotNull(activity, "activity");
        this.initialDescription = checkNotNull(initialDescription, "initialDescription");
        comment = none();
        changeHistory = none();
        date = issue.getCreated(); //n.b. Date only accurate to the nearest second
    }

    /**
     * Used for comment items
     *
     * @param issue The issue
     * @param comment The comment
     */
    public JiraActivityItem(final Issue issue, final String displaySummary, final Pair<ActivityObjectType, ActivityVerb> activity,
            final Comment comment)
    {
        this.issue = checkNotNull(issue, "issue");
        this.displaySummary = checkNotNull(displaySummary, "initialSummary");
        this.activity = checkNotNull(activity, "activity");
        this.comment = some(checkNotNull(comment, "comment"));
        changeHistory = none();
        date = comment.getCreated();
        initialDescription = none();
    }

    /**
     * Used for issue changed items
     *
     * @param issue The issue
     * @param comment A comment
     * @param changeHistory The change history
     */
    public JiraActivityItem(final Issue issue, final String displaySummary, final Pair<ActivityObjectType, ActivityVerb> activity,
            final Comment comment, final ChangeHistory changeHistory)
    {
        this.issue = checkNotNull(issue, "issue");
        this.displaySummary = checkNotNull(displaySummary, "initialSummary");
        this.activity = checkNotNull(activity, "activity");
        this.changeHistory = some(checkNotNull(changeHistory, "changeHistory"));
        this.comment = some(checkNotNull(comment, "comment"));
        date = changeHistory.getTimePerformed();
        initialDescription = none();
    }

    public JiraActivityItem(final Issue issue, final String displaySummary, final Pair<ActivityObjectType, ActivityVerb> activity,
            final ChangeHistory changeHistory)
    {
        this.issue = checkNotNull(issue, "issue");
        this.displaySummary = checkNotNull(displaySummary, "initialSummary");
        this.activity = checkNotNull(activity, "activity");
        this.changeHistory = some(checkNotNull(changeHistory, "changeHistory"));
        comment = none();
        date = changeHistory.getTimePerformed();
        initialDescription = none();
    }

    public Issue getIssue()
    {
        return issue;
    }

    public String getDisplaySummary()
    {
        return displaySummary;
    }

    public Option<String> getInitialDescription()
    {
        return initialDescription;
    }

    public Option<ChangeHistory> getChangeHistory()
    {
        return changeHistory;
    }

    public Iterable<String> getChangeHistoryAuthors()
    {
        return transform(filter(getChangeHistory(), HAS_AUTHOR), USER_NAME);
    }

    public Option<Comment> getComment()
    {
        return comment;
    }

    public Date getDate()
    {
        return date;
    }

    public Pair<ActivityObjectType, ActivityVerb> getActivity()
    {
        return activity;
    }

    @Override
    public String toString()
    {
        return issue.getKey();
    }

    static final Predicate<ChangeHistory> HAS_AUTHOR = new Predicate<ChangeHistory>()
    {
        public boolean apply(final ChangeHistory input)
        {
            return input.getUsername() != null;
        }
    };

    static final Function<ChangeHistory, String> USER_NAME = new Function<ChangeHistory, String>()
    {
        public String apply(final ChangeHistory from)
        {
            return from.getUsername();
        }
    };
}
