package com.atlassian.applinks.application.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.api.application.confluence.ConfluenceSpaceEntityType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;

public class ConfluenceSpaceEntityTypeImpl
        extends IconizedIdentifiableType
        implements ConfluenceSpaceEntityType, NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("confluence.space");

    public ConfluenceSpaceEntityTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return ConfluenceApplicationType.class;
    }

    public String getI18nKey()
    {
        return "applinks.confluence.space";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.confluence.space.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.confluence.space.short";
    }

    public URI getDisplayUrl(final ApplicationLink link, final String space)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof ConfluenceApplicationType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), "display", space);
    }
}
