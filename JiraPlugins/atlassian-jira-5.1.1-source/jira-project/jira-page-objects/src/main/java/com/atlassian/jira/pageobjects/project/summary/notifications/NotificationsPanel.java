package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class NotificationsPanel extends AbstractSummaryPanel
{
    @ElementBy (id = "project-config-notif")
    private PageElement notificationScheme;

    @ElementBy(id = "project-config-email", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement projectEmail;

    @ElementBy(id = "project-config-email-change")
    private PageElement changeProjectEmail;

    @ElementBy(id = "project-config-mailserver")
    private PageElement mailServer;

    @ElementBy(id="project-config-mailserver-config")
    private PageElement configureMailServer;

    @Inject
    private PageBinder binder;

    public String getNotificationSchemeLinkText()
    {
        return notificationScheme.getText();
    }

    public String getNotificationSchemeLinkUrl()
    {
        return notificationScheme.getAttribute("href");
    }

    public NotificationScheme viewNotificationScheme()
    {
        notificationScheme.click();
        return binder.bind(NotificationScheme.class);
    }

    public Boolean hasProjectEmail()
    {
        return projectEmail.isPresent();
    }

    public String getProjectEmail()
    {
        return projectEmail.getText();
    }

    public PageElement projectEmailHolder()
    {
        return projectEmail;
    }

    public Boolean hasEditEmailPermissions()
    {
        return changeProjectEmail.isPresent();
    }

    public Boolean hasConfigureMailPermissions()
    {
        return configureMailServer.isPresent();
    }

    public ProjectEmailDialog openProjectEmailDialog()
    {
        changeProjectEmail.click();
        return binder.bind(ProjectEmailDialog.class);
    }

    public Boolean hasServerConfiguration()
    {
        return !configureMailServer.isPresent();
    }

    public OutgoingMailServers configureMailServer()
    {
        configureMailServer.click();
        return binder.bind(OutgoingMailServers.class);
    }

}
