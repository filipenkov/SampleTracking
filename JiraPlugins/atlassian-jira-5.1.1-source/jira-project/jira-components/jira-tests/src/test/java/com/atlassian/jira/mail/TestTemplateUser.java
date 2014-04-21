package com.atlassian.jira.mail;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v4.0
 */
public class TestTemplateUser extends ListeningTestCase
{
    @Test
    public void testUser()
    {
        User user = new MockUser("fredf", "Fred Flintstone", "fred@bedrock.com");
        TemplateUser templateUser = TemplateUser.getUser(user);

        assertEquals("fredf", templateUser.getName());
        //noinspection deprecation
        assertEquals("Fred Flintstone", templateUser.getFullName());
        assertEquals("Fred Flintstone", templateUser.getDisplayName());
        assertEquals("fred@bedrock.com", templateUser.getEmailAddress());
        //noinspection deprecation
        assertEquals("fred@bedrock.com", templateUser.getEmail());
        assertEquals("fredf", templateUser.toString());
    }

    @Test
    public void testNullUser()
    {
        TemplateUser templateUser = TemplateUser.getUser(null);

        assertEquals("Anonymous", templateUser.getName());
        //noinspection deprecation
        assertEquals("Anonymous", templateUser.getFullName());
        assertEquals("Anonymous", templateUser.getDisplayName());
        assertEquals("", templateUser.getEmailAddress());
        //noinspection deprecation
        assertEquals("", templateUser.getEmail());
        assertEquals("Anonymous", templateUser.toString());
    }
}
