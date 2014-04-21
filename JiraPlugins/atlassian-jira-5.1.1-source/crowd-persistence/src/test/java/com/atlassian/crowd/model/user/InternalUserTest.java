package com.atlassian.crowd.model.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static org.junit.Assert.assertEquals;

public class InternalUserTest
{
    private static final String SHORT_STRING = StringUtils.repeat("X", 255);
    private static final String LONG_STRING = StringUtils.repeat("X", 300);
    private static final String TRUNCATED_STRING = StringUtils.repeat("X", 252).concat("...");

    private static final Directory DIRECTORY = new DirectoryImpl(new InternalEntityTemplate(-1L, "directory", true, null, null));
    private static final PasswordCredential CREDENTIAL = new PasswordCredential("", true);

    @Test
    public void testInternalUser_ShortFields()
    {
        final UserTemplate userTemplate = new UserTemplate("user", SHORT_STRING, SHORT_STRING, SHORT_STRING);
        userTemplate.setEmailAddress(SHORT_STRING);
        final InternalUser user = new InternalUser(userTemplate, DIRECTORY, CREDENTIAL);

        assertEquals(SHORT_STRING, user.getFirstName());
        assertEquals(SHORT_STRING, user.getLastName());
        assertEquals(SHORT_STRING, user.getDisplayName());
        assertEquals(SHORT_STRING, user.getEmailAddress());

        assertEquals(toLowerCase(SHORT_STRING), user.getLowerFirstName());
        assertEquals(toLowerCase(SHORT_STRING), user.getLowerLastName());
        assertEquals(toLowerCase(SHORT_STRING), user.getLowerDisplayName());
        assertEquals(toLowerCase(SHORT_STRING), user.getLowerEmailAddress());
    }

    @Test
    public void testInternalUser_LongFields()
    {
        final UserTemplate userTemplate = new UserTemplate("user", LONG_STRING, LONG_STRING, LONG_STRING);
        userTemplate.setEmailAddress(LONG_STRING);
        final InternalUser user = new InternalUser(userTemplate, DIRECTORY, CREDENTIAL);

        assertEquals(TRUNCATED_STRING, user.getFirstName());
        assertEquals(TRUNCATED_STRING, user.getLastName());
        assertEquals(TRUNCATED_STRING, user.getDisplayName());
        assertEquals(TRUNCATED_STRING, user.getEmailAddress());

        assertEquals(toLowerCase(TRUNCATED_STRING), user.getLowerFirstName());
        assertEquals(toLowerCase(TRUNCATED_STRING), user.getLowerLastName());
        assertEquals(toLowerCase(TRUNCATED_STRING), user.getLowerDisplayName());
        assertEquals(toLowerCase(TRUNCATED_STRING), user.getLowerEmailAddress());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInternalUser_LongName()
    {
        final UserTemplate userTemplate = new UserTemplate(LONG_STRING);
        new InternalUser(userTemplate, DIRECTORY, CREDENTIAL);
    }
}
