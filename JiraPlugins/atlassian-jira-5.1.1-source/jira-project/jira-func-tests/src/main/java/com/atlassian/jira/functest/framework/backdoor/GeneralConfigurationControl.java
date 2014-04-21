package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.model.JiraMode;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate General Configuration.
 *
 * @since v5.1
 */
public class GeneralConfigurationControl extends BackdoorControl<GeneralConfigurationControl>
{
    public GeneralConfigurationControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public GeneralConfigurationControl allowUnassignedIssues()
    {
        setAllowUnassignedIssues(true);
        return this;
    }

    public GeneralConfigurationControl disallowUnassignedIssues()
    {
        setAllowUnassignedIssues(false);
        return this;
    }

    public GeneralConfigurationControl setContactAdminFormOn()
    {
        toggleContactAdminForm(true);
        return this;
    }

    public GeneralConfigurationControl setContactAdminFormOff()
    {
        toggleContactAdminForm(false);
        return this;
    }

    public GeneralConfigurationControl setCaptchaOnSignupOn()
    {
        toggleCaptchaOnSignup(true);
        return this;
    }

    public GeneralConfigurationControl setCaptchaOnSignupOff()
    {
        toggleCaptchaOnSignup(false);
        return this;
    }

    public GeneralConfigurationControl setJiraMode(JiraMode mode)
    {
        post(createResource().path("applicationProperties/string/set"), new KeyValueHolder("jira.mode", mode.optionValue()));
        return this;
    }



    public void toggleContactAdminForm(boolean isOn)
    {
        get(createResource().path("applicationProperties/option/set")
                .queryParam("key", "jira.show.contact.administrators.form")
                .queryParam("value", Boolean.toString(isOn)));
    }

    public void toggleCaptchaOnSignup(boolean isOn)
    {
        get(createResource().path("applicationProperties/option/set")
                .queryParam("key", "jira.option.captcha.on.signup")
                .queryParam("value", Boolean.toString(isOn)));
    }

    private void setAllowUnassignedIssues(boolean flag)
    {
        get(createResource().path("applicationProperties/option/set")
                .queryParam("key", "jira.option.allowunassigned")
                .queryParam("value", Boolean.toString(flag)));
    }

    private static class KeyValueHolder
    {
        KeyValueHolder(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }
}
