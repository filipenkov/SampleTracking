package com.atlassian.streams.jira.renderer;

import com.atlassian.jira.issue.Issue;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.spi.renderer.Renderers;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class IssueActivityObjectRendererFactory
{
    private final TemplateRenderer templateRenderer;
    private final StreamsEntryRendererFactory rendererFactory;

    public IssueActivityObjectRendererFactory(TemplateRenderer templateRenderer,
            StreamsEntryRendererFactory rendererFactory)
    {
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
    }

    public Function<Iterable<ActivityObject>, Option<Html>> newIssueActivityObjectsRenderer(Issue issue)
    {
        return rendererFactory.newActivityObjectsRenderer(newIssueActivityObjectRendererWithSummary(issue));
    }

    public Function<ActivityObject, Option<Html>> newIssueActivityObjectRendererWithSummary(Issue issue)
    {
        return new IssueActivityObjectRenderer(issue, true);
    }

    public Function<ActivityObject, Option<Html>> newIssueActivityObjectRendererWithoutSummary(Issue issue)
    {
        return new IssueActivityObjectRenderer(issue, false);
    }

    private final class IssueActivityObjectRenderer implements Function<ActivityObject, Option<Html>>
    {
        private final Issue issue;
        private final boolean withSummary;

        public IssueActivityObjectRenderer(Issue issue, boolean withSummary)
        {
            this.issue = issue;
            this.withSummary = withSummary;
        }

        public Option<Html> apply(final ActivityObject o)
        {
            return o.getTitle().map(render(o));
        }

        private Function<String, Html> render(final ActivityObject o)
        {
            return new Function<String, Html>()
            {
                public Html apply(final String title)
                {
                    return new Html(Renderers.render(templateRenderer,
                            "activity-object-link-issue.vm",
                            ImmutableMap.<String, Object>of(
                                    "activityObject", o,
                                    "issue", issue,
                                    "withSummary", withSummary)));
                }
            };
        }
    }
}
