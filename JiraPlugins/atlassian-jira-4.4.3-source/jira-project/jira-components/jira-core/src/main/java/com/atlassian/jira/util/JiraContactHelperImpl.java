package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

/**
 * @since v4.4
 */
public class JiraContactHelperImpl implements JiraContactHelper
{
    private final String ADMINISTRATORS_LINK = "secure/ContactAdministrators!default.jspa";

    public JiraContactHelperImpl()
    {

    }

    @Override
    public String getAdministratorContactLinkHtml(String baseUrl, I18nHelper i18nHelper)
    {
        if (baseUrl == null)
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

        return i18nHelper.getText("common.concepts.contact.administrator", link, closeLink);
    }

    @Override
    public String getAdministratorContactMessage(I18nHelper i18nHelper)
    {
        return i18nHelper.getText("common.concepts.contact.administrator", "", "");
    }
}
