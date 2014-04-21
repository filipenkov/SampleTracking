package com.atlassian.streams.jira;

import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

public class AggregatedJiraActivityItem
{
    private final JiraActivityItem item;
    private final Option<Iterable<JiraActivityItem>> relatedActivityItems;
    private final Pair<ActivityObjectType, ActivityVerb> activity;

    public AggregatedJiraActivityItem(JiraActivityItem item)
    {
        this.item = checkNotNull(item, "item");
        this.relatedActivityItems = none();
        this.activity = item.getActivity();
    }

    public AggregatedJiraActivityItem(JiraActivityItem item,
            Iterable<JiraActivityItem> relatedActivityItems,
            Pair<ActivityObjectType, ActivityVerb> activity)
    {
        this.item = checkNotNull(item, "item");
        this.relatedActivityItems = option(relatedActivityItems);
        this.activity = checkNotNull(activity, "activity");
    }

    public Option<Iterable<JiraActivityItem>> getRelatedActivityItems()
    {
        return relatedActivityItems;
    }

    public Pair<ActivityObjectType, ActivityVerb> getActivity()
    {
        return activity;
    }

    public JiraActivityItem getActivityItem()
    {
        return item;
    }
}
