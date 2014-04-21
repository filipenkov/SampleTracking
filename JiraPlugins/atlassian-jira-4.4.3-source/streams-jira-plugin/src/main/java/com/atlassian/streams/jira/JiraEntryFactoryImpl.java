package com.atlassian.streams.jira;

import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.builder.ChangeEntryBuilder;
import com.atlassian.streams.jira.builder.CommentEntryBuilder;
import com.atlassian.streams.jira.builder.CreatedEntryBuilder;

import com.google.common.base.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Options.getValues;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public class JiraEntryFactoryImpl implements JiraEntryFactory
{
    private static final Logger log = LoggerFactory.getLogger(JiraEntryFactoryImpl.class);

    private final CreatedEntryBuilder createdEntryBuilder;
    private final ChangeEntryBuilder changeEntryBuilder;
    private final CommentEntryBuilder commentEntryBuilder;

    public JiraEntryFactoryImpl(CreatedEntryBuilder createdEntryBuilder,
            ChangeEntryBuilder changeEntryBuilder,
            CommentEntryBuilder commentEntryBuilder)
    {
        this.createdEntryBuilder = checkNotNull(createdEntryBuilder, "createdEntryBuilder");
        this.changeEntryBuilder = checkNotNull(changeEntryBuilder, "changeEntryBuilder");
        this.commentEntryBuilder = checkNotNull(commentEntryBuilder, "commentEntryBuilder");
    }

    public Iterable<StreamsEntry> getEntries(final Iterable<AggregatedJiraActivityItem> items)
    {
        // we have to filter out null entries returned by toStreamsEntries, which happens when there are
        // Project Import entries.
        return getValues(transform(items, toStreamsEntries()));
    }
    
    private Function<AggregatedJiraActivityItem, Option<StreamsEntry>> toStreamsEntries()
    {
        return new Function<AggregatedJiraActivityItem, Option<StreamsEntry>>()
        {
            public Option<StreamsEntry> apply(AggregatedJiraActivityItem item)
            {
                try
                {
                    // If the change and comment are null, it's an issue creation event
                    if (isIssueCreation(item))
                    {
                        return createdEntryBuilder.build(item.getActivityItem());
                    }
                    else if (isPostComment(item))
                    {
                        return commentEntryBuilder.build(item.getActivityItem());
                    }
                    else
                    {
                        return changeEntryBuilder.build(item);
                    }
                }
                catch (Exception e)
                {
                    log.warn("Error creating streams entry", e);
                    return none(StreamsEntry.class);
                }
            }

            private boolean isPostComment(AggregatedJiraActivityItem item)
            {
                return pair(comment(), post()).equals(item.getActivity());
            }

            private boolean isIssueCreation(AggregatedJiraActivityItem item)
            {
                return pair(issue(), post()).equals(item.getActivity());
            }
        };
    }
}
