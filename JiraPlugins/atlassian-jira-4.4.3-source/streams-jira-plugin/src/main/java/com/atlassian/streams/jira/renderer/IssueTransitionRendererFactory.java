package com.atlassian.streams.jira.renderer;

import java.util.Map;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.RESOLUTION;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.atlassian.streams.spi.renderer.Renderers.truncate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.memoize;

public class IssueTransitionRendererFactory
{
    private final StreamsEntryRendererFactory rendererFactory;
    private final I18nResolver i18nResolver;
    private final IssueActivityObjectRendererFactory issueActivityObjectRendererFactory;
    private final TemplateRenderer templateRenderer;
    private final UriProvider uriProvider;
    private final Function<Comment, Html> commentRenderer;
    private final JiraHelper helper;

    public IssueTransitionRendererFactory(StreamsEntryRendererFactory rendererFactory,
            I18nResolver i18nResolver,
            IssueActivityObjectRendererFactory issueActivityObjectRendererFactory,
            TemplateRenderer templateRenderer,
            UriProvider uriProvider,
            JiraHelper helper)
    {
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.issueActivityObjectRendererFactory = checkNotNull(issueActivityObjectRendererFactory, "issueActivityObjectRendererFactory");
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
        this.helper = checkNotNull(helper, "helper");
        this.commentRenderer = helper.renderComment();
    }

    public Renderer newCustomTransitionRenderer(JiraActivityItem item, String statusName)
    {
        return new CustomTransitionRenderer(item, statusName);
    }

    public Renderer newResolvedRenderer(JiraActivityItem item)
    {
        return new ResolvedRenderer(item);
    }

    public Renderer newSystemTransitionRenderer(JiraActivityItem item, ActivityVerb verb)
    {
        return new SystemTransitionRenderer(item, verb);
    }

    private abstract class TransitionRenderer implements Renderer
    {
        private final JiraActivityItem item;
        Supplier<Option<Pair<Comment, Html>>> commentHtml = memoize(new Supplier<Option<Pair<Comment, Html>>>()
        {
            public Option<Pair<Comment, Html>> get()
            {
                if (!item.getComment().isDefined())
                {
                    return none();
                }
                return some(pair(item.getComment().get(), item.getComment().map(commentRenderer).get()));
            }
        });

        public TransitionRenderer(JiraActivityItem item)
        {
            this.item = item;
        }

        public JiraActivityItem item()
        {
            return item;
        }

        public final Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return commentHtml.get().flatMap(renderComment(false));
        }

        public final Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return commentHtml.get().flatMap(renderComment(true));
        }

        private Function<Pair<Comment, Html>, Option<Html>> renderComment(final boolean truncate)
        {
            return new Function<Pair<Comment, Html>, Option<Html>>()
            {
                public Option<Html> apply(Pair<Comment, Html> comment)
                {
                    Html commentHtml = truncate ? truncate(SUMMARY_LIMIT, comment.second()) : comment.second();
                    if (truncate && comment.second().equals(commentHtml))
                    {
                        return none(); // we don't want a summary if it will be the same as the content
                    }
                    Map<String, Object> context = ImmutableMap.<String, Object>builder().
                        put("contentHtml", commentHtml).
                        put("truncated", truncate).
                        put("contentUri", uriProvider.getIssueCommentUri(comment.first())).
                        build();

                    return some(new Html(render(templateRenderer, "jira-comment-block.vm", context)));
                }
            };
        }
    }

    private final class ResolvedRenderer extends TransitionRenderer
    {
        private final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        private final Function<Iterable<ActivityObject>, Option<Html>> activityObjectsRenderer;

        public ResolvedRenderer(JiraActivityItem item)
        {
            super(item);
            activityObjectsRenderer = issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(item.getIssue());
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            for (String resolution : getResolution(item()))
            {
                return new Html(i18nResolver.getText("streams.item.jira.status.resolved.with.resolution",
                        authorsRenderer.apply(entry.getAuthors()),
                        activityObjectsRenderer.apply(entry.getActivityObjects()).get(),
                        resolution));
            }
            return new Html(i18nResolver.getText("streams.item.jira.status.resolved.without.resolution",
                    authorsRenderer.apply(entry.getAuthors()),
                    activityObjectsRenderer.apply(entry.getActivityObjects()).get()));
        }
    }

    private final class SystemTransitionRenderer extends TransitionRenderer
    {
        private final Function<StreamsEntry, Html> titleRenderer;

        SystemTransitionRenderer(JiraActivityItem item, ActivityVerb verb)
        {
            super(item);
            titleRenderer = rendererFactory.newTitleRenderer("streams.item.jira.status." + verb.key(),
                rendererFactory.newAuthorsRenderer(),
                some(issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(item.getIssue())),
                some(rendererFactory.newActivityObjectRendererWithSummary()));
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return titleRenderer.apply(entry);
        }
    }

    private final class CustomTransitionRenderer extends TransitionRenderer
    {
        private final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        private final Function<Iterable<ActivityObject>, Option<Html>> activityObjectsRenderer;
        private final String statusName;

        public CustomTransitionRenderer(JiraActivityItem item, String statusName)
        {
            super(item);
            this.statusName = statusName;
            this.activityObjectsRenderer = issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(item.getIssue());
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return getResolution(item()).map(transitionWithResolution(entry)).getOrElse(transitionWithoutResolution(entry));
        }

        private Function<String, Html> transitionWithResolution(final StreamsEntry entry)
        {
            return new Function<String, Html>()
            {
                public Html apply(String resolution)
                {
                    return new Html(i18nResolver.getText("streams.item.jira.status.transition.with.resolution",
                            authorsRenderer.apply(entry.getAuthors()),
                            statusName,
                            activityObjectsRenderer.apply(entry.getActivityObjects()).get(),
                            resolution));
                }
            };
        }

        private Html transitionWithoutResolution(final StreamsEntry entry)
        {
            return new Html(i18nResolver.getText("streams.item.jira.status.transition.without.resolution",
                    authorsRenderer.apply(entry.getAuthors()),
                    statusName,
                    activityObjectsRenderer.apply(entry.getActivityObjects()).get()));
        }
    }

    private Option<String> getResolution(JiraActivityItem item)
    {
        for (GenericValue changeItem : getChangeItems(item))
        {
            if (RESOLUTION.equalsIgnoreCase(changeItem.getString("field")))
            {
                return helper.getNewChangeItemNameTranslation(changeItem);
            }
        }
        return none();
    }
}
