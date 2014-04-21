package com.atlassian.jira.dev.backdoor.applinks;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean for AppLinks EntityType.
 *
 * @since v4.3
 */
@XmlRootElement
class EntityTypeBean
{
    public final String applicationTypeClassName;
    public final String i18nKey;
    public final String pluralizedI18nKey;
    public final String iconUrl;

    EntityTypeBean(String applicationTypeClassName, String i18nKey, String pluralizedI18nKey, String iconUrl)
    {
        this.applicationTypeClassName = applicationTypeClassName;
        this.i18nKey = i18nKey;
        this.pluralizedI18nKey = pluralizedI18nKey;
        this.iconUrl = iconUrl;
    }
}
