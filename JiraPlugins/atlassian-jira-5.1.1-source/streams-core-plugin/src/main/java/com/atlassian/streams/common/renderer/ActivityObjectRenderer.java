package com.atlassian.streams.common.renderer;

import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.common.Function2;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.spi.renderer.Renderers;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.streams.api.common.Fold.foldl;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

final class ActivityObjectRenderer implements Function<ActivityObject, Option<Html>>
{
    private final TemplateRenderer templateRenderer;
    private final boolean withSummary;

    public ActivityObjectRenderer(final TemplateRenderer templateRenderer, boolean withSummary)
    {
        this.templateRenderer = templateRenderer;
        this.withSummary = withSummary;
    }

    @Override
    public Option<Html> apply(final ActivityObject o)
    {
        return titleAsHtml(o).map(renderHtml(o));
    }

    private Function<Html, Html> renderHtml(final ActivityObject o)
    {
        return new Function<Html, Html>()
        {
            @Override
            public Html apply(final Html title)
            {
                return new Html(Renderers.render(templateRenderer, "activity-object-link.vm", ImmutableMap.of(
                        "activityObject", o,
                        "title", title,
                        "summary", summaryAsHtml(o),
                        "withSummary", withSummary)));
            }
        };
    }

    public static Option<Html> titleAsHtml(ActivityObject o)
    {
        return foldl(o.getTitle(), o.getTitleAsHtml(), new Function2<String, Option<Html>, Option<Html>>()
        {
            @Override
            public Option<Html> apply(String title, Option<Html> titleAsHtml)
            {
                return some(new Html(escapeHtml(title)));
            }
        });
    }

    public static Option<Html> summaryAsHtml(ActivityObject o)
    {
        for (String summary : o.getSummary())
        {
            return some(new Html(escapeHtml(summary)));
        }
        return none();
    }
}