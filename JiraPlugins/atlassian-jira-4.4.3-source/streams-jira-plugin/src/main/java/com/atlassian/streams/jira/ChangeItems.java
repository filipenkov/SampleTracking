package com.atlassian.streams.jira;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistory;

import com.atlassian.streams.api.common.Option;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.ATTACHMENT;
import static com.atlassian.jira.issue.IssueFieldConstants.STATUS;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.get;
import static org.apache.commons.lang.StringUtils.isBlank;

public final class ChangeItems
{
    private static final Iterable<String> FIELDS_TO_SKIP = ImmutableSet.of("projectimport", "workflow", "worklogid");

    private ChangeItems()
    {
        //do not instantiate directly
    }

    public static Iterable<GenericValue> getChangeItems(JiraActivityItem item)
    {
        for (ChangeHistory history : item.getChangeHistory())
        {
            return getChangeItems(history);
        }
        return ImmutableList.of();
    }

    public static Iterable<GenericValue> getChangeItems(ChangeHistory history)
    {
        return filter(getUnfilteredChangeItems(history), displayable);
    }

    public static GenericValue getFirstChangeItem(JiraActivityItem item)
    {
        return get(getChangeItems(item), 0);
    }

    public static Option<Long> getWorklogId(JiraActivityItem item)
    {
        for (ChangeHistory history : item.getChangeHistory())
        {
            for (GenericValue changeItem : getUnfilteredChangeItems(history))
            {
                if ("worklogid".equalsIgnoreCase(changeItem.getString("field")))
                {
                    String newValue = changeItem.getString("oldvalue");
                    if (!isBlank(newValue))
                    {
                        return some(Long.parseLong(newValue));
                    }
                }
            }
        }
        return none();
    }

    @SuppressWarnings("unchecked")
    private static Iterable<GenericValue> getUnfilteredChangeItems(ChangeHistory changeHistory)
    {
        return changeHistory.getChangeItems();
    }

    public static int updatedFieldCount(Iterable<GenericValue> changeItems)
    {
        int count = 0;
        boolean attachmentCounted = false;
        for (GenericValue changeItem : changeItems)
        {
            if (isAttachment(changeItem))
            {
                if (!attachmentCounted)
                {
                    count++;
                }
                attachmentCounted = true;
            }
            else
            {
                count++;
            }
        }
        return count;
    }

    public static boolean isAttachment(GenericValue v)
    {
        return isAttachment().apply(v);
    }

    public static Predicate<GenericValue> isAttachment()
    {
        return IsAttachment.INSTANCE;
    }

    private enum IsAttachment implements Predicate<GenericValue>
    {
        INSTANCE;

        public boolean apply(GenericValue v)
        {
            return ATTACHMENT.equalsIgnoreCase(v.getString("field"));
        }
    }

    public static Predicate<GenericValue> isStatusUpdate()
    {
        return and(IsSystemField.INSTANCE, IsStatusUpdate.INSTANCE);
    }

    public static boolean isStatusUpdate(GenericValue gv)
    {
        return isStatusUpdate().apply(gv);
    }

    private enum IsSystemField implements Predicate<GenericValue>
    {
        INSTANCE;

        public boolean apply(GenericValue gv)
        {
            return "jira".equalsIgnoreCase(gv.getString("fieldtype"));
        }
    }

    private enum IsStatusUpdate implements Predicate<GenericValue>
    {
        INSTANCE;

        public boolean apply(GenericValue gv)
        {
            return STATUS.equalsIgnoreCase(gv.getString("field"));
        }
    }

    public static Predicate<GenericValue> isLinkUpdate()
    {
        return and(IsSystemField.INSTANCE, IsLinkUpdate.INSTANCE);
    }

    public static boolean isLinkUpdate(GenericValue gv)
    {
        return isLinkUpdate().apply(gv);
    }

    private enum IsLinkUpdate implements Predicate<GenericValue>
    {
        INSTANCE;

        public boolean apply(GenericValue gv)
        {
            return "link".equalsIgnoreCase(gv.getString("field"));
        }
    }

    public static Predicate<GenericValue> isDeletedComment()
    {
        return and(IsSystemField.INSTANCE, IsDeletedComment.INSTANCE);
    }

    private enum IsDeletedComment implements Predicate<GenericValue>
    {
        INSTANCE;

        public boolean apply(GenericValue gv)
        {
            return gv.getString("field").equalsIgnoreCase(IssueFieldConstants.COMMENT) && isBlank(gv.getString("newvalue"));
        }
    }

    private static final Predicate<GenericValue> displayable = new Predicate<GenericValue>()
    {
        public boolean apply(GenericValue item)
        {
            return !contains(FIELDS_TO_SKIP, item.getString("field").toLowerCase());
        }
    };
}
