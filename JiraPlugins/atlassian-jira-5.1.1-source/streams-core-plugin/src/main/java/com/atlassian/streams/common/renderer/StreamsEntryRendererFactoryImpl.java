package com.atlassian.streams.common.renderer;

import java.net.URI;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.base.Function;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class StreamsEntryRendererFactoryImpl implements StreamsEntryRendererFactory
{
    private final I18nResolver i18nResolver;
    private final TemplateRenderer templateRenderer;

    public StreamsEntryRendererFactoryImpl(StreamsI18nResolver i18nResolver, TemplateRenderer templateRenderer)
    {
        this.i18nResolver = i18nResolver;
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
    }

    public Renderer newCommentRenderer(String comment)
    {
        return newCommentRenderer(newCommentTitleRenderer(), comment);
    }

    public Renderer newCommentRenderer(Html comment)
    {
        return newCommentRenderer(newCommentTitleRenderer(), comment);
    }

    public Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, String comment)
    {
        return new CommentRenderer(templateRenderer, titleRenderer, comment);
    }

    public Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, Html comment)
    {
        return newCommentRenderer(titleRenderer, comment, none(URI.class));
    }

    public Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, Html comment, Option<URI> styleLink)
    {
        return new CommentRenderer(templateRenderer, titleRenderer, comment, styleLink);
    }

    public Function<StreamsEntry, Html> newTitleRenderer(String key)
    {
        return newTitleRenderer(key, newAuthorsRenderer(), some(newActivityObjectsRenderer()), some(newActivityObjectRendererWithSummary()));
    }

    public Function<StreamsEntry, Html> newTitleRenderer(final String key,
            final Function<Iterable<UserProfile>, Html> authorsRenderer,
            final Option<Function<Iterable<ActivityObject>, Option<Html>>> activityObjectRenderer,
            final Option<Function<ActivityObject, Option<Html>>> targetRenderer)
    {
        return new TitleRenderer(i18nResolver, key, authorsRenderer, activityObjectRenderer, targetRenderer);
    }

    public Function<Iterable<ActivityObject>, Option<Html>> newActivityObjectsRenderer()
    {
        return newActivityObjectsRenderer(newActivityObjectRendererWithSummary());
    }

    public Function<Iterable<ActivityObject>, Option<Html>> newActivityObjectsRenderer(
            Function<ActivityObject, Option<Html>> objectRenderer)
    {
        return new CompoundStatementRenderer<ActivityObject>(i18nResolver, objectRenderer);
    }

    public Function<Iterable<UserProfile>, Html> newAuthorsRenderer()
    {
        return new AuthorsRenderer(i18nResolver, templateRenderer, true);
    }

    public Function<Iterable<UserProfile>, Html> newUserProfileRenderer()
    {
        return new AuthorsRenderer(i18nResolver, templateRenderer, false);
    }

    public Function<ActivityObject, Option<Html>> newActivityObjectRendererWithSummary()
    {
        return new ActivityObjectRenderer(templateRenderer, true);
    }

    public Function<ActivityObject, Option<Html>> newActivityObjectRendererWithoutSummary()
    {
        return new ActivityObjectRenderer(templateRenderer, false);
    }

    public <T> Function<Iterable<T>, Option<Html>> newCompoundStatementRenderer(Function<T, Option<Html>> render)
    {
        return new CompoundStatementRenderer<T>(i18nResolver, render);
    }

    public Function<StreamsEntry, Html> newCommentTitleRenderer()
    {
        return CommentRenderer.standardTitleRenderer(this);
    }
}
