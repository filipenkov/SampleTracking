package com.atlassian.jira.plugins.importer.po;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import java.util.List;

public class ViewCustomFieldsPage extends AbstractJiraPage {
    @ElementBy(id="custom-fields")
    private PageElement customFields;

    @Override
    public TimedCondition isAt() {
        return customFields.timed().isVisible();
    }

    @Override
    public String getUrl() {
        return "/secure/admin/ViewCustomFields.jspa";
    }

    public List<CustomFieldItem> getCustomFields() {
        final List<PageElement> rows = customFields.find(By.tagName("tbody")).findAll(By.tagName("tr"));

        return Immutables.transformThenCopyToList(rows, new Function<PageElement, CustomFieldItem>() {
            @Override
            public CustomFieldItem apply(@Nullable PageElement input) {
                PageElement firstCol = input.find(By.tagName("td"));
                return new CustomFieldItem(firstCol.find(By.tagName("strong")).getText(),
                        firstCol.find(By.cssSelector("div.description")).getText(),
                        StringUtils.replace(StringUtils.replace(firstCol.getAttribute("id"), "custom-fields-", ""), "-name", ""));
            }
        });
    }

    public static class CustomFieldItem {
        public String name;
        public String description;
        public String id;

        public CustomFieldItem(String name, String description, String id) {
            this.name = name;
            this.description = description;
            this.id = id;
        }
    }
}
