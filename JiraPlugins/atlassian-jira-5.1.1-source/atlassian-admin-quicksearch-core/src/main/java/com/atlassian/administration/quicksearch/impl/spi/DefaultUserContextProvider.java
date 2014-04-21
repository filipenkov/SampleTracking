package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.internal.ContextMapProvider;
import com.atlassian.administration.quicksearch.internal.NullContextMapProvider;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.administration.quicksearch.spi.UserContextProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * {@link com.atlassian.administration.quicksearch.spi.UserContextProvider} based on SAL API
 *
 * @since 1.0
 */
public class DefaultUserContextProvider implements UserContextProvider
{
    private static final String ATTRIBUTE_KEY = "com.atlassian.administration.quicksearch.USER_CONTEXT";

    private final UserManager userManager;
    private final I18nResolver i18nResolver;
    private final LocaleResolver localeResolver;
    private final ContextMapProvider contextMapProvider;

    public DefaultUserContextProvider(UserManager userManager, I18nResolver i18nResolver, LocaleResolver localeResolver,
                                      ContextMapProvider provider)
    {
        this.userManager = userManager;
        this.i18nResolver = i18nResolver;
        this.localeResolver = localeResolver;
        this.contextMapProvider = provider;
    }

    public DefaultUserContextProvider(UserManager userManager, I18nResolver i18nResolver, LocaleResolver localeResolver)
    {
        this(userManager, i18nResolver, localeResolver, NullContextMapProvider.INSTANCE);
    }

    @Override
    public UserContext getUserContext(HttpServletRequest request)
    {
        final UserContext existing = (UserContext) request.getAttribute(ATTRIBUTE_KEY);
        if (existing != null)
        {
            if (existing == UnauthenticatedContext.INSTANCE)
            {
                return null;
            }
            else
            {
                return existing;
            }
        }
        String username = userManager.getRemoteUsername(request);
        if (username == null)
        {
            request.setAttribute(ATTRIBUTE_KEY, UnauthenticatedContext.INSTANCE);
            return null;
        }
        else
        {
            final UserContext context = new DefaultUserContext(username, localeResolver.getLocale(request), i18nResolver, request,
                contextMapProvider.addContextTo(Collections.<String, Object>emptyMap(), request));
            request.setAttribute(ATTRIBUTE_KEY, context);
            return context;
        }
    }


    // TODO non-null :(
    private static enum UnauthenticatedContext implements UserContext
    {
        INSTANCE;


        @Override
        public String getUsername() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

        @Override
        public I18nResolver getI18nResolver() {
            return null;
        }

        @Override
        public HttpServletRequest getRequest() {
            return null;
        }

        @Override
        public Map<String, Object> getContextMap() {
            return null;
        }
    }
}
