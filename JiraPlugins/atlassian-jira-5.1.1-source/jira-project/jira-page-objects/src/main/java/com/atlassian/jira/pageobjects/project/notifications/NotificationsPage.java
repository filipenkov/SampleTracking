package com.atlassian.jira.pageobjects.project.notifications;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.EditNotificationsPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.project.summary.notifications.ProjectEmailDialog;
import com.atlassian.jira.pageobjects.project.summary.notifications.SelectNotificationScheme;
import com.atlassian.jira.pageobjects.project.summary.notifications.OutgoingMailServers;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the notifications page in JIRA.
 *
 * @since v4.4
 */
public class NotificationsPage extends AbstractJiraPage
{
    private static final String SCHEME_NAME_ID = "project-config-notification-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-notification-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-notification-scheme-change";

    @ElementBy(id = "project-config-panel-header")
    private PageElement header;

    @ElementBy(id = "project-config-mailserver")
    private PageElement mailServerMessage;

    @ElementBy(id = "project-config-notifications-table")
    private PageElement table;

    @ElementBy(id = "project-config-notifications-none")
    private PageElement noNotificationsMessage;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy(id = "project-config-email")
    private PageElement projectEmail;

    @ElementBy(id = "project-config-email-change")
    private PageElement changeProjectEmail;

    @ElementBy(id = "project-config-mailserver")
    private PageElement mailServer;

    @ElementBy(id="project-config-mailserver-config")
    private PageElement configureMailServer;

    @Inject
    private PageBinder binder;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    private final String projectKey;

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    public NotificationsPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public List<Notification> getNotifications()
    {
        List<Notification> notifications = new ArrayList<Notification>();

        List<PageElement> rows = table.findAll(By.cssSelector(".project-config-notification"));
        for (PageElement row : rows)
        {
            Notification notification = new Notification();
            List<PageElement> tds = row.findAll(By.cssSelector("td"));

            boolean hasNotification = false;
            for (PageElement td : tds)
            {
                if (td.hasClass("project-config-notification-name"))
                {
                    notification.setName(StringUtils.stripToNull(td.getText()));
                    hasNotification = true;
                }
                else if (td.hasClass("project-config-notification-entitylist"))
                {
                    List<String> entities = new ArrayList<String>();
                    List<PageElement> lis = td.findAll(By.cssSelector("li"));
                    for (PageElement li : lis)
                    {
                        entities.add(li.getText());
                    }
                    notification.setEntities(entities);
                }
            }
            if (hasNotification)
            {
                notifications.add(notification);
            }
        }
        return notifications;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey + "/notifications";
    }

    @Override
    public TimedCondition isAt()
    {
        return table.timed().isPresent();
    }

    public boolean hasNotificationScheme()
    {
        return !schemeName.hasClass("project-config-none");
    }

    public String getNotificationSchemeName()
    {
        if (hasNotificationScheme())
        {
            return schemeName.getText();
        }
        else
        {
            return null;
        }
    }

    public String getDesc()
    {
        return schemeName.getAttribute("title");
    }

    public boolean isNotificationsMessagePresent()
    {
        return noNotificationsMessage.isPresent();
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public boolean hasProjectEmail()
    {
        return projectEmail.isPresent();
    }

    public String getProjectEmail()
    {
        return projectEmail.getText();
    }

    public boolean canEditEmail()
    {
        return changeProjectEmail.isPresent();
    }

    public boolean canChangeMailServer()
    {
        return configureMailServer.isPresent();
    }

    public ProjectEmailDialog openProjectEmailDialog()
    {
        changeProjectEmail.click();
        return binder.bind(ProjectEmailDialog.class);
    }

    public boolean hasMailServer()
    {
        return !configureMailServer.isPresent() && !mailServerMessage.isPresent();
    }

    public OutgoingMailServers configureMailServer()
    {
        configureMailServer.click();
        return binder.bind(OutgoingMailServers.class);
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public EditNotificationsPage gotoScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditNotificationsPage.class, Long.valueOf(schemeId));
    }

    public SelectNotificationScheme gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), SelectNotificationScheme.class, Long.valueOf(projectId));
    }
}
