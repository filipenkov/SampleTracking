package com.atlassian.jira.pageobjects.project.permissions;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.admin.EditPermissionScheme;
import com.atlassian.jira.pageobjects.pages.admin.SelectPermissionScheme;
import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
public class ProjectPermissionPageTab extends AbstractProjectConfigPageTab
{
    public static final String TAB_LINK_ID = "view_project_permissions_tab";
    private static final String URI_TEMPLATE = "/plugins/servlet/project-config/%s/permissions";

    private static final String SCHEME_NAME_ID = "project-config-permissions-scheme-name";
    private static final String EDIT_LINK_ID = "project-config-permissions-scheme-edit";
    private static final String CHANGE_LINK_ID = "project-config-permissions-scheme-change";

    private final String uri;

    @ElementBy (id = "project-config-panel-permissions")
    private PageElement permissionsPage;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    private PageElement schemeName;
    private PageElement schemeEditLink;
    private PageElement schemeChangeLink;
    private DropDown dropDown;

    public ProjectPermissionPageTab(String projectKey)
    {
        this.uri = String.format(URI_TEMPLATE, projectKey);
    }

    @Init
    public void initialise()
    {
        dropDown = pageBinder.bind(DropDown.class, By.id("project-config-tab-actions"), By.id("project-config-tab-actions-list"));
        schemeName = elementFinder.find(By.id(SCHEME_NAME_ID));
        schemeEditLink = elementFinder.find(By.id(EDIT_LINK_ID));
        schemeChangeLink = elementFinder.find(By.id(CHANGE_LINK_ID));
    }

    public String getSchemeName()
    {
        return schemeName.getText();
    }

    public String getSchemeDescription()
    {
        final String title = schemeName.getAttribute("title");
        return (title == null) ? "" : title;
    }

    public boolean isSchemeLinked()
    {
        return dropDown.hasItemById(EDIT_LINK_ID);
    }

    public boolean isSchemeChangeAvailable()
    {
        return dropDown.hasItemById(CHANGE_LINK_ID);
    }

    public EditPermissionScheme gotoScheme()
    {
        final String schemeId = schemeEditLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(EDIT_LINK_ID), EditPermissionScheme.class, Long.valueOf(schemeId));
    }

    public SelectPermissionScheme gotoSelectScheme()
    {
        final String projectId = schemeChangeLink.getAttribute("data-id");
        return dropDown.openAndClick(By.id(CHANGE_LINK_ID), SelectPermissionScheme.class, Long.valueOf(projectId));
    }

    public List<PermissionGroup> getPermissionGroups()
    {
        List<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>();

        if (!permissionsPage.find(By.className("project-config-permissions")).isPresent())
        {
            return permissionGroups;
        }

        final List<PageElement> permissionGroupElements = permissionsPage.findAll(By.className("project-config-permissions"));

        for (PageElement permissionGroupElement : permissionGroupElements)
        {
            permissionGroups.add(pageBinder.bind(PermissionGroup.class, permissionGroupElement.getAttribute("data-id")));
        }

        return permissionGroups;
    }

    public Permission getPermissionByName(String name)
    {
        final List<PermissionGroup> permissionGroups = getPermissionGroups();
        for (PermissionGroup permissionGroup : permissionGroups)
        {
            final List<Permission> permissions = permissionGroup.getPermissions();
            for (Permission permission : permissions)
            {
                if (permission.getName().equals(name))
                {
                    return permission;
                }
            }
        }

        return null;
    }

    @Override
    public TimedCondition isAt()
    {
        return permissionsPage.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }
}
