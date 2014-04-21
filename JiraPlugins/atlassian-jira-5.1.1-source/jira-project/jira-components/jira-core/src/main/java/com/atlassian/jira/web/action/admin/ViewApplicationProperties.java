/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Locale;

@WebSudoRequired
public class ViewApplicationProperties extends ProjectActionSupport
{
    protected final UserPickerSearchService searchService;
    protected final LocaleManager localeManager;
    protected final TimeZoneService timeZoneService;
    protected final RendererManager rendererManager;
    private final PluginAccessor pluginAccessor;
    private final GzipCompression gZipCompression;
    private final FeatureManager featureManager;
    private final IntroductionProperty introductionProperty;
    private boolean useGravatar;
    protected boolean disableInlineEdit;

    public ViewApplicationProperties(UserPickerSearchService searchService, LocaleManager localeManager, TimeZoneService timeZoneService, RendererManager rendererManager, PluginAccessor pluginAccessor, GzipCompression gZipCompression, FeatureManager featureManager, IntroductionProperty introductionProperty)
    {
        this.searchService = searchService;
        this.localeManager = localeManager;
        this.timeZoneService = timeZoneService;
        this.rendererManager = rendererManager;
        this.pluginAccessor = pluginAccessor;
        this.gZipCompression = gZipCompression;
        this.featureManager = featureManager;
        this.introductionProperty = introductionProperty;

        this.useGravatar = getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_AVATAR_FROM_GRAVATAR);
        this.disableInlineEdit = getApplicationProperties().getOption(APKeys.JIRA_OPTION_DISABLE_INLINE_EDIT);
    }

    public LocaleManager getLocaleManager()
    {
        return localeManager;
    }

    public String getJiraMode()
    {
        StringBuilder i18nString = new StringBuilder("admin.jira.mode.").append(getApplicationProperties().getString("jira.mode"));
        return getText(i18nString.toString());
    }

    public String getDisplayNameOfLocale(Locale locale)
    {
        return locale.getDisplayName(getLocale());
    }

    public boolean useSystemTimeZone()
    {
        return timeZoneService.useSystemTimeZone();
    }

    public TimeZoneInfo getDefaultTimeZoneInfo()
    {
        return timeZoneService.getDefaultTimeZoneInfo(getJiraServiceContext());
    }

    public boolean isUseGravatar()
    {
        return useGravatar;
    }

    public void setUseGravatar(boolean useGravatar)
    {
        this.useGravatar = useGravatar;
    }

    public String getContactAdministratorsMessage()
    {
        String message = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_CONTACT_ADMINISTRATORS_MESSSAGE);
        return rendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(message, null);
    }

    public boolean getShowPluginHints()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_SHOW_MARKETING_LINKS);
    }

    public String getTacUrl()
    {
        return HelpUtil.getInstance().getHelpPath("application.properties.server.language.from.tac").getUrl();
    }

    public boolean isDisableInlineEdit()
    {
        return disableInlineEdit;
    }

    public boolean isShowDisableInlineEdit()
    {
        return pluginAccessor.isPluginEnabled("com.atlassian.jira.jira-issue-nav-plugin");
    }

    public GzipCompression getGzipCompression()
    {
        return gZipCompression;
    }

    public IntroductionProperty getIntroductionProperty()
    {
        return introductionProperty;
    }
}
