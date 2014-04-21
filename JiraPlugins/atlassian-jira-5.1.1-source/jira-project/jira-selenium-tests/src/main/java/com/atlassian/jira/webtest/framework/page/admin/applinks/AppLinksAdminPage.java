package com.atlassian.jira.webtest.framework.page.admin.applinks;

import com.atlassian.jira.webtest.framework.page.admin.AdminPage;

/**
 * Represents the AppLinks administration page.
 *
 * @since v4.3
 */
public interface AppLinksAdminPage extends AdminPage
{
    /**
     * Clicks on the "Add Application Link" button.
     *
     * @return a NewAppLinkWizard
     */
    NewAppLinkWizard clickAddApplicationLink();

    /**
     * Returns the page object for the application link to the application having the given base URL.
     *
     * @param applicationBaseURL a String containing the application base URL
     * @return an ApplicationLink
     */
    ApplicationLink applicationLink(String applicationBaseURL);
}
