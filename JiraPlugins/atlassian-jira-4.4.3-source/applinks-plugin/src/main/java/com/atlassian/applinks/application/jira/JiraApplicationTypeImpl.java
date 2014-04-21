package com.atlassian.applinks.application.jira;

import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.application.IconizedIdentifiableType;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * Used for both:
 * <ul>
 *  <li>a UAL-enabled JIRA instance; and</li>
 *  <li>a JIRA instance that predates UAL but does has have Trusted Applications support.</li>
 * </ul>
 *
 * @since v3.0
 */
public class JiraApplicationTypeImpl extends IconizedIdentifiableType implements JiraApplicationType, NonAppLinksApplicationType
{
    static final TypeId TYPE_ID = new TypeId("jira");

    public JiraApplicationTypeImpl(AppLinkPluginUtil pluginUtil, WebResourceManager webResourceManager)
    {
        super(pluginUtil, webResourceManager);
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "applinks.jira";
    }
}
