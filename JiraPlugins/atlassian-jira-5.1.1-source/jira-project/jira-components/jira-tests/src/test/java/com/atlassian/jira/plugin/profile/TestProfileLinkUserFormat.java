package com.atlassian.jira.plugin.profile;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.google.common.collect.Maps;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestProfileLinkUserFormat extends ListeningTestCase
{
    final String avatar10023Url = "http://localhost/jira/avatar_known";

    AvatarService mockAvatarService;
    JiraAuthenticationContext jiraAuthenticationContext;
    Avatar avatar10023;
    User loggedInUser;

    @Before
    public void setUp() throws Exception
    {
        loggedInUser = new MockUser("username");

        jiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(jiraAuthenticationContext.getLoggedInUser()).andReturn(loggedInUser);
        replay(jiraAuthenticationContext);

        avatar10023 = createMock(Avatar.class);
        expect(avatar10023.getId()).andReturn(10023L).anyTimes();
        replay(avatar10023);

        mockAvatarService(false);
    }

    @Test
    public void testGetHtmlNullUser()
    {
        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject(null);
        mockUserUtilControl.setReturnValue(null);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);

        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", null, "user", null, "fullname", null, "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("Anonymous");
        mockUserFormatModuleDescriptorControl.replay();


        final String html = userFormat.format(null, "testid");
        assertEquals("Anonymous", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
        verify(mockAvatarService);
    }

    @Test
    public void testGetHtmlUnknownUser()
    {
        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject("unknown");
        mockUserUtilControl.setReturnValue(null);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "unknown", "user", null, "fullname", "unknown", "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("unknown");
        mockUserFormatModuleDescriptorControl.replay();

        final String html = userFormat.format("unknown", "testid");
        assertEquals("unknown", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
        verify(mockAvatarService);
    }

    @Test
    public void testGetHtmlKnownUserWithFullName()
    {
        final User adminUser = new MockUser("admin", "Administrator", "admin@example.com");

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject("admin");
        mockUserUtilControl.setReturnValue(adminUser);
        mockUserUtil.getDisplayableNameSafely(adminUser);
        mockUserUtilControl.setReturnValue("Administrator");
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser, "fullname", "Administrator", "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        final String html = userFormat.format("admin", "testid");
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
        verify(mockAvatarService);
    }

    @Test
    public void testGetHtmlWithParams()
    {
        final User adminUser = new MockUser("admin", "Administrator", "admin@example.com");

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject("admin");
        mockUserUtilControl.setDefaultReturnValue(adminUser);
        mockUserUtil.getDisplayableNameSafely(adminUser);
        mockUserUtilControl.setReturnValue("Administrator");
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser, "fullname", "Administrator", "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        final String html = userFormat.format("admin", "testid", Maps.<String, Object>newHashMap());
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
        verify(mockAvatarService);
    }

    @Test
    public void testGetHtmlKnownUserWithNoFullName()
    {
        final User adminUser = new MockUser("admin", "", "admin@example.com");

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject("admin");
        mockUserUtilControl.setReturnValue(adminUser);
        mockUserUtil.getDisplayableNameSafely(adminUser);
        mockUserUtilControl.setReturnValue("admin");
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser, "fullname", "admin", "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        final String html = userFormat.format("admin", "testid");
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
        verify(mockAvatarService);
    }

    @Test
    public void testGetHtmlWithAvatar()
    {
        final User adminUser = new MockUser("admin", "Administrator", "admin@example.com");

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUserObject("admin");
        mockUserUtilControl.setReturnValue(adminUser);
        mockUserUtil.getDisplayableNameSafely(adminUser);
        mockUserUtilControl.setReturnValue("Administrator");
        mockUserUtilControl.replay();

        mockAvatarService(true);
        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        ProfileLinkUserFormat userFormat = new ProfileLinkUserFormat(mockUserFormatModuleDescriptor, mockUserUtil, mockAvatarService, jiraAuthenticationContext);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser,
                "fullname", "Administrator", "id", "testid", "avatarURL", avatar10023Url));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        final String html = userFormat.format("admin", "testid");
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
    }

    protected void mockAvatarService(boolean avatarsEnabled)
    {
        mockAvatarService = createMock(AvatarService.class);
        expect(mockAvatarService.getAvatar(any(User.class), any(String.class))).andReturn(avatar10023).anyTimes();
        expect(mockAvatarService.getAvatarURL(any(User.class), any(String.class), same(Avatar.Size.SMALL))).andReturn(URI.create(avatar10023Url)).anyTimes();
        replay(mockAvatarService);
    }
}
