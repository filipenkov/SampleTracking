package com.atlassian.jira.web.action;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.user.User;
import mock.user.MockOSUser;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unit tests for Administrators.
 *
 * @since v4.2
 */
public class TestAdministrators extends ListeningTestCase
{
    private final Collection<User> admins = new ArrayList<User>();
    private final Collection<User> sysAdmins = new ArrayList<User>();

    public TestAdministrators()
    {

        admins.add(new MockOSUser("luser"));
        sysAdmins.add(new MockOSUser("root"));
    }

    @Test
    public void testGetAdministrators() throws Exception
    {
        UserUtil userUtil = createNiceMock(UserUtil.class);
        expect(userUtil.getAdministrators()).andReturn(admins);
        expect(userUtil.getSystemAdministrators()).andReturn(sysAdmins);
        replay(userUtil);
    }

    @Test
    public void testGetSystemAdministrators() throws Exception
    {
        UserUtil userUtil = createNiceMock(UserUtil.class);
        expect(userUtil.getAdministrators()).andReturn(admins);
        expect(userUtil.getSystemAdministrators()).andReturn(sysAdmins);
        replay(userUtil);
    }
}
