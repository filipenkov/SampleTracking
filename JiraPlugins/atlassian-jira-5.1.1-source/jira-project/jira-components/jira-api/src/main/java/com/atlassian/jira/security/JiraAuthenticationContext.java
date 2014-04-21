package com.atlassian.jira.security;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Locale;

/**
 * The JiraAuthenticationContext is used for tracking a user's session in JIRA and all
 * it's custom parameters, such as Locale and I18n.
 */
@PublicApi
public interface JiraAuthenticationContext
{
    /**
     * Returns the currently logged in User.
     *
     * @return The logged in User, or null
     * @deprecated use {@link #getLoggedInUser()}. Since v4.3
     */
    User getUser();

    /**
     * Returns the currently logged in User.
     *
     * @return The logged in User, or null
     */
    User getLoggedInUser();

    /**
     * Returns a boolean indicating whether there is a currently logged in user.
     *
     * @return true if there is a currently logged in user
     * @since v5.0.4
     */
    boolean isLoggedInUser();

    /**
     * Get the users locale.
     *
     * @return The user's locale, or the default system locale.
     */
    Locale getLocale();

    /**
     * Method used to get a nice representation of a date using a user's locale.
     *
     * @return A {@link OutlookDate}
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter} instead. Since v5.0.
     */
    @Deprecated
    OutlookDate getOutlookDate();

    /**
     * @deprecated Use getText() method on {@link #getI18nHelper()}.
     *
     * @param key the text key
     * @return the translated text
     */
    @Deprecated
    String getText(String key);

    /**
     * Useful for localisation of messages.
     *
     * @return An instance of {@link I18nHelper}
     */
    I18nHelper getI18nHelper();

    /**
     * Useful for localisation of messages.
     *
     * @return An instance of {@link I18nHelper}
     *
     * @deprecated Use {@link #getI18nHelper()} instead. Deprecated since v4.0
     */
    @Deprecated
    I18nHelper getI18nBean();

    /**
     * This  comes to use in places like Jelly where we need to switch the identity of a user during execution.
     *
     * @param user the currently logged in user
     */
    void setLoggedInUser(User user);

}
