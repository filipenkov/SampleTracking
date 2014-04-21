package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.sal.api.message.I18nResolver;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link com.atlassian.administration.quicksearch.spi.UserContext}.
 *
 * @since 1.0
 */
public class DefaultUserContext implements UserContext
{
    public static UserContext unauthenticated(HttpServletRequest request)
    {
        return new DefaultUserContext(null, null, null, request);
    }


    private final String username;
    private final Locale locale;
    private final I18nResolver resolver;
    private final HttpServletRequest request;
    private final Map<String,Object> context;

    public DefaultUserContext(String username, Locale locale, I18nResolver resolver,
                              HttpServletRequest request, Map<String,Object> context)
    {
        this.username = username;
        this.locale = checkNotNull(locale, "locale");
        this.resolver = checkNotNull(resolver, "resolver");
        this.request = checkNotNull(request, "request");
        this.context = checkNotNull(context, "context");
    }

    public DefaultUserContext(String username, Locale locale, I18nResolver resolver, HttpServletRequest request)
    {
        this(username, locale, resolver, request, Collections.<String, Object>emptyMap());
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAuthenticated()
    {
        return username != null;
    }

    @Nonnull
    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Nonnull
    @Override
    public I18nResolver getI18nResolver()
    {
        return resolver;
    }

    @Nonnull
    @Override
    public HttpServletRequest getRequest()
    {
        return request;
    }

    @Nonnull
    @Override
    public Map<String, Object> getContextMap()
    {
        return context;
    }
}
