package com.atlassian.streams.jira.builder;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.renderer.CommentRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;
import com.atlassian.streams.spi.StreamsUriBuilder;

import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class CommentEntryBuilder
{
    private static final String COMMENT_CATEGORY = "comment";
    private final JiraHelper helper;
    private final CommentRendererFactory rendererFactory;
    private final StreamsI18nResolver i18nResolver;

    CommentEntryBuilder(JiraHelper helper, CommentRendererFactory rendererFactory, StreamsI18nResolver i18nResolver)
    {
        this.helper = checkNotNull(helper, "helper");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
    }

    public Option<StreamsEntry> build(JiraActivityItem item)
    {
        for (Comment comment : item.getComment())
        {
            return some(new StreamsEntry(helper.newCommentBuilder(item).
                authors(ImmutableNonEmptyList.of(helper.getUserProfile().apply(comment.getAuthor()))).
                categories(ImmutableList.of(COMMENT_CATEGORY)).
                inReplyTo(some(new StreamsUriBuilder().setUrl(helper.getIssueUri(item).toASCIIString()).getUri())).
                verb(post()).
                addActivityObject(helper.buildActivityObject(comment)).
                target(some(helper.buildActivityObject(comment.getIssue(), item.getDisplaySummary()))).
                renderer(rendererFactory.newInstance(item, comment)), i18nResolver));
        }
        return none();
    }
}
