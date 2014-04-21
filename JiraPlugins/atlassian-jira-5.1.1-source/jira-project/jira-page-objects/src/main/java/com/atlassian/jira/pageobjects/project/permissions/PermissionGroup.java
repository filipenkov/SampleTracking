package com.atlassian.jira.pageobjects.project.permissions;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.4
 */
public class PermissionGroup
{
    private static final String PERMISSION_GROUP_PREFIX = "project-config-permissions-group-";
    private static final String PERMISSION_CLASS = "project-config-permission";
    private static final String DATA_ID = "data-id";
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;


    private PageElement permissionGroup;
    private PageElement nameCell;


    private final String id;
    private final String tableId;
    private final String nameId;

    public PermissionGroup(String id)
    {
        this.id = id;
        this.tableId = PERMISSION_GROUP_PREFIX + id;
        this.nameId = PERMISSION_GROUP_PREFIX + "name-" + id;
    }

    @Init
    public void initialise()
    {
        permissionGroup = elementFinder.find(By.id(this.tableId));
        nameCell = elementFinder.find(By.id(this.nameId));
    }

    public String getName()
    {
        return nameCell.getText();
    }


    public List<Permission> getPermissions()
    {
        List<Permission> permissions = new ArrayList<Permission>();

        if (!permissionGroup.find(By.className(PERMISSION_CLASS)).isPresent())
        {
            return permissions;
        }

        final List<PageElement> permissionElements = permissionGroup.findAll(By.className(PERMISSION_CLASS));

        for (PageElement permissionElement : permissionElements)
        {
            permissions.add(pageBinder.bind(Permission.class, permissionElement.getAttribute(DATA_ID)));
        }

        return permissions;
    }
}
