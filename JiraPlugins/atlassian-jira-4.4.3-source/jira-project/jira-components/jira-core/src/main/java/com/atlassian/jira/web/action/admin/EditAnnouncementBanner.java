package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;

@WebSudoRequired
public class EditAnnouncementBanner extends JiraWebActionSupport
{
    public static final String ANNOUNCEMENT_PREVIEW = "announcement_preview_banner_st";

    private String announcement;
    private String bannerVisibility;
    private ApplicationProperties applicationProperties;
    public static final String PUBLIC_BANNER = "public";
    public static final String PRIVATE_BANNER = "private";

    public EditAnnouncementBanner(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String doDefault() throws Exception
    {
        String preview = request.getParameter(ANNOUNCEMENT_PREVIEW);
        announcement = (preview == null) ? applicationProperties.getDefaultBackedText(APKeys.JIRA_ALERT_HEADER) : preview;
        this.bannerVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY);
        return INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        applicationProperties.setText(APKeys.JIRA_ALERT_HEADER, announcement);
        applicationProperties.setString(APKeys.JIRA_ALERT_HEADER_VISIBILITY, bannerVisibility);
        return INPUT;
    }

    public Collection getVisibilityModes()
    {
        return EasyList.build(new TextOption(PUBLIC_BANNER, getText("admin.menu.optionsandsettings.announcement.banner.visibility.public")),
                              new TextOption(PRIVATE_BANNER, getText("admin.menu.optionsandsettings.announcement.banner.visibility.private")));
    }

    public String getAnnouncement()
    {
        return announcement;
    }

    public void setAnnouncement(String announcement)
    {
        this.announcement = announcement;
    }

    public boolean isPreview()
    {
        return request.getParameter(ANNOUNCEMENT_PREVIEW) != null;
    }

    public String getBannerVisibility()
    {
        return bannerVisibility;
    }

    public void setBannerVisibility(String bannerVisibility)
    {
        this.bannerVisibility = bannerVisibility;
    }
}