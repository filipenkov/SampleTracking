package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This objects holds the values of the selected form configuration options.
 *
 * @since 3.0
 */
@XmlRootElement (name = "configFormValues")
public class ConfigurationFormValuesEntity
{
    private boolean trustEachOther;
    private boolean shareUserbase;

    @SuppressWarnings("unused")
    public ConfigurationFormValuesEntity()
    {
    }

    public ConfigurationFormValuesEntity(final boolean trustEachOther, final boolean shareUserbase)
    {
        this.trustEachOther = trustEachOther;
        this.shareUserbase = shareUserbase;
    }

    public boolean shareUserbase()
    {
        return shareUserbase;
    }

    public boolean trustEachOther()
    {
        return trustEachOther;
    }
}
