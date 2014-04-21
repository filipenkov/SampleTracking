package com.atlassian.jira.pageobjects.project.components;

import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.global.UserLink;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Simple representation of a component in JIRA.
 *
 * @since v4.4
 */
public class Component
{

    private static final String EDITABLE_SELECTOR = ".aui-restfultable-editable[data-field-name=%s]";

    @Inject
    private AtlassianWebDriver atlassianWebDriver;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    private PageElement row;

    public enum ComponentAssigneeType
    {
        COMPONENT_LEAD, PROJECT_DEFAULT, PROJECT_LEAD, UNASSIGNED, UNKNOWN
    }

    private ComponentAssigneeType componentAssigneeType;
    private String name;
    private String id;
    private String description;
    private Boolean hasInvalidLead;
    private Boolean hasInvalidAssignee;
    private User lead;
    private UserLink leadLink;

    public Component(final String id)
    {
        this.id = id;
    }

    @Init
    public void getRow()
    {
        row = elementFinder.find(By.id("component-" + id + "-row"));
    }

    public Boolean hasInvalidLead()
    {
        return hasInvalidLead;
    }

    public void setHasInvalidLead(Boolean hasInvalidLead)
    {
        this.hasInvalidLead = hasInvalidLead;
    }

    public Boolean hasInvalidAssignee()
    {
        return hasInvalidAssignee;
    }

    public void setHasInvalidAssignee(Boolean hasInvalidAssignee)
    {
        this.hasInvalidAssignee = hasInvalidAssignee;
    }

    public ComponentAssigneeType getComponentAssigneeType()
    {
        return componentAssigneeType;
    }

    public Component setComponentAssigneeType(ComponentAssigneeType componentAssigneeType)
    {
        this.componentAssigneeType = componentAssigneeType;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public Component setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Component setName(String name)
    {
        this.name = name;
        return this;
    }

    public User getLead()
    {
        return lead;
    }

    public Component setLead(User user)
    {
        this.lead = user;
        return this;
    }

    public Component setLeadLink(UserLink link)
    {
        this.leadLink = link;
        return this;
    }

    public UserLink getLeadLink()
    {
        return leadLink;
    }

    public EditComponentForm edit(final String name)
    {
        row.find(ByJquery.$(String.format(EDITABLE_SELECTOR, name))).click();
        return pageBinder.bind(EditComponentForm.class, By.id(row.getAttribute("id")));
    }

    public DeleteComponentDialog delete()
    {
        // so our only hover on visible link can be clickable
        atlassianWebDriver.executeScript("jQuery('#" + row.getAttribute("id") + "').addClass('aui-restfultable-active')");
        row.find(By.className("aui-resfultable-delete")).click();
        return pageBinder.bind(DeleteComponentDialog.class, id);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Component component = (Component) o;

        if (componentAssigneeType != component.componentAssigneeType) { return false; }
        if (description != null ? !description.equals(component.description) : component.description != null)
        {
            return false;
        }
        if (lead != null ? !lead.equals(component.lead) : component.lead != null) { return false; }
        if (leadLink != null ? !leadLink.equals(component.leadLink) : component.leadLink != null) { return false; }
        if (name != null ? !name.equals(component.name) : component.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = componentAssigneeType != null ? componentAssigneeType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (lead != null ? lead.hashCode() : 0);
        result = 31 * result + (leadLink != null ? leadLink.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
