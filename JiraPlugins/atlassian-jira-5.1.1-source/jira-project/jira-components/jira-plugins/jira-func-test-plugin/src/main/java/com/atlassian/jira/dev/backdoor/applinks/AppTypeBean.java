package com.atlassian.jira.dev.backdoor.applinks;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * JAXB bean for AppLinks ApplicationType.
 *
 * @since v4.3
 */
class AppTypeBean
{
    @JsonProperty
    private String i18nKey;

    @JsonProperty
    private String iconUrl;

    AppTypeBean()
    {
    }

    AppTypeBean(String i18nKey, String iconUrl)
    {
        this.i18nKey = i18nKey;
        this.iconUrl = iconUrl;
    }
}
