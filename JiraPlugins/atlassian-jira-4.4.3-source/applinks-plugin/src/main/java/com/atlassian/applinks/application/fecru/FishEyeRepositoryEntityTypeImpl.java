package com.atlassian.applinks.application.fecru;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType;
import com.atlassian.applinks.api.application.fecru.FishEyeRepositoryEntityType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;

public class FishEyeRepositoryEntityTypeImpl
        extends IconizedIdentifiableType
        implements FishEyeRepositoryEntityType, NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("fecru.repository");

    public FishEyeRepositoryEntityTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.fecru.repository";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.fecru.repository.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.fecru.repository.short";
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return FishEyeCrucibleApplicationType.class;
    }

    public URI getDisplayUrl(final ApplicationLink link, final String repo)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof FishEyeCrucibleApplicationType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), "changelog", repo);
    }
}