package com.atlassian.streams.jira.renderer;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.Html;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.streams.api.StreamsEntry.Html.trimHtmlToNone;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.atlassian.streams.spi.renderer.Renderers.truncate;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreatedIssueRendererFactory
{
    private final StreamsEntryRendererFactory rendererFactory;
    private final IssueActivityObjectRendererFactory issueActivityObjectRendererFactory;
    private final TemplateRenderer templateRenderer;
    private final UriProvider uriProvider;
    private final JiraHelper helper;

    public CreatedIssueRendererFactory(StreamsEntryRendererFactory rendererFactory,
            IssueActivityObjectRendererFactory issueActivityObjectRendererFactory,
            TemplateRenderer templateRenderer,
            UriProvider uriProvider,
            JiraHelper helper)
    {
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.issueActivityObjectRendererFactory = checkNotNull(issueActivityObjectRendererFactory, "issueActivityObjectRendererFactory");
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
        this.helper = checkNotNull(helper, "helper");
    }

    public Renderer newInstance(Issue issue, Option<String> initialDescription)
    {
        return new CreatedRenderer(issue, initialDescription);
    }

    private final class CreatedRenderer implements Renderer
    {
        private final Function<StreamsEntry, Html> titleRenderer;
        private final Function<Boolean, Option<Html>> renderDescription;

        public CreatedRenderer(Issue issue, Option<String> initialDescription)
        {
            titleRenderer = rendererFactory.newTitleRenderer("streams.item.jira.issue.post",
                rendererFactory.newAuthorsRenderer(),
                some(issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(issue)),
                some(rendererFactory.newActivityObjectRendererWithSummary()));
            this.renderDescription = renderDescription(issue, initialDescription);
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return renderDescription.apply(false);
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return renderDescription.apply(true);
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return titleRenderer.apply(entry);
        }

        private Function<Boolean, Option<Html>> renderDescription(final Issue issue, Option<String> initialDescription)
        {
            final Option<Html> description = option(helper.renderIssueFieldValue(issue, "description", initialDescription.getOrElse(""))).flatMap(trimHtmlToNone());
            return new Function<Boolean, Option<Html>>()
            {
                public Option<Html> apply(Boolean truncate)
                {
                    return description.flatMap(renderContent(truncate));
                }

                private Function<Html, Option<Html>> renderContent(final boolean truncate)
                {
                    return new Function<Html, Option<Html>>()
                    {
                        public Option<Html> apply(Html d)
                        {
                            Html description = truncate ? truncate(SUMMARY_LIMIT, d) : d;
                            if (truncate && d.equals(description))
                            {
                                return none(); // we don't want a summary if it will be the same as the content
                            }
                            Map<String, Object> context = ImmutableMap.<String, Object>builder().
                                put("contentHtml", description).
                                put("truncated", truncate).
                                put("contentUri", uriProvider.getIssueUri(issue.getKey())).
                                build();

                            return some(new Html(render(templateRenderer, "jira-content-block.vm", context)));
                        }
                    };
                }
            };
        }
    }
}
