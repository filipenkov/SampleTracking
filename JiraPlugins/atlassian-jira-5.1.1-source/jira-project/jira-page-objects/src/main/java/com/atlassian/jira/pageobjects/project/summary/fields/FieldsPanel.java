package com.atlassian.jira.pageobjects.project.summary.fields;

import com.atlassian.jira.pageobjects.project.summary.AbstractSummaryPanel;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the screens panel in the project configuration page.
 *
 * @since v4.4
 */
public class FieldsPanel extends AbstractSummaryPanel
{

    @ElementBy (id = "project-config-webpanel-summary-fields")
    private PageElement fieldsSummaryPanel;

    public List<FieldConfigListItem> fieldConfigs()
    {
        final List<FieldConfigListItem> fieldConfigListItems = Lists.newArrayList();
        final List<PageElement> fieldConfigElements = fieldsSummaryPanel.findAll(ByJquery.$(".project-config-list > li"));

        for (final PageElement fieldConfigsElement : fieldConfigElements)
        {
            final PageElement fieldConfigLabel = fieldConfigsElement.find(ByJquery.$(".project-config-list-label"));
            final PageElement fieldConfigLink = fieldConfigLabel.find(By.tagName("a"));

            final String fieldConfigName = fieldConfigLabel.find(By.cssSelector(".project-config-fieldconfig-name")).getText();
            final String fieldConfigUrl = (fieldConfigLink.isPresent()) ?
                    fieldConfigLink.getAttribute("href") : null;
            final boolean isDefaultFieldConfig = fieldConfigsElement.find(By.className("project-config-list-default")).isPresent();

            fieldConfigListItems.add(new FieldConfigListItem(fieldConfigName, fieldConfigUrl, isDefaultFieldConfig));
        }

        return fieldConfigListItems;
    }

    public String fieldConfigSchemeEditLinkText()
    {
        return fieldsSummaryPanel.find(getSchemeLinkLocator()).getText();
    }

    public String fieldConfigSchemeEditLinkUrl()
    {
        return fieldsSummaryPanel.find(getSchemeLinkLocator()).getAttribute("href");
    }

    private By getSchemeLinkLocator()
    {
        return By.cssSelector(".project-config-summary-scheme > a");
    }

    public static class FieldConfigListItem
    {
        private final String name;
        private final String fieldConfigUrl;
        private final boolean defaultFieldConfig;

        public FieldConfigListItem(final String name, final String fieldConfigUrl, final boolean defaultFieldConfig)
        {
            this.name = name;
            this.fieldConfigUrl = fieldConfigUrl;
            this.defaultFieldConfig = defaultFieldConfig;
        }

        public String getName()
        {
            return name;
        }

        public String getFieldConfigUrl()
        {
            return fieldConfigUrl;
        }

        public boolean isDefaultFieldConfig()
        {
            return defaultFieldConfig;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            FieldConfigListItem that = (FieldConfigListItem) o;

            if (defaultFieldConfig != that.defaultFieldConfig) { return false; }
            if (fieldConfigUrl != null ? !fieldConfigUrl.equals(that.fieldConfigUrl) : that.fieldConfigUrl != null)
            { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (fieldConfigUrl != null ? fieldConfigUrl.hashCode() : 0);
            result = 31 * result + (defaultFieldConfig ? 1 : 0);
            return result;
        }
    }
}
