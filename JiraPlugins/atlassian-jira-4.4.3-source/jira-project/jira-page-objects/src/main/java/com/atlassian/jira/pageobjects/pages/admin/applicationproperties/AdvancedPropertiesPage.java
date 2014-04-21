package com.atlassian.jira.pageobjects.pages.admin.applicationproperties;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Advanced Configuration page object
 *
 * @since v4.4
 */
public class AdvancedPropertiesPage extends AbstractJiraPage
{
    @ElementBy(id = "application-properties-table")
    private PageElement advancedPropertiesTable;

    @Override
    public String getUrl()
    {
        return "/secure/admin/AdvancedApplicationProperties.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(
                advancedPropertiesTable.timed().isPresent(),
                Conditions.not(isTableLoading())
        );
    }

    public TimedQuery<Boolean> isTableLoading()
    {
        return advancedPropertiesTable.timed().hasClass("loading");
    }

    public List<AdvancedApplicationProperty> getApplicationProperties()
    {
        final List<AdvancedApplicationProperty> applicationProperties = Lists.newArrayList();

        final List<PageElement> rows = advancedPropertiesTable.findAll(By.cssSelector(".jira-restfultable-row"));
        for (final PageElement row : rows)
        {
            applicationProperties.add(createApplicationPropertyFromRow(By.cssSelector("tr[data-row-key='" + row.getAttribute("data-row-key") + "']")));
        }

        return applicationProperties;
    }

    private AdvancedApplicationProperty createApplicationPropertyFromRow(final By locator)
    {
        return pageBinder.bind(AdvancedApplicationPropertyImpl.class, locator);
    }

    public AdvancedApplicationProperty getProperty(final String propertyKey)
    {
        return createApplicationPropertyFromRow(By.cssSelector("tr[data-row-key='" + propertyKey + "']"));
    }

}
