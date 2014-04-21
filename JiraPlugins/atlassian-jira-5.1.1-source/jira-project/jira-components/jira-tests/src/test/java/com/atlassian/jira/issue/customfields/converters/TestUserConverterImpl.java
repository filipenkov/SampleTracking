package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestUserConverterImpl extends MockControllerTestCase
{
    @Test
    public void testGetString() throws Exception
    {
        UserConverterImpl userConverter = new UserConverterImpl(null);
        assertEquals("tom", userConverter.getString(new MockUser("tom")));
        assertEquals("", userConverter.getString(null));
    }

    @Test
    public void testGetUser() throws Exception
    {
        UserManager mockUserManager = getMock(UserManager.class);
        expect(mockUserManager.getUser("tom")).andReturn(new MockUser("tom"));
        replay();

        UserConverterImpl userConverter = new UserConverterImpl(mockUserManager);
        assertNull(userConverter.getUser("")); 
        assertNull(userConverter.getUser(" "));
        assertNull(userConverter.getUser(null));
        assertEquals("tom", userConverter.getUser("tom").getName());
    }
}
