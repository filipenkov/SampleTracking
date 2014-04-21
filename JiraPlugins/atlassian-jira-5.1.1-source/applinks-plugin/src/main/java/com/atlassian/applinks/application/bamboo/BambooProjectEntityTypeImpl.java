package com.atlassian.applinks.application.bamboo;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooProjectEntityType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;

public class BambooProjectEntityTypeImpl
        extends IconizedIdentifiableType
        implements BambooProjectEntityType, NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("bamboo.project");

    public BambooProjectEntityTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return BambooApplicationType.class;
    }

    public String getI18nKey()
    {
        return "applinks.bamboo.project";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.bamboo.project.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.bamboo.project.short";
    }

    public URI getDisplayUrl(final ApplicationLink link, final String entityKey)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof BambooApplicationType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), "browse", entityKey);
    }
}
