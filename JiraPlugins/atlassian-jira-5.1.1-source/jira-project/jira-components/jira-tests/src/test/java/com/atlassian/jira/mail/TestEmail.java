package com.atlassian.jira.mail;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;

/**
 * Tests the expansive {@link com.atlassian.jira.mail.Email} class.
 *
 * @since v3.13
 */
public class TestEmail extends ListeningTestCase
{

    @Test
    public void testHeaders() {
        final MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_EXCLUDE_PRECEDENCE_EMAIL_HEADER, true);
        Email.ConfigurationDependencies mockDeps = new Email.ConfigurationDependencies() {
            public ApplicationProperties getApplicationProperties()
            {
                return mockApplicationProperties;
            }

            public JiraApplicationContext getJiraApplicationContext()
            {
                return new JiraApplicationContext(){
                    public String getFingerPrint()
                    {
                        return "fingerlicker";
                    }
                };
            }
        };
        Email email = new Email("chris@example.com", mockDeps);
        Map headers = email.getHeaders();
        String fingerPrint = (String) headers.get(Email.HEADER_JIRA_FINGER_PRINT);
        assertEquals("fingerlicker", fingerPrint);
        assertNull(headers.get("Precedence"));
    }

}
