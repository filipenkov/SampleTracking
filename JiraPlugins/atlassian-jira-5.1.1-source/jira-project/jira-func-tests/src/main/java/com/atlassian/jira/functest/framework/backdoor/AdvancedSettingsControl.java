package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Advanced Configuration.
 *
 * @since v5.0.3
 */
public class AdvancedSettingsControl extends BackdoorControl<AdvancedSettingsControl>
{
    public AdvancedSettingsControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void setTextFieldCharacterLengthLimit(long limit)
    {
        post(createResource().path("applicationProperties/text/set"), new KeyValueHolder("jira.text.field.character.limit", String.valueOf(limit)), String.class);
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