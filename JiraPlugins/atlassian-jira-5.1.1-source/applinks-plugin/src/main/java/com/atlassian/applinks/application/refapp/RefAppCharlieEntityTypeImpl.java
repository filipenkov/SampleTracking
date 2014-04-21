package com.atlassian.applinks.application.refapp;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.refapp.RefAppApplicationType;
import com.atlassian.applinks.api.application.refapp.RefAppCharlieEntityType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;

public class RefAppCharlieEntityTypeImpl
        extends IconizedIdentifiableType
        implements RefAppCharlieEntityType, NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("refapp.charlie");

    public RefAppCharlieEntityTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return RefAppApplicationType.class;
    }

    public String getI18nKey()
    {
        return "applinks.refapp.charlie";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.refapp.charlie.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.refapp.charlie.short";
    }

    public URI getDisplayUrl(final ApplicationLink link, final String charlie)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof RefAppApplicationType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), "plugins", "servlet", "charlie", charlie);
    }
}