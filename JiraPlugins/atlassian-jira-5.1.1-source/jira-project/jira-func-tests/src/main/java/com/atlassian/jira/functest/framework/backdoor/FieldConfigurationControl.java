package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Field Configurations.
 *
 * See FieldConfigurationBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0.1
 * @author mtokar
 */
public class FieldConfigurationControl extends BackdoorControl<FieldConfigurationControl>
{
    public FieldConfigurationControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Copies an existing field configuration. The new field configuration will have the name
     * <code>"Copy of " + original name</code> unless specified.
     *
     * @param name the name of the existing field configuration
     * @param copyName the name of the new field configuration
     */
    public void copyFieldConfiguration(String name, String copyName)
    {
        get(createResource().path("fieldConfiguration/copy")
                .queryParam("name", name)
                .queryParam("copyName", copyName)
        );
    }

}
