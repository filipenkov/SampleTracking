package com.atlassian.administration.quicksearch.spi;

import com.atlassian.sal.api.message.I18nResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * <p/>
 * Represents current user context.
 *
 * <p/>
 * User context is unique by means of the username and locale and those values can be used to cache evaluations of
 * methods that take this class as parameter (along with other parameters).
 *
 * <p/>
 * Given an unauthenticated request, all methods of this interface except for {@link #getRequest()} will return <code>null</code>
 *
 * @since v5.0
 */
public interface UserContext extends RenderingContext
{

    /**
     * Name of the current user. If there is no authenticated user associated with the executing request,
     * return <code>null</code>.
     *
     * @return current user, or <code>null</code> if no user authenticated
     */
    @Nullable
    String getUsername();

    /**
     * Check whether there is an authenticated user.
     *
     * @return <code>true</code>, if there is an authenticated user associated with the current request.
     */
    boolean isAuthenticated();

    /**
     * Get locale associated with the current user. Default locale if {@link #isAuthenticated()} returns <code>false</code>.
     *
     * @return get user locale
     */
    @Nonnull
    Locale getLocale();

    /**
     * Get I18n resolver associated with the current user. Default language resolver if no user associated with
     * this context ({@link #isAuthenticated()} returns <code>false</code>).
     *
     * @return i18n resolver
     */
    @Nonnull
    I18nResolver getI18nResolver();

}
