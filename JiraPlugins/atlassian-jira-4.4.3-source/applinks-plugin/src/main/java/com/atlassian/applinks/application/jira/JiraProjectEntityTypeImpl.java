package com.atlassian.applinks.application.jira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.application.jira.JiraProjectEntityType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;

/**
 * Represents a project in a non-UAL JIRA instance.
 *
 * @since v3.0
 */
public class JiraProjectEntityTypeImpl
        extends IconizedIdentifiableType
        implements NonAppLinksEntityType, JiraProjectEntityType
{
    private static final TypeId TYPE_ID = new TypeId("jira.project");

    public JiraProjectEntityTypeImpl(final AppLinkPluginUtil pluginUtil, final WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.jira.project";
    }

    public String getPluralizedI18nKey()
    {
        return "applinks.jira.project.plural";
    }

    public String getShortenedI18nKey()
    {
        return "applinks.jira.project.short";
    }

    public Class<? extends ApplicationType> getApplicationType()
    {
        return JiraApplicationType.class;
    }

    public URI getDisplayUrl(final ApplicationLink link, final String project)
    {
        Assertions.isTrue(String.format("Application link %s is not of type %s",
                link.getId(), getApplicationType().getName()),
                link.getType() instanceof JiraApplicationType);

        return URIUtil.uncheckedConcatenate(link.getDisplayUrl(), "browse", project);
    }
}
