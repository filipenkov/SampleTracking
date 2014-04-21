package com.atlassian.jira.pageobjects.project.components;

import com.atlassian.jira.pageobjects.global.SoyUserProfileLink;
import com.atlassian.jira.pageobjects.project.AbstractProjectConfigPageTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Represents the components page in JIRA.
 *
 * @since v4.4
 */
public class ComponentsPageTab extends AbstractProjectConfigPageTab
{
    public static final String TAB_LINK_ID = "view_project_components_tab";

    private final static Map<String, Component.ComponentAssigneeType> ASSIGNEE_TYPE_MAP;

    @ElementBy (id = "project-config-components-table")
    private PageElement table;

    @Inject
    private PageBinder binder;

    private EditComponentForm createComponentForm;

    private final String projectKey;

    // TODO this should be encapsulated in a RestfulTable page object
    @ElementBy (className = "aui-restfultable-init")
    private PageElement tableInit;

    static
    {
        Map<String, Component.ComponentAssigneeType> map = new HashMap<String, Component.ComponentAssigneeType>();
        map.put("project-config-component-project-default", Component.ComponentAssigneeType.PROJECT_DEFAULT);
        map.put("project-config-component-component-lead", Component.ComponentAssigneeType.COMPONENT_LEAD);
        map.put("project-config-component-project-lead", Component.ComponentAssigneeType.PROJECT_LEAD);
        map.put("project-config-component-unassigned", Component.ComponentAssigneeType.UNASSIGNED);
        map.put("project-config-component-unknown", Component.ComponentAssigneeType.UNKNOWN);

        ASSIGNEE_TYPE_MAP = Collections.unmodifiableMap(map);
    }


    public ComponentsPageTab(String projectKey)
    {
        this.projectKey = projectKey;
    }

    @WaitUntil
    public void isReady()
    {
       waitUntilFalse(isLoading());
    }

    @Init
    public void initialise()
    {
        createComponentForm = pageBinder.bind(EditComponentForm.class, By.className("aui-restfultable-create"));
    }

    public TimedCondition isLoading()
    {
        return tableInit.timed().isPresent();
    }

    public List<Component> getComponents()
    {
        List<Component> components = new ArrayList<Component>();

        List<PageElement> rows = table.findAll(By.cssSelector(".project-config-component"));
        for (PageElement row : rows)
        {
            if (row.find(By.cssSelector(".project-config-no-entires")).isPresent())
            {
                continue;
            }

            Component component = binder.bind(Component.class, row.getAttribute("data-id"));
            List<PageElement> tds = row.findAll(By.cssSelector("td"));

            for (PageElement td : tds)
            {
                if (td.hasClass("project-config-component-name"))
                {
                    component.setName(StringUtils.stripToNull(td.getText()));
                }
                else if (td.hasClass("project-config-component-description"))
                {
                    component.setDescription(StringUtils.stripToNull(td.getText()));
                }
                else if (td.hasClass("project-config-component-lead"))
                {
                    SoyUserProfileLink profileLink = SoyUserProfileLink.parse(td);
                    if (profileLink != null)
                    {
                        component.setLeadLink(profileLink).setLead(profileLink.getUser());
                    }
                    if (td.find(By.className("project-config-invalid")).isPresent())
                    {
                        component.setHasInvalidLead(true);
                    }

                }
                else if (td.hasClass("project-config-component-assignee"))
                {
                    PageElement span = td.find(By.className("project-config-component-assignee"));

                    component.setComponentAssigneeType(getAssigneType(span));

                    if (span.hasClass("project-config-invalid"))
                    {
                        component.setHasInvalidAssignee(true);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    public Component getComponentByName(final String name)
    {
        final List<Component> components = getComponents();

        for (Component component : components)
        {
            if (component.getName().equals(name)) {
                return component;
            }
        }

        return null;
    }

    private Component.ComponentAssigneeType getAssigneType(PageElement typeElement)
    {
        if (typeElement.hasClass("project-config-component-project-default"))
        {
            return Component.ComponentAssigneeType.PROJECT_DEFAULT;
        }
        else if (typeElement.hasClass("project-config-component-component-lead"))
        {
            return Component.ComponentAssigneeType.COMPONENT_LEAD;
        }
        else if (typeElement.hasClass("project-config-component-project-lead"))
        {
            return Component.ComponentAssigneeType.PROJECT_LEAD;
        }
        else if (typeElement.hasClass("project-config-component-unassigned"))
        {
            return Component.ComponentAssigneeType.UNASSIGNED;
        }
        else if (typeElement.hasClass("project-config-component-unknown"))
        {
            return Component.ComponentAssigneeType.UNKNOWN;
        }
        else
        {
            assertNotNull("Page has invalid assignee type: " + typeElement.getText());
            return Component.ComponentAssigneeType.UNKNOWN;
        }
    }

    public EditComponentForm getCreateComponentForm()
    {
        return createComponentForm;
    }


    public boolean hasEmptyMessage()
    {
        return table.find(By.cssSelector(".aui-restfultable-no-entires")).isPresent();
    }

    @Override
    public String getUrl()
    {
        return String.format("/plugins/servlet/project-config/%s/components", projectKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return table.timed().isPresent();
    }
}
