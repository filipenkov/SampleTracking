package com.atlassian.jira.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import javax.annotation.Nullable;

/**
 * @since v4.4
 */
public class JiraContactHelperImpl implements JiraContactHelper
{
    static final String CONTACT_ADMINISTRATOR_KEY = "common.concepts.contact.administrator";
    static final String ADMINISTRATORS_LINK = "secure/ContactAdministrators!default.jspa";

    private final ApplicationProperties applicationProperties;

    public JiraContactHelperImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getAdministratorContactLinkHtml(@Nullable String baseUrl, I18nHelper i18nHelper)
    {
        if (baseUrl == null || isContactFormTurnedOff())
        {
            return getAdministratorContactMessage(i18nHelper);
        }
        String url;
        if (baseUrl.endsWith("/"))
        {
            url = baseUrl + ADMINISTRATORS_LINK;
        }
        else
        {
            url = baseUrl + "/" + ADMINISTRATORS_LINK;
        }
        String link = "<a href=\"" + url + "\">";
        String closeLink = "</a>";

        return i18nHelper.getText(CONTACT_ADMINISTRATOR_KEY, link, closeLink);
    }


    private boolean isContactFormTurnedOff()
    {
        return !applicationProperties.getOption(APKeys.JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM);
    }

    @Override
    public String getAdministratorContactMessage(I18nHelper i18nHelper)
    {
        return i18nHelper.getText(CONTACT_ADMINISTRATOR_KEY, "", "");
    }
}
