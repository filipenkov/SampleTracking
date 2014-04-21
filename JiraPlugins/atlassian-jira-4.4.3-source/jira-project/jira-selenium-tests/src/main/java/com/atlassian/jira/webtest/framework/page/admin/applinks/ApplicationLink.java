package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * Page object for an application link on the application links admin page.
 *
 * @since v4.3
 */
public interface ApplicationLink extends PageObject
{
    /**
     * Returns the remote application name.
     *
     * @return a TimedQuery<String>
     */
    TimedQuery<String> name();

    /**
     * Returns true if this link is not present on the page.
     *
     * @return a TimeCondition indicating whether this link is present on the page
     */
    TimedCondition isNotPresent();

    /**
     * Returns the remote application base URL.
     *
     * @return a TimedQuery<String>
     */
    TimedQuery<String> baseURL();

    boolean checkForConfiguredAuthenticationType(AuthType authType);

    /**
     * Clicks the "Delete" link on the
     *
     * @return a DeleteApplicationLink
     */
    DeleteApplicationLink clickDelete();

    /**
     * Possible authentication types.
     */
    enum AuthType
    {
        NONE(null), BASIC_AUTH("BasicAuthenticationProvider"), OAUTH("OAuthenticationProvider"), TRUSTED_APPS("TrustedAppsAuthenticationProvider");

        private final String className;

        AuthType(String className)
        {
            this.className = className;
        }

        public String getAuthProviderClass()
        {
            return className;
        }
    }
}
