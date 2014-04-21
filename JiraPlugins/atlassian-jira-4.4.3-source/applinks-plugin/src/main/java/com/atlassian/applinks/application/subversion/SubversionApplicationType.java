package com.atlassian.applinks.application.subversion;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;

import java.net.URI;

/**
 * @since   3.0
 */
public class SubversionApplicationType implements ApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("subversion");

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.subversion";
    }

    /**
     * @since   3.1
     */
    public URI getIconUrl()
    {
        return null;
    }
}
