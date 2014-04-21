package com.atlassian.streams.common.renderer;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Option;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.google.common.base.Preconditions.checkNotNull;

final class AuthorsRenderer implements Function<Iterable<UserProfile>, Html>
{
    private final Function<Iterable<UserProfile>, Option<Html>> compoundRenderer;
    private final I18nResolver i18nResolver;

    public AuthorsRenderer(final I18nResolver i18nResolver, final TemplateRenderer templateRenderer, boolean authorStyle)
    {
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        compoundRenderer = new CompoundStatementRenderer<UserProfile>(i18nResolver,
                new UserProfileRenderer(checkNotNull(templateRenderer, "templateRenderer"), authorStyle));
    }

    @HtmlSafe
    public Html apply(final Iterable<UserProfile> authors)
    {
        return compoundRenderer.apply(authors).getOrElse(renderUnknownAuthor());
    }

    private Html renderUnknownAuthor()
    {
        return new Html(i18nResolver.getText("streams.authors.unknown"));
    }

    private final class UserProfileRenderer implements Function<UserProfile, Option<Html>>
    {
        private final TemplateRenderer templateRenderer;
        private final boolean authorStyle;

        private UserProfileRenderer(final TemplateRenderer templateRenderer, final boolean authorStyle)
        {
            this.templateRenderer = templateRenderer;
            this.authorStyle = authorStyle;
        }

        public Option<Html> apply(final UserProfile userProfile)
        {
            return some(new Html(render(templateRenderer, "user-profile-link.vm",
                    ImmutableMap.of("userProfile", userProfile, "authorStyle", authorStyle))));
        }
    }
}
