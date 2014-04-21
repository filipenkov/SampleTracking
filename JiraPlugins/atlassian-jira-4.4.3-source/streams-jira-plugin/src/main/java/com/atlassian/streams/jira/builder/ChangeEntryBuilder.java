package com.atlassian.streams.jira.builder;

import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.AggregatedJiraActivityItem;
import com.atlassian.streams.jira.JiraActivityItem;

import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.jira.ChangeItems.isStatusUpdate;
import static com.atlassian.streams.jira.JiraActivityVerbs.transition;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.find;

public class ChangeEntryBuilder
{
    private final StatusChangeEntryBuilder statusChangeEntryBuilder;
    private final GeneralUpdateEntryBuilder generalUpdateEntryBuilder;

    ChangeEntryBuilder(StatusChangeEntryBuilder statusChangeEntryBuilder,
            GeneralUpdateEntryBuilder generalUpdateEntryBuilder)
    {
        this.statusChangeEntryBuilder = checkNotNull(statusChangeEntryBuilder, "statusChangeEntryBuilder");
        this.generalUpdateEntryBuilder = checkNotNull(generalUpdateEntryBuilder, "generalUpdateEntryBuilder");
    }

    public Option<StreamsEntry> build(AggregatedJiraActivityItem aggregatedItem)
    {
        if (isTransitionVerb(aggregatedItem))
        {
            JiraActivityItem item = aggregatedItem.getActivityItem();
            return statusChangeEntryBuilder.build(item, find(getChangeItems(item), isStatusUpdate()));
        }
        return generalUpdateEntryBuilder.build(aggregatedItem);
    }

    private boolean isTransitionVerb(AggregatedJiraActivityItem aggregatedItem)
    {
        if (!aggregatedItem.getRelatedActivityItems().isDefined())
        {
            ActivityVerb verb = aggregatedItem.getActivity().second();
            if (transition().equals(verb))
            {
                return true;
            }
            for (ActivityVerb childVerb : verb.parent())
            {
                return transition().equals(childVerb);
            }
        }
        return false;
    }
}
