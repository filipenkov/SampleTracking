package com.atlassian.streams.jira.builder;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.streams.spi.StreamsUriBuilder;

import com.google.common.base.Function;

import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityObjectTypes.file;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public class ActivityObjectBuilder
{
    private final UriProvider issueUriBuilder;

    ActivityObjectBuilder(UriProvider issueUriBuilder)
    {
        this.issueUriBuilder = checkNotNull(issueUriBuilder, "issueUriBuilder");
    }

    public ActivityObject build(Issue issue, String issueSummary)
    {
        return new ActivityObject(ActivityObject.params()
            .id(new StreamsUriBuilder().setUrl(issueUriBuilder.getIssueUri(issue).toASCIIString()).getUri().toASCIIString())
            .activityObjectType(issue())
            .title(option(issue.getKey()))
            .summary(some(issueSummary))
            .alternateLinkUri(issueUriBuilder.getIssueUri(issue)));
    }

    public ActivityObject build(Comment comment)
    {
        final StreamsUriBuilder idBuilder = new StreamsUriBuilder().setUrl(issueUriBuilder.getIssueUri(comment.getIssue()).toASCIIString()).setTimestamp(comment.getUpdated());

        return new ActivityObject(ActivityObject.params()
            .id(idBuilder.getUri().toASCIIString())
            .title(none(String.class))
            .activityObjectType(comment())
            .alternateLinkUri(issueUriBuilder.getIssueCommentUri(comment)));
    }

    public Iterable<ActivityObject> build(Iterable<Attachment> attachments)
    {
        return transform(attachments, toAttachmentActivityObject());
    }

    private Function<Attachment, ActivityObject> toAttachmentActivityObject()
    {
        return toAttachmentActivityObject;
    }

    private final Function<Attachment, ActivityObject> toAttachmentActivityObject = new Function<Attachment, ActivityObject>()
    {
        public ActivityObject apply(Attachment attachment)
        {
            final StreamsUriBuilder idBuilder = new StreamsUriBuilder().setUrl(issueUriBuilder.getIssueUri(attachment.getIssueObject()).toASCIIString()).setTimestamp(attachment.getCreated());
            return new ActivityObject(ActivityObject.params().
                id(idBuilder.getUri().toASCIIString()).
                activityObjectType(file()).
                title(some(attachment.getFilename())).
                alternateLinkUri(issueUriBuilder.getAttachmentUri(attachment)));
        }
    };
}
