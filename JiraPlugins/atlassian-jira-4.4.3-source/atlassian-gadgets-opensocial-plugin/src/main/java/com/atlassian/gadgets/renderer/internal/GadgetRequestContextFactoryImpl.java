package com.atlassian.gadgets.renderer.internal;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;

import org.apache.commons.lang.BooleanUtils;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;

/**
 * Default implementation of {@code GadgetRequestContextFactory}.
 */
public final class GadgetRequestContextFactoryImpl implements GadgetRequestContextFactory
{
    final static String IGNORE_CACHE_PROPERTY_KEY = "com.atlassian.gadgets.dashboard.ignoreCache";
    final static String DEBUG_PROPERTY_KEY = "com.atlassian.gadgets.debug";

    private final LocaleResolver localeResolver;
    private final UserManager userManager;

    /**
     * Constructor.
     * @param localeResolver {@code LocaleResolver} implementation to use
     * @param userManager {@code UserManager} implementation to use to retrieve the user associated with a request 
     */
    public GadgetRequestContextFactoryImpl(LocaleResolver localeResolver, UserManager userManager)
    {
        this.localeResolver = localeResolver;
        this.userManager = userManager;
    }

    public GadgetRequestContext get(HttpServletRequest request)
    {
        Locale locale = localeResolver.getLocale(request);
        String viewer = userManager.getRemoteUsername(request);

        return gadgetRequestContext()
            .locale(locale)
            .ignoreCache(getCacheSetting(request))
            .debug(isDebugEnabled(request))
            .viewer(viewer)
            .build();
    }

    private boolean getCacheSetting(HttpServletRequest request)
    {
        return isEnabled(request, "ignoreCache", IGNORE_CACHE_PROPERTY_KEY, false);
    }

    private boolean isDebugEnabled(HttpServletRequest request)
    {
        return isEnabled(request, "debug", DEBUG_PROPERTY_KEY, false);
    }
    
    private boolean isEnabled(HttpServletRequest request, String parameterName, String propertyName, boolean defaultValue)
    {
        // if there is a setting on the request, use that
        Boolean enabled = BooleanUtils.toBooleanObject(request.getParameter(parameterName));
        if (enabled != null)
        {
            return enabled;
        }

        // if there is a system property, use that
        enabled = BooleanUtils.toBooleanObject(System.getProperty(propertyName));
        if (enabled != null)
        {
            return enabled;
        }

        return defaultValue;
    }
}
