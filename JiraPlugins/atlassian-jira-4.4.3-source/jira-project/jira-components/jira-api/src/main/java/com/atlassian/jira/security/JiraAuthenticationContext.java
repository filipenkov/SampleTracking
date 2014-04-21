package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Locale;

/**
 * The JiraAuthenticationContext is used for tracking a user's session in JIRA and all
 * it's custom parameters, such as Locale and I18n.
 */
public interface JiraAuthenticationContext
{
    /**
     * Returns the currently logged in User.
     *
     * @return The logged in User, or null
     *
     * @deprecated use {@link #getLoggedInUser()}. Since v4.3
     */
    com.opensymphony.user.User getUser();

    /**
     * Returns the currently logged in User.
     *
     * @return The logged in User, or null
     */
    User getLoggedInUser();

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
     */
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

    /**
     * This  comes to use in places like Jelly where we need to switch the identity of a user during execution.
     *
     * @param user the currently logged in user
     *
     * @deprecated use {@link #setLoggedInUser(com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    void setUser(com.opensymphony.user.User user);
}
