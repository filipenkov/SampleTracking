package com.atlassian.applinks.application.fecru;

import com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * @since   3.0
 */
public class FishEyeCrucibleApplicationTypeImpl extends IconizedIdentifiableType implements FishEyeCrucibleApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("fecru");

    public FishEyeCrucibleApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.fecru";
    }
}