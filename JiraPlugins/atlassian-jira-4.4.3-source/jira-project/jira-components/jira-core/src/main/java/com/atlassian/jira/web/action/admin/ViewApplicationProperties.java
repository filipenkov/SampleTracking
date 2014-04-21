/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Locale;

@WebSudoRequired
public class ViewApplicationProperties extends ProjectActionSupport
{
    protected final UserPickerSearchService searchService;
    private final LocaleManager localeManager;
    protected final TimeZoneService timeZoneService;
    protected final RendererManager rendererManager;

    public ViewApplicationProperties(UserPickerSearchService searchService, LocaleManager localeManager, TimeZoneService timeZoneService, RendererManager rendererManager)
    {
        this.searchService = searchService;
        this.localeManager = localeManager;
        this.timeZoneService = timeZoneService;
        this.rendererManager = rendererManager;
    }

    /**
     * Indicates the current AJAX user picker application setting.
     * @return true only if the AJAX user picker is turned on.
     */
    public boolean canPerformAjaxSearch()
    {
        return searchService.isAjaxSearchEnabled();
    }

    public LocaleManager getLocaleManager()
    {
        return localeManager;
    }

    public String getJiraMode()
    {
        StringBuffer i8nString = new StringBuffer("admin.jira.mode.").append(getApplicationProperties().getString("jira.mode"));
        return getText(i8nString.toString());
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
        return HelpUtil.getInstance().getHelpPath("plugin.hint.tac").getUrl();
    }
}
