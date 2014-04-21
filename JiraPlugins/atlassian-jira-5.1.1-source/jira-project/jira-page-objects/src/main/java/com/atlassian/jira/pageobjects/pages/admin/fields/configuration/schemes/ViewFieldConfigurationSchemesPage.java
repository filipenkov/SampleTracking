package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.ConfigureFieldConfigurationSchemePage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openqa.selenium.By;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.junit.Assert.assertTrue;

/**
 * Represents the &quot;view field configurations page&quot; available from within the JIRA Administration UI.
 *
 * @since v5.0.1
 */
public class ViewFieldConfigurationSchemesPage extends AbstractJiraPage
{
    private static final String CONFIGURE_LINK_CSS_SELECTOR =
            "tbody tr[data-field-configuration-scheme-name='%s'] .operations-list [data-operation=configure]";

    @ElementBy(id="add-field-configuration-scheme")
    private PageElement addFieldConfigurationSchemeButton;

    @ElementBy(id = "field-configuration-schemes-table")
    private PageElement fieldConfigurationSchemesTable;

    @Override
    public TimedCondition isAt()
    {
        return addFieldConfigurationSchemeButton.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewFieldLayoutSchemes.jspa";
    }

    public AddFieldConfigurationSchemeDialog openAddFieldConfigurationSchemeDialog()
    {
        addFieldConfigurationSchemeButton.click();
        return pageBinder.bind(AddFieldConfigurationSchemeDialog.class);
    }

    public ConfigureFieldConfigurationSchemePage configure(final String fieldConfigurationSchemeName)
    {
        final PageElement configureLink = fieldConfigurationSchemesTable.find
                (
                        By.cssSelector(format(CONFIGURE_LINK_CSS_SELECTOR, fieldConfigurationSchemeName))
                );
        assertTrue("Attempted to configure a field configuration scheme that is not present on the page", configureLink.isPresent());

        final String fieldConfigurationSchemeId = removeStart(configureLink.getAttribute("id"), "configure_");

        configureLink.click();
        return pageBinder.bind(ConfigureFieldConfigurationSchemePage.class, fieldConfigurationSchemeId);
    }

    public Iterable<FieldConfigurationSchemeItem> getFieldConfigurationSchemes()
    {
        final List<PageElement> fieldConfigurationSchemeRows =
                fieldConfigurationSchemesTable.findAll(By.cssSelector("tbody tr"));

        return copyOf(transform(fieldConfigurationSchemeRows, new Function<PageElement, FieldConfigurationSchemeItem>()
        {
            @Override
            public FieldConfigurationSchemeItem apply(final PageElement pageElement)
            {
                final String fieldConfigName = pageElement.
                        find(By.cssSelector("[data-scheme-field=name]")).getText();

                String fieldDescriptionName = "";
                if (pageElement.find(By.className("description")).isPresent())
                {
                    fieldDescriptionName = pageElement.find(By.className("description")).getText();
                }
                return new FieldConfigurationSchemeItem(fieldConfigName, fieldDescriptionName);
            }
        }));
    }

    /**
     * Represents the data for a field configuration in the {@link ViewFieldConfigurationSchemesPage}
     *
     * @since 5.0.1
     */
    public static class FieldConfigurationSchemeItem
    {
        private final String name;

        private final String description;

        public FieldConfigurationSchemeItem(final String name, final String description)
        {
            this.name = name;
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof FieldConfigurationSchemeItem)) { return false; }

            final FieldConfigurationSchemeItem rhs = (FieldConfigurationSchemeItem) obj;

            return new EqualsBuilder().
                    append(getName(), rhs.getName()).
                    append(getDescription(), rhs.getDescription()).
                    isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("description", description).
                    toString();
        }
    }
}
