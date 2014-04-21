package com.atlassian.jira.pageobjects.project.permissions;


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
public class Permission
{
    private static final String PERMISSION_NAME_CLASS = "project-config-permission-name";
    private static final String PERMISSION_DESC_CLASS = "project-config-permission-description";
    private static final String PERMISSION_ENTITIES_CLASS = "project-config-permission-entity";
    private static final String PERMISSION_ID_PREFIX = "project-config-permissions-";
    @Inject
    private PageElementFinder elementFinder;


    private final String id;
    private final String rowId;

    private PageElement permission;

    private PageElement nameCell;
    private PageElement descriptionCell;
    private List<PageElement> entityCells;

    public Permission(String id)
    {
        this.id = id;
        this.rowId = PERMISSION_ID_PREFIX + id;
    }

    @Init
    public void initialise()
    {
        permission = elementFinder.find(By.id(this.rowId));
        nameCell = permission.find(By.className(PERMISSION_NAME_CLASS));
        descriptionCell = permission.find(By.className(PERMISSION_DESC_CLASS));
        entityCells = permission.findAll(By.className(PERMISSION_ENTITIES_CLASS));
    }

    public String getName()
    {
        return nameCell.getText();
    }

    public String getDescription()
    {
        return descriptionCell.getText();
    }

    public List<String> getEntities()
    {
        final List<String> entities = new ArrayList<String>();
        for (PageElement entity : entityCells)
        {
            entities.add(entity.getText());
        }

        return entities;
    }


}
