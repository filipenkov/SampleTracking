package com.atlassian.applinks.application.confluence;

import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

public class ConfluenceApplicationTypeImpl extends IconizedIdentifiableType implements ConfluenceApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("confluence");

    public ConfluenceApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public String getI18nKey()
    {
        return "applinks.confluence";
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }
}
