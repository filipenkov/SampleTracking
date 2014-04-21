package com.atlassian.applinks.application.bamboo;

import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

public class BambooApplicationTypeImpl extends IconizedIdentifiableType implements BambooApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("bamboo");

    public BambooApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.bamboo";
    }

}
