package com.atlassian.sal.jira.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.sal.api.user.UserProfile;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.Assert;
import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;

/**
 */
public class TestDefaultUserManager extends TestCase
{
    public void testGetRemoteUsername()
    {
        User mockUser = new UserTemplate("tommy");

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        final JiraAuthenticationContext mockJiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(null);
        expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(mockUser);

        replay(mockJiraAuthenticationContext, mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, mockJiraAuthenticationContext, mockCrowdService, null, null);
        String username = defaultUserManager.getRemoteUsername();
        assertNull(username);

        username = defaultUserManager.getRemoteUsername();
        assertEquals("tommy", username);

        verify(mockJiraAuthenticationContext, mockCrowdService);
    }

    public void testIsSystemAdminNoUser()
    {
        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(null);
        replay(mockCrowdService);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(mockCrowdService);
    }

    public void testIsSystemAdminNoPermissions()
    {
        final User mockUser = new UserTemplate("tommy");

        final GlobalPermissionManager mockGlobalPermissionManager = createMock(GlobalPermissionManager.class);
        expect(mockGlobalPermissionManager.hasPermission(44, mockUser)).andReturn(false);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);
        replay(mockCrowdService, mockGlobalPermissionManager);

        DefaultUserManager defaultUserManager = new DefaultUserManager(mockGlobalPermissionManager, null, mockCrowdService, null, null);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(mockCrowdService, mockGlobalPermissionManager);
    }

    public void testIsSystemAdmin()
    {
        final User mockUser = new UserTemplate("tommy");

        final GlobalPermissionManager mockGlobalPermissionManager = createMock(GlobalPermissionManager.class);
        expect(mockGlobalPermissionManager.hasPermission(44, mockUser)).andReturn(true);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);
        replay(mockCrowdService, mockGlobalPermissionManager);

        DefaultUserManager defaultUserManager = new DefaultUserManager(mockGlobalPermissionManager, null, mockCrowdService, null, null)
        {
            //package level protected for testing
            User getUser(String username)
            {
                Assert.assertEquals("tommy", username);
                return mockUser;
            }
        };

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertTrue(systemAdmin);

        verify(mockCrowdService, mockGlobalPermissionManager);
    }

    public void testGetRemoteUserRequest()
    {
        final User mockUser = new UserTemplate("tommy");

        JiraAuthenticationContext jiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(jiraAuthenticationContext.getLoggedInUser()).andReturn(mockUser);

        final HttpServletRequest mockHttpServletRequest = createMock(HttpServletRequest.class);
        replay(jiraAuthenticationContext, mockHttpServletRequest);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, jiraAuthenticationContext, null, null, null);
        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertEquals("tommy", remoteUsername);

        verify(mockHttpServletRequest, jiraAuthenticationContext);
    }

    public void testGetRemoteUserRequestNoUser()
    {
        JiraAuthenticationContext jiraAuthenticationContext = createMock(JiraAuthenticationContext.class);
        expect(jiraAuthenticationContext.getLoggedInUser()).andReturn(null);

        final HttpServletRequest mockHttpServletRequest = createMock(HttpServletRequest.class);
        replay(jiraAuthenticationContext, mockHttpServletRequest);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, jiraAuthenticationContext, null, null, null);
        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertNull(remoteUsername);

        verify(mockHttpServletRequest, jiraAuthenticationContext);
    }

    public void testUserProfile_userdoesntexist() throws Exception
    {
        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(null);

        replay(mockCrowdService);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");
        assertNull(profile);
    }

    public void testUserProfile() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");
        mockUser.setEmailAddress("tommy@example.com");
        mockUser.setDisplayName("Tommy Golightly");

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);

        replay(mockCrowdService);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");
        assertEquals("tommy@example.com", profile.getEmail());
        assertEquals("tommy", profile.getUsername());
        assertEquals("Tommy Golightly", profile.getFullName());
        assertEquals("/secure/ViewProfile.jspa?name=tommy", profile.getProfilePageUri().toString());
    }

    public void testUserProfile_crazyName() throws Exception
    {
        final String username = "=?&!; #";
        final UserTemplate mockUser = new UserTemplate(username);
        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser(username)).andReturn(mockUser);

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        final UserProfile profile = defaultUserManager.getUserProfile(username);
        assertEquals(username, profile.getUsername());
        assertEquals("/secure/ViewProfile.jspa?name=%3D%3F%26%21%3B+%23", profile.getProfilePageUri().toString());
    }

    public void testUserProfile_avatarsDisabled() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);

        final AvatarManager avatarManager = createMock(AvatarManager.class);
        expect(avatarManager.isUserAvatarsEnabled()).andReturn(false);
        replay(mockCrowdService, avatarManager);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, avatarManager, null);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        assertNull(profile.getProfilePictureUri());

    }

    public void testUserProfile_noAvatar() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");

        final UserPropertyManager userPropertyManager = createMock(UserPropertyManager.class);
        final PropertySet propertySet = createMock(PropertySet.class);
        expect(propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY)).andReturn(false);
        expect(userPropertyManager.getPropertySet(mockUser)).andReturn(propertySet);

        final AvatarManager avatarManager = createMock(AvatarManager.class);
        expect(avatarManager.isUserAvatarsEnabled()).andReturn(true);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);

        replay(mockCrowdService, avatarManager, propertySet, userPropertyManager);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, avatarManager, userPropertyManager);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertNull(picture);
    }

    public void testUserProfile_avatar() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");

        final PropertySet propertySet = createMock(PropertySet.class);
        expect(propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY)).andReturn(true);
        expect(propertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY)).andReturn(2000L);

        final UserPropertyManager userPropertyManager = createMock(UserPropertyManager.class);
        expect(userPropertyManager.getPropertySet(mockUser)).andReturn(propertySet);

        final Avatar avatar = createMock(Avatar.class);
        expect(avatar.getId()).andReturn(2000L);

        final AvatarManager avatarManager = createMock(AvatarManager.class);
        expect(avatarManager.isUserAvatarsEnabled()).andReturn(true);
        expect(avatarManager.getById(2000L)).andReturn(avatar);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("tommy")).andReturn(mockUser);

        replay(mockCrowdService, avatarManager, propertySet, userPropertyManager, avatar);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, avatarManager, userPropertyManager);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertEquals("/secure/useravatar?avatarId=2000", picture.toString());
    }

    public void testAuthenticate_goodUser()
            throws FailedAuthenticationException
    {
        final UserTemplate mockUser = new UserTemplate("user1");

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.authenticate("user1", "password1")).andReturn(mockUser);

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        assertTrue(defaultUserManager.authenticate("user1", "password1"));

        verify(mockCrowdService);
    }

    public void testAuthenticate_nonExistingUser()
            throws FailedAuthenticationException
    {
        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.authenticate("user1", "password1")).andThrow(new FailedAuthenticationException("username/password is incorrect"));

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        assertFalse(defaultUserManager.authenticate("user1", "password1"));

        verify(mockCrowdService);
    }

    public void testIsUserInGroup_goodUser()
    {
        final User mockUser = createMock(User.class);
        final Group mockGroup = createMock(Group.class);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("user1")).andReturn(mockUser);
        expect(mockCrowdService.getGroup("group1")).andReturn(mockGroup);
        expect(mockCrowdService.isUserMemberOfGroup(eq(mockUser), eq(mockGroup))).andReturn(true);

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        assertTrue(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService);
    }

    public void testIsUserInGroup_nonExistingUser()
    {
        final Group mockGroup = createMock(Group.class);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("user1")).andReturn(null);
        expect(mockCrowdService.getGroup("group1")).andReturn(mockGroup);

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService);
    }

    public void testIsUserInGroup_nonExistingGroup()
    {
        final User mockUser = createMock(User.class);

        final CrowdService mockCrowdService = createMock(CrowdService.class);
        expect(mockCrowdService.getUser("user1")).andReturn(mockUser);
        expect(mockCrowdService.getGroup("group1")).andReturn(null);

        replay(mockCrowdService);
        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null, null);
        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService);
    }
}

