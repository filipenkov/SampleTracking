package com.atlassian.jira.webtest.framework.impl.selenium.page.admin;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.Locators;
import com.atlassian.jira.webtest.framework.core.locator.mapper.LocatorDataBean;
import com.atlassian.jira.webtest.framework.model.admin.GeneralConfigurationProperty;

import java.util.Map;

/**
 * Mappings to locate general properties.
 *
 * @since v4.3
 */
final class GeneralPropertyMappings
{
    private GeneralPropertyMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static Map<GeneralConfigurationProperty, LocatorData> EDIT_LOCATOR_MAPPINGS = MapBuilder.<GeneralConfigurationProperty, LocatorData>newBuilder()
            .toMap();

    
    public static LocatorData editLocator(GeneralConfigurationProperty property)
    {
        LocatorData loc = EDIT_LOCATOR_MAPPINGS.get(property);
        if (loc == null)
        {
            throw new IllegalArgumentException("need to add locator for:" + property);
        }
        return loc;
    }
}
