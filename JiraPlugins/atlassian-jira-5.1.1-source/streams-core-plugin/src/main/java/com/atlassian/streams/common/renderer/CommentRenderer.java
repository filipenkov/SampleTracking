package com.atlassian.streams.common.renderer;

import java.net.URI;
import java.util.Map;

import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.common.Function2;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.streams.api.common.Functions.trimToNone;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.spi.renderer.Renderers.getExcerptUsingLimit;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.atlassian.streams.spi.renderer.Renderers.replaceNbsp;
import static com.atlassian.streams.spi.renderer.Renderers.stripBasicMarkup;
import static com.atlassian.streams.spi.renderer.Renderers.truncate;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;

final class CommentRenderer implements Renderer
{
    private final TemplateRenderer templateRenderer;
    private final Function<StreamsEntry, Html> titleRenderer;
    private final Function2<StreamsEntry, Boolean, Option<Html>> commentRenderer;
    private final Option<URI> styleLink;

    public CommentRenderer(final TemplateRenderer templateRenderer,
            final Function<StreamsEntry, Html> titleRenderer,
            final String comment)
    {
        this(templateRenderer, titleRenderer, option(comment), none(Html.class), none(URI.class));
    }

    public CommentRenderer(final TemplateRenderer templateRenderer,
            final Function<StreamsEntry, Html> titleRenderer,
            final Html comment,
            Option<URI> styleLink)
    {
        this(templateRenderer, titleRenderer, none(String.class), some(comment), styleLink);
    }

    public CommentRenderer(final TemplateRenderer templateRenderer,
            final Function<StreamsEntry, Html> titleRenderer,
            final Option<String> wikiComment,
            final Option<Html> htmlComment,
            Option<URI> styleLink)
    {
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.titleRenderer = checkNotNull(titleRenderer, "titleRenderer");
        this.commentRenderer = htmlComment.map(renderHtml).getOrElse(renderWiki(wikiComment));
        this.styleLink = checkNotNull(styleLink, "styleLink");
    }

    public Option<Html> renderContentAsHtml(StreamsEntry entry)
    {
        return commentRenderer.apply(entry, false);
    }

    public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
    {
        return commentRenderer.apply(entry, true);
    }

    private final Function<Html, Function2<StreamsEntry, Boolean, Option<Html>>> renderHtml = new Function<Html, Function2<StreamsEntry, Boolean, Option<Html>>>()
    {
        public Function2<StreamsEntry, Boolean, Option<Html>> apply(final Html h)
        {
            return new Function2<StreamsEntry, Boolean, Option<Html>>()
            {
                public Option<Html> apply(StreamsEntry entry, Boolean truncate)
                {
                    Html comment = truncate ? truncate(SUMMARY_LIMIT, h) : h;
                    if ((truncate && h.equals(comment)) || isBlank(h.toString()))
                    { 
                        return none();
                    }
                    Map<String, Object> context = ImmutableMap.<String, Object>builder().
                            put("commentHtml", comment).
                            put("truncated", truncate).
                            put("commentUri", entry.getAlternateLink()).
                            put("styleLink", styleLink).
                            build();

                    return some(new Html(render(templateRenderer, "comment-block.vm", context)));
                }
            };
        }
    };

    private Function2<StreamsEntry, Boolean, Option<Html>> renderWiki(final Option<String> comment)
    {
        final Option<String> strippedComment = comment.map(stripBasicMarkup()).flatMap(trimToNone());

        return new Function2<StreamsEntry, Boolean, Option<Html>>()
        {
            public Option<Html> apply(StreamsEntry entry, Boolean truncate)
            {
                return strippedComment.flatMap(renderF(entry, truncate));
            }

            private Function<String, Option<Html>> renderF(final StreamsEntry entry, final Boolean truncate)
            {
                return new Function<String, Option<Html>>()
                {
                    public Option<Html> apply(String s)
                    {
                        String comment = truncate ? getExcerptUsingLimit(s, SUMMARY_LIMIT) : s;

                        if (truncate && s.equals(comment))
                        {
                            return none();
                        }

                        Map<String, Object> context = ImmutableMap.<String, Object>builder().
                                put("comment", replaceNbsp(comment)).
                                put("truncated", truncate).
                                put("commentUri", entry.getAlternateLink()).build();

                        return some(new Html(render(templateRenderer, "comment-block.vm", context)));
                    }
                };
            }
        };
    };

    public Html renderTitleAsHtml(StreamsEntry entry)
    {
        return titleRenderer.apply(entry);
    }

    static Function<StreamsEntry, Html> standardTitleRenderer(StreamsEntryRendererFactory rendererFactory)
    {
        return new StandardTitleRenderer(rendererFactory);
    }

    static final class StandardTitleRenderer implements Function<StreamsEntry, Html>
    {
        private final StreamsEntryRendererFactory rendererFactory;

        public StandardTitleRenderer(StreamsEntryRendererFactory rendererFactory)
        {
            this.rendererFactory = rendererFactory;
        }

        public Html apply(StreamsEntry entry)
        {
            String key = entry.getTarget().isDefined() ? "streams.title.commented.on" : "streams.title.commented";
            return rendererFactory.newTitleRenderer(key).apply(entry);
        }
    }
}