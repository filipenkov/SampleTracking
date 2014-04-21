package com.atlassian.applinks.application.generic;

import java.net.URI;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

public class GenericEntityTypeImpl
        extends IconizedIdentifiableType
        implements NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("generic.entity");

    public GenericEntityTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return GenericApplicationTypeImpl.class;
    }

    public String getI18nKey()
    {
        return "applinks.generic.entity";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.generic.entity.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.generic.entity.short";
    }

    public URI getDisplayUrl(final ApplicationLink link, final String entity)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof GenericApplicationTypeImpl);

        // we don't know what kind of app the peer is, so we can't point to anything
        return null;
    }
}
