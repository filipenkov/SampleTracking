package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.impl.selenium.page.admin.SeleniumEditGeneralConfiguration;
import com.atlassian.jira.webtest.framework.model.admin.GeneralConfigurationProperty;
import com.atlassian.jira.webtest.framework.page.SubmittableChildPage;

/**
 * Represents the edit mode of the 'Global configuration' page.
 *
 * @since v4.3
 */
public interface EditGeneralConfiguration extends SubmittableChildPage<ViewGeneralConfiguration>
{
    /**
     * Set global configuration property to <tt>newValue</tt>.
     *
     * @param property property to set
     * @param newValue new property value
     * @return this configuration page instance
     */
    EditGeneralConfiguration setPropertyValue(GeneralConfigurationProperty property, String newValue);

    /**
     * Sets the mode to either "Private" or "Public"
     * @param mode
     * @return
     */
    SeleniumEditGeneralConfiguration setMode(String mode);

    /**
     * Sets the external user management option.
     * @param enable indicates whether to enable or disable external user management.
     * @return this configuration page instance.
     */
    SeleniumEditGeneralConfiguration setExternalUserManagement(boolean enable);
}
