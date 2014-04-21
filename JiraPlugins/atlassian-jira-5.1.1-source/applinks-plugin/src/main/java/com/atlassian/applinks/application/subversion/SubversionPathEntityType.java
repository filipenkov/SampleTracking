package com.atlassian.applinks.application.subversion;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;

import java.net.URI;

public class SubversionPathEntityType implements NonAppLinksEntityType
{
    private static final TypeId TYPE_ID = new TypeId("subvesion.path");

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return SubversionApplicationType.class;
    }

    public String getI18nKey()
    {
        return "applinks.subversion.path";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.subversion.path.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.subversion.path.short";
    }

    /**
     * @since   3.1
     */
    public URI getIconUrl()
    {
        return null;
    }

    /**
     * @since   3.1
     */
    public URI getDisplayUrl(final ApplicationLink link, final String path)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof SubversionPathEntityType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), path);
    }
}
