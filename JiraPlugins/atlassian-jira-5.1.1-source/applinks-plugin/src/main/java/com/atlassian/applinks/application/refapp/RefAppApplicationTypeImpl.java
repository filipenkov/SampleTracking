package com.atlassian.applinks.application.refapp;

import com.atlassian.applinks.api.application.refapp.RefAppApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

public class RefAppApplicationTypeImpl extends IconizedIdentifiableType implements RefAppApplicationType
{
    static final TypeId TYPE_ID = new TypeId("refapp");

    public RefAppApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.refapp";
    }

}