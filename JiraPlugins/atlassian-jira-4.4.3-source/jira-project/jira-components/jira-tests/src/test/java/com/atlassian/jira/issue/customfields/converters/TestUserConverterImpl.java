package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;

import com.opensymphony.user.User;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.user.util.UserUtil;

public class TestUserConverterImpl extends MockControllerTestCase
{
    @Test
    public void testGetString() throws Exception
    {
        UserConverterImpl userConverter = new UserConverterImpl(null);
        assertEquals("tom", userConverter.getString(new User("tom", new MockProviderAccessor(), new MockCrowdService())));
        assertEquals("", userConverter.getString(null));
    }

    @Test
    public void testGetUser() throws Exception
    {
        UserUtil mockUserUtil = getMock(UserUtil.class);
        expect(mockUserUtil.getUser("tom")).andReturn(new User("tom", new MockProviderAccessor(), new MockCrowdService()));
        replay();

        UserConverterImpl userConverter = new UserConverterImpl(mockUserUtil);
        assertNull(userConverter.getUser("")); 
        assertNull(userConverter.getUser(" "));
        assertNull(userConverter.getUser(null));
        assertEquals("tom", userConverter.getUser("tom").getName());
    }
}
