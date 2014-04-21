package com.atlassian.jira.dev.backdoor.applinks;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean for AppLinks ApplicationType.
 *
 * @since v4.3
 */
@XmlRootElement
class AppTypeBean
{
    public final String i18nKey;
    public final String iconUrl;

    AppTypeBean(String i18nKey, String iconUrl)
    {
        this.i18nKey = i18nKey;
        this.iconUrl = iconUrl;
    }
}
