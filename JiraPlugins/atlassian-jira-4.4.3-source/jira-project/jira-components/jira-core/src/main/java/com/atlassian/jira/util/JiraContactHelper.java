package com.atlassian.jira.util;

/**
 * Helper for getting User Contact information links
 *
 * @since v4.4
 */
public interface JiraContactHelper
{
    /**
     * Get the full link text for the contact administration message as a snippet.
     * This message is not puncuated or capatilised and should be able to be inserted within a more complete message.
     *
     * in English "contact your Jira Administrators"
     *
     * @param baseUrl Base Url of the application
     * @param i18nHelper i18NHelper
     * @return String containing HTML
     */
    String getAdministratorContactLinkHtml(String baseUrl, I18nHelper i18nHelper);

    /**
     * Get the text for the contact administration message as a snippet.
     * This message is not puncuated or capatilised and should be able to be inserted within a more complete message.
     * If you want a hyperlink then use {@link #getAdministratorContactLinkHtml(String baseUrl, I18nHelper i18nHelper)}
     *
     * in English "contact your Jira Administrators"
     *
     * @param i18nHelper i18NHelper
     * @return String containing HTML
     */
    String getAdministratorContactMessage(I18nHelper i18nHelper);

}
