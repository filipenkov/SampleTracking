package com.atlassian.streams.api.renderer;

import java.net.URI;

import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Option;

import com.google.common.base.Function;

/**
 * Factory to create {@link StreamsEntry.Renderer}s for some common types of activity entries.
 */
public interface StreamsEntryRendererFactory
{
    /**
     * Creates a {@code StreamsEntry.Renderer} for a comment entry.  Uses {@code comment} as the content of the entry.
     *
     * @param comment comment to use as the content of the entry
     * @return renderer for the comment
     */
    Renderer newCommentRenderer(String comment);
    Renderer newCommentRenderer(Html comment);

    Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, String message);
    Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, Html message);
    Renderer newCommentRenderer(Function<StreamsEntry, Html> titleRenderer, Html apply, Option<URI> styleLink);

    Function<StreamsEntry, Html> newCommentTitleRenderer();

    Function<StreamsEntry, Html> newTitleRenderer(String key);

    Function<StreamsEntry, Html> newTitleRenderer(String key,
            Function<Iterable<UserProfile>, Html> authorsRenderer,
            Option<Function<Iterable<ActivityObject>, Option<Html>>> activityObjectRenderer,
            Option<Function<ActivityObject, Option<Html>>> targetRenderer);

    Function<Iterable<UserProfile>, Html> newAuthorsRenderer();

    Function<Iterable<UserProfile>, Html> newUserProfileRenderer();

    Function<Iterable<ActivityObject>, Option<Html>> newActivityObjectsRenderer();

    Function<Iterable<ActivityObject>, Option<Html>> newActivityObjectsRenderer(Function<ActivityObject, Option<Html>> objectRenderer);

    Function<ActivityObject, Option<Html>> newActivityObjectRendererWithSummary();

    Function<ActivityObject, Option<Html>> newActivityObjectRendererWithoutSummary();

    <T> Function<Iterable<T>, Option<Html>> newCompoundStatementRenderer(Function<T, Option<Html>> render);
}
