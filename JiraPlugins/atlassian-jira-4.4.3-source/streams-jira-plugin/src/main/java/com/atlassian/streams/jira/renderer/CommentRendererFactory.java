package com.atlassian.streams.jira.renderer;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;

import com.google.common.base.Function;

import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class CommentRendererFactory
{
    private final StreamsEntryRendererFactory rendererFactory;
    private final IssueActivityObjectRendererFactory issueActivityObjectRendererFactory;
    private final JiraHelper helper;

    public CommentRendererFactory(StreamsEntryRendererFactory rendererFactory,
            IssueActivityObjectRendererFactory issueActivityObjectRendererFactory,
            JiraHelper helper)
    {
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.issueActivityObjectRendererFactory = checkNotNull(issueActivityObjectRendererFactory, "issueActivityObjectRendererFactory");
        this.helper = checkNotNull(helper, "helper");
    }

    public Renderer newInstance(JiraActivityItem item, Comment comment)
    {
        return rendererFactory.newCommentRenderer(
                new IssueCommentTitleRenderer(item),
                helper.renderComment().apply(comment));
    }

    private final class IssueCommentTitleRenderer implements Function<StreamsEntry, Html>
    {
        private final JiraActivityItem item;

        public IssueCommentTitleRenderer(JiraActivityItem item)
        {
            this.item = item;
        }

        public Html apply(StreamsEntry entry)
        {
            String key = entry.getTarget().isDefined() ? "streams.title.commented.on" : "streams.title.commented";
            return newIssueTitleRenderer(item, key).apply(entry);
        }

        private Function<StreamsEntry, Html> newIssueTitleRenderer(JiraActivityItem item, String key)
        {
            return rendererFactory.newTitleRenderer(key, rendererFactory.newAuthorsRenderer(),
                    some(issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(item.getIssue())),
                    some(issueActivityObjectRendererFactory.newIssueActivityObjectRendererWithSummary(item.getIssue())));
        }
    }

}
