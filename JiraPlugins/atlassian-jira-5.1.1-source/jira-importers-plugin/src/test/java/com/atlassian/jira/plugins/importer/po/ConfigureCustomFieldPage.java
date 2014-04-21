package com.atlassian.jira.plugins.importer.po;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

public class ConfigureCustomFieldPage extends AbstractJiraPage {

    @ElementBy(id = "add_new_context")
    private PageElement addNewContextLink;

    private final String id;

    public ConfigureCustomFieldPage(String id) {
        this.id = StringUtils.replace(id, "customfield_", "");
    }

    @Override
    public TimedCondition isAt() {
        return addNewContextLink.timed().isVisible();
    }

    public boolean isEditOptionsVisible() {
        return elementFinder.find(By.id(String.format("customfield_%s-edit-options", id))).isVisible();
    }

    @Override
    public String getUrl() {
        return "/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + id;
    }
}
