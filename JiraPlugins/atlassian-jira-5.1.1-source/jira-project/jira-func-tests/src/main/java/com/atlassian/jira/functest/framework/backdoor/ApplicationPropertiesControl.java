package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate
 * ApplicationProperties.
 *
 * See ApplicationPropertiesBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class ApplicationPropertiesControl extends BackdoorControl<ApplicationPropertiesControl>
{
    public ApplicationPropertiesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public ApplicationPropertiesControl setOption(String key, boolean value)
    {
        get(createResource().path("applicationProperties/option/set")
                .queryParam("key", key)
                .queryParam("value", "" + value));
        return this;
    }

    public ApplicationPropertiesControl setText(String key, String value)
    {
        post(createResource().path("applicationProperties/text/set"), new KeyValueHolder(key, value), String.class);
        return this;
    }

    public ApplicationPropertiesControl setString(String key, String value)
    {
        post(createResource().path("applicationProperties/string/set"), new KeyValueHolder(key, value), String.class);
        return this;
    }

    public ApplicationPropertiesControl disableXsrfChecking()
    {
        return setOption("jira.xsrf.enabled", false);
    }

    public ApplicationPropertiesControl enableXsrfChecking()
    {
        return setOption("jira.xsrf.enabled", true);
    }

    private static class KeyValueHolder
    {
        public String key;
        public String value;

        public KeyValueHolder(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }
}
