package com.atlassian.applinks.application.generic;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * 
 * The generic application type supports all "out-of-the-box" authentication types that UAL ships with.
 * This application type can be used to authenticate to an application for which UAL does not have a specific application type,
 * but the application supports one or more of UAL's authentication providers.
 * 
 * @since   3.0
 */
public class GenericApplicationTypeImpl extends IconizedIdentifiableType implements ApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("generic");

    public GenericApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.generic";
    }

}
