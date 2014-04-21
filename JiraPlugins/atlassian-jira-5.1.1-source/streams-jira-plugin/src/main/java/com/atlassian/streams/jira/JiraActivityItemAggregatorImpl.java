package com.atlassian.streams.jira;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.joda.time.Interval;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.jira.ChangeItems.getFirstChangeItem;
import static com.atlassian.streams.jira.ChangeItems.isLinkUpdate;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityVerbs.link;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.joda.time.Duration.standardSeconds;

/**
 * Combines related activity item entries under certain conditions
 */
public class JiraActivityItemAggregatorImpl implements JiraActivityItemAggregator
{
    /**
     * Aggregate related activity item entries under certain conditions into a single entry
     * @param activityItems the activity item list to aggregate
     * @return the modified activity item list with combined entries
     */
    public Iterable<AggregatedJiraActivityItem> aggregate(Iterable<JiraActivityItem> activityItems)
    {
        List<JiraActivityItem> activities = ImmutableList.copyOf(activityItems);
        ImmutableList.Builder<AggregatedJiraActivityItem> builder = new ImmutableList.Builder<AggregatedJiraActivityItem>();

        for (int i = 0; i < activities.size(); i++)
        {
            JiraActivityItem item = activities.get(i);
            if (linkUpdate(item))
            {
                if (!previouslyLinked(activities, i))
                {
                    builder.add(getAggregatedIssueLinks(activities, i));
                }
            }
            else
            {
                builder.add(new AggregatedJiraActivityItem(item));
            }
        }

        return builder.build();
    }

    private AggregatedJiraActivityItem getAggregatedIssueLinks(List<JiraActivityItem> activities, int currentIndex)
    {
        JiraActivityItem firstActivity = activities.get(currentIndex);
        ImmutableList.Builder<JiraActivityItem> builder = ImmutableList.builder();

        for (int i = currentIndex+1; i < activities.size(); i++)
        {
            // We only check the activities within a second
            if (dateDiffIsLongerThanASecond(firstActivity, activities.get(i)))
            {
                break;
            }
            if (isLinked(firstActivity, activities.get(i)))
            {
                builder.add(activities.get(i));
            }
        }

        Iterable<JiraActivityItem> relatedItems = builder.build();
        if (isEmpty(relatedItems))
        {
            return new AggregatedJiraActivityItem(firstActivity);
        }
        return new AggregatedJiraActivityItem(firstActivity, builder.build(), pair(issue(), link()));
    }

    private boolean previouslyLinked(List<JiraActivityItem> activities, int currentIndex)
    {
        JiraActivityItem secondActivity = activities.get(currentIndex);

        for (int i = currentIndex-1; i >= 0; i--)
        {
            // We only check the activities within a second
            if (dateDiffIsLongerThanASecond(activities.get(i), secondActivity))
            {
                return false;
            }
            if (isLinked(secondActivity, activities.get(i)))
            {
                return true;
            }
        }
        return false;
    }

    private boolean linkUpdate(JiraActivityItem item)
    {
        if (pair(issue(), update()).equals(item.getActivity()))
        {
            Iterable<GenericValue> changeItems = getChangeItems(item);
            if (size(changeItems) == 1)
            {
                return isLinkUpdate(getOnlyElement(changeItems));
            }
        }
        return false;
    }

    private boolean isLinked(JiraActivityItem first, JiraActivityItem second)
    {
        if (!linkUpdate(second))
        {
            return false;
        }

        String issueKey = first.getIssue().getKey();
        GenericValue changeItem1 = getFirstChangeItem(first);
        GenericValue changeItem2 = getFirstChangeItem(second);

        if (isBlank(changeItem1.getString("newvalue")) && !isBlank(changeItem1.getString("oldvalue")))
        {
            return issueKey.equals(changeItem2.getString("oldvalue"));
        }
        return issueKey.equals(changeItem2.getString("newvalue"));
    }

    private boolean dateDiffIsLongerThanASecond(JiraActivityItem first, JiraActivityItem second)
    {
        return dateDiffIsLongerThanASecond(first.getDate().getTime(), second.getDate().getTime());
    }

    private boolean dateDiffIsLongerThanASecond(long first, long second)
    {
        return new Interval(min(first, second), max(first, second)).toDuration().isLongerThan(standardSeconds(1));
    }
}
