package com.atlassian.streams.jira.builder;

import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.AggregatedJiraActivityItem;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.renderer.AttachmentRendererFactory;
import com.atlassian.streams.jira.renderer.IssueUpdateRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.streams.api.ActivityObjectTypes.file;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityVerbs.link;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;

class GeneralUpdateEntryBuilder
{
    private final JiraHelper helper;
    private final AttachmentRendererFactory attachmentRendererFactory;
    private final IssueUpdateRendererFactory issueUpdateRendererFactory;
    private final StreamsI18nResolver i18nResolver;

    GeneralUpdateEntryBuilder(JiraHelper helper,
              AttachmentRendererFactory attachmentRendererFactory,
              IssueUpdateRendererFactory issueUpdateRendererFactory,
              StreamsI18nResolver i18nResolver)
    {
        this.helper = checkNotNull(helper, "helper");
        this.attachmentRendererFactory = checkNotNull(attachmentRendererFactory, "attachmentRendererFactory");
        this.issueUpdateRendererFactory = checkNotNull(issueUpdateRendererFactory, "issueUpdateRendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
    }

    Option<StreamsEntry> build(AggregatedJiraActivityItem aggregatedItem)
    {
        if (aggregatedItem.getRelatedActivityItems().isDefined())
        {
            return buildMultipleActivityItem(aggregatedItem);
        }
        return buildSingleActivityItem(aggregatedItem.getActivityItem());
    }

    private Option<StreamsEntry> buildSingleActivityItem(JiraActivityItem item)
    {
        Iterable<GenericValue> changeItems = filter(getChangeItems(item), helper.validAttachment());
        if (isEmpty(changeItems))
        {
            return none();
        }

        if (pair(file(), post()).equals(item.getActivity()))
        {
                return buildAttachments(item, changeItems);
        }
        else
        {
            return some(new StreamsEntry(helper.newBuilder(item).
                authors(helper.getUserProfiles(item.getChangeHistoryAuthors())).
                addActivityObject(helper.buildActivityObject(item.getIssue(), item.getDisplaySummary())).
                verb(update()).
                renderer(issueUpdateRendererFactory.newRenderer(item, changeItems)), i18nResolver));
        }
    }

    private Option<StreamsEntry> buildMultipleActivityItem(AggregatedJiraActivityItem aggregatedItem)
    {
        if (pair(issue(), link()).equals(aggregatedItem.getActivity()))
        {
            JiraActivityItem mainItem = aggregatedItem.getActivityItem();
            return some(new StreamsEntry(helper.newLinkedIssueBuilder(mainItem).
                authors(helper.getUserProfiles(mainItem.getChangeHistoryAuthors())).
                addActivityObject(helper.buildActivityObject(mainItem.getIssue(), mainItem.getDisplaySummary())).
                verb(update()).
                renderer(issueUpdateRendererFactory.newIssueLinkEntryRenderer(aggregatedItem)), i18nResolver));
        }
        return none();
    }

    private Option<StreamsEntry> buildAttachments(JiraActivityItem item, Iterable<GenericValue> changeItems)
    {
        Iterable<Attachment> attachments = helper.extractAttachments(changeItems);

        if (!isEmpty(attachments))
        {
            return some(new StreamsEntry(helper.newBuilder(item).
                    authors(helper.getUserProfiles(item.getChangeHistoryAuthors())).
                    verb(post()).
                    addActivityObjects(helper.buildActivityObjects(attachments)).
                    target(some(helper.buildActivityObject(item.getIssue(), item.getDisplaySummary()))).
                    renderer(attachmentRendererFactory.newAttachmentsEntryRenderer(item, attachments)), i18nResolver));
        }
        else
        {
            // If attachment has been deleted, we shouldn't display any streams entry (STRM-913)
            return none();
        }
    }
}
