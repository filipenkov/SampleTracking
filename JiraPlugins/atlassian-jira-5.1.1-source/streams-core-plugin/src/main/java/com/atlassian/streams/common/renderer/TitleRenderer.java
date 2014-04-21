package com.atlassian.streams.common.renderer;

import java.io.Serializable;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.common.Option;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.common.Functions.singletonList;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.toArray;

final class TitleRenderer implements Function<StreamsEntry, Html>
{
    private final I18nResolver i18nResolver;
    private final String key;
    private final Function<Iterable<UserProfile>, Html> authorsRenderer;
    private final Option<Function<Iterable<ActivityObject>, Option<Html>>> activityObjectRenderer;
    private final Option<Function<ActivityObject, Option<Html>>> targetRenderer;

    TitleRenderer(I18nResolver i18nResolver,
            String key,
            Function<Iterable<UserProfile>, Html> authorsRenderer,
            Option<Function<Iterable<ActivityObject>, Option<Html>>> activityObjectRenderer,
            Option<Function<ActivityObject, Option<Html>>> targetRenderer)
    {
        this.i18nResolver = i18nResolver;
        this.key = key;
        this.authorsRenderer = authorsRenderer;
        this.activityObjectRenderer = activityObjectRenderer;
        this.targetRenderer = targetRenderer;
    }

    public Html apply(StreamsEntry entry)
    {
        Option<Html> objectHtml = activityObjectRenderer.flatMap(render(entry.getActivityObjects()));
        Option<Html> targetHtml = targetRenderer.flatMap(render(entry.getTarget()));
        return new Html(getText(
                key,
                concat(
                    ImmutableList.of(authorsRenderer.apply(entry.getAuthors())),
                    objectHtml.map(singletonList(Html.class)).getOrElse(ImmutableList.<Html>of()),
                    targetHtml.map(singletonList(Html.class)).getOrElse(ImmutableList.<Html>of()))));
    }

    private String getText(String key, Iterable<Html> args)
    {
        return i18nResolver.getText(key, toArray(args, Serializable.class));
    }

    private Function<Function<Iterable<ActivityObject>, Option<Html>>, Option<Html>> render(final Iterable<ActivityObject> activityObjects)
    {
        return new Function<Function<Iterable<ActivityObject>, Option<Html>>, Option<Html>>()
        {
            public Option<Html> apply(Function<Iterable<ActivityObject>, Option<Html>> renderer)
            {
                return renderer.apply(activityObjects);
            }
        };
    }

    private Function<Function<ActivityObject, Option<Html>>, Option<Html>> render(final Option<ActivityObject> target)
    {
        return new Function<Function<ActivityObject, Option<Html>>, Option<Html>>()
        {
            public Option<Html> apply(Function<ActivityObject, Option<Html>> renderer)
            {
                return target.flatMap(renderer);
            }
        };
    }
}