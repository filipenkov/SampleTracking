package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import java.util.Map;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * The implementation of the {@link com.atlassian.plugin.webresource.WebResourceIntegration} for JIRA.
 */
public class JiraWebResourceIntegration implements WebResourceIntegration
{
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final I18nBean.CachingFactory i18nFactory;

    public JiraWebResourceIntegration(
            final PluginAccessor pluginAccessor, final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory requestContextFactory, final BuildUtilsInfo buildUtilsInfo,
            final JiraAuthenticationContext jiraAuthenticationContext, I18nBean.CachingFactory i18nFactory)
    {
        this.i18nFactory = i18nFactory;
        this.pluginAccessor = notNull("pluginAccessor", pluginAccessor);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.requestContextFactory = notNull("requestContextFactory", requestContextFactory);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraAuthenticationContext = notNull("jiraAuthenticationContext", jiraAuthenticationContext);
    }

    public PluginAccessor getPluginAccessor()
    {
        return pluginAccessor;
    }

    public Map<String, Object> getRequestCache()
    {
        return JiraAuthenticationContextImpl.getRequestCache();
    }

    public String getSystemCounter()
    {
        return applicationProperties.getDefaultBackedString(APKeys.WEB_RESOURCE_FLUSH_COUNTER);
    }

    public String getSystemBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    public String getBaseUrl()
    {
        return getBaseUrl(UrlMode.AUTO);
    }

    public String getBaseUrl(final UrlMode urlMode)
    {
        switch (urlMode)
        {
            case RELATIVE:
            case AUTO:
                return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            case ABSOLUTE:
                return requestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            default:
                throw new AssertionError("Unsupported URLMode: " + urlMode);
        }
    }

    public String getSuperBatchVersion()
    {
        return applicationProperties.getDefaultBackedString(APKeys.WEB_RESOURCE_SUPER_BATCH_FLUSH_COUNTER);
    }

    @Override
    public String getStaticResourceLocale()
    {
        final I18nHelper i18n = jiraAuthenticationContext.getI18nHelper();
        return i18n.getLocale().toString() + i18nFactory.getStateHashCode();
    }
}
