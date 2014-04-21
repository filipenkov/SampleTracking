package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.studio.MockStudioHooks;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.ComponentLocator;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestFullNameUserFormat extends MockControllerTestCase
{
    private ComponentLocator componentLocator;

    @Before
    public void setUp() throws Exception
    {
        componentLocator = createMock(ComponentLocator.class);
        replay();
    }

    @Test
    public void testUserNameIsBlank()
    {
        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(null);

        assertNull(userFormatterUtil.format(null, "someid"));
        assertNull(userFormatterUtil.format("", "someid"));
        assertNull(userFormatterUtil.format("    ", "someid"));
    }

    @Test
    public void testRegularCase()
    {
        final User dude = new MockUser("dude", "Mr Mock", null);

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();

        mockUserUtil.getUserObject("dude");
        mockUserUtilControl.setReturnValue(dude);
        mockUserUtil.getDisplayableNameSafely(dude);
        mockUserUtilControl.setReturnValue("Mr Mock");
        mockUserUtilControl.replay();

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(mockUserUtil);

        assertEquals("Mr Mock", userFormatterUtil.format("dude", "fullname"));
        mockUserUtilControl.verify();
    }

    @Test
    public void testHTMLEncoding()
    {
        final String fullName = "Mr Mock<script>alert('owned')</script>";
        final User dude = new MockUser("dude", fullName, null);

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();

        mockUserUtil.getUserObject("dude");
        mockUserUtilControl.setReturnValue(dude);
        mockUserUtil.getDisplayableNameSafely(dude);
        mockUserUtilControl.setReturnValue(fullName);
        mockUserUtilControl.replay();

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(mockUserUtil);

        assertEquals("Mr Mock&lt;script&gt;alert(&#39;owned&#39;)&lt;/script&gt;", userFormatterUtil.format("dude", "fullname"));
        mockUserUtilControl.verify();
    }

    @Test
    public void testUserNameDoesntExist()
    {
        final UserUtil userUtil = new UserUtilImpl(componentLocator, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, new MockStudioHooks())
        {
            public User getUserObject(final String userName)
            {
                return null;
            }
        };

        final FullNameUserFormat userFormatterUtil = new FullNameUserFormat(userUtil);

        assertEquals("TheIncredibleHulk", userFormatterUtil.format("TheIncredibleHulk", "someid"));
    }
}
