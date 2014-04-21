package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockUser;
import org.junit.Test;
import com.atlassian.jira.user.util.UserUtil;

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

        admins.add(new MockUser("luser"));
        sysAdmins.add(new MockUser("root"));
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
