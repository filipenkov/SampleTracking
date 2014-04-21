package com.atlassian.sal.jira.user;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.user.UserProfile;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultUserManager extends TestCase
{
    public void testGetRemoteUsername()
    {
        User mockUser = new UserTemplate("tommy");

        final CrowdService mockCrowdService = mock(CrowdService.class);
        final JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getLoggedInUser()).thenReturn(null).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, mockJiraAuthenticationContext, mockCrowdService, null);
        String username = defaultUserManager.getRemoteUsername();
        assertNull(username);

        username = defaultUserManager.getRemoteUsername();
        assertEquals("tommy", username);

        verify(mockJiraAuthenticationContext, times(2)).getLoggedInUser();
    }

    public void testIsSystemAdminNoUser()
    {
        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(null);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(mockCrowdService).getUser("tommy");
    }

    public void testIsSystemAdminNoPermissions()
    {
        final User mockUser = new UserTemplate("tommy");

        final GlobalPermissionManager mockGlobalPermissionManager = mock(GlobalPermissionManager.class);
        when(mockGlobalPermissionManager.hasPermission(44, mockUser)).thenReturn(false);

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(mockGlobalPermissionManager, null, mockCrowdService, null);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertFalse(systemAdmin);

        verify(mockCrowdService).getUser("tommy");
        verify(mockGlobalPermissionManager).hasPermission(44, mockUser);
    }

    public void testIsSystemAdmin()
    {
        final User mockUser = new UserTemplate("tommy");

        final GlobalPermissionManager mockGlobalPermissionManager = mock(GlobalPermissionManager.class);
        when(mockGlobalPermissionManager.hasPermission(44, mockUser)).thenReturn(true);

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(mockGlobalPermissionManager, null, mockCrowdService, null);

        boolean systemAdmin = defaultUserManager.isSystemAdmin("tommy");
        assertTrue(systemAdmin);

        verify(mockCrowdService).getUser("tommy");
        verify(mockGlobalPermissionManager).hasPermission(44, mockUser);
    }

    public void testGetRemoteUserRequest()
    {
        final User mockUser = new UserTemplate("tommy");

        JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(mockUser);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, jiraAuthenticationContext, null, null);
        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertEquals("tommy", remoteUsername);

        verify(jiraAuthenticationContext).getLoggedInUser();
    }

    public void testGetRemoteUserRequestNoUser()
    {
        JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(null);

        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, jiraAuthenticationContext, null, null);
        final String remoteUsername = defaultUserManager.getRemoteUsername(mockHttpServletRequest);
        assertNull(remoteUsername);

        verify(jiraAuthenticationContext).getLoggedInUser();
    }

    public void testUserProfile_userdoesntexist() throws Exception
    {
        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(null);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");
        assertNull(profile);
    }

    public void testUserProfile() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");
        mockUser.setEmailAddress("tommy@example.com");
        mockUser.setDisplayName("Tommy Golightly");

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
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
        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser(username)).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        final UserProfile profile = defaultUserManager.getUserProfile(username);
        assertEquals(username, profile.getUsername());
        assertEquals("/secure/ViewProfile.jspa?name=%3D%3F%26%21%3B+%23", profile.getProfilePageUri().toString());
    }

    public void testUserProfile_noAvatar() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(mockUser);

        final User remoteUser = mock(User.class);

        JiraAuthenticationContext authenticationContext = mock(JiraAuthenticationContext.class);
        when(authenticationContext.getLoggedInUser()).thenReturn(remoteUser);

        AvatarService avatarService = mock(AvatarService.class);
        when(avatarService.getAvatarURL(remoteUser, "tommy", Avatar.Size.LARGE)).thenReturn(null);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, authenticationContext, mockCrowdService, avatarService);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertNull(picture);
    }

    public void testUserProfile_avatar() throws Exception
    {
        final UserTemplate mockUser = new UserTemplate("tommy");

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("tommy")).thenReturn(mockUser);

        JiraAuthenticationContext authenticationContext = mock(JiraAuthenticationContext.class);
        when(authenticationContext.getLoggedInUser()).thenReturn(mockUser);

        final URI avatarUri = new URI("http://example.invalid/secure/useravatar?avatarId=2000");

        AvatarService avatarService = mock(AvatarService.class);
        when(avatarService.getAvatarURL(mockUser, "tommy", Avatar.Size.LARGE)).thenReturn(avatarUri);
        when(avatarService.hasCustomUserAvatar(mockUser, "tommy")).thenReturn(false);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, authenticationContext, mockCrowdService, avatarService);
        final UserProfile profile = defaultUserManager.getUserProfile("tommy");

        final URI picture = profile.getProfilePictureUri();
        assertEquals(avatarUri, picture);
    }

    public void testUserProfile_avatarServiceGetsLoggedInUser()
    {
        final User avatarUser = new UserTemplate("tommy");

        final CrowdService crowdService = mock(CrowdService.class);
        when(crowdService.getUser("tommy")).thenReturn(avatarUser);

        final AvatarService avatarService = mock(AvatarService.class);

        final User remoteUser = mock(User.class);

        JiraAuthenticationContext authenticationContext = mock(JiraAuthenticationContext.class);
        when(authenticationContext.getLoggedInUser()).thenReturn(remoteUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, authenticationContext, crowdService, avatarService);

        defaultUserManager.getUserProfile("tommy").getProfilePictureUri(48, 48);
        verify(avatarService).getAvatarURL(remoteUser, "tommy", Avatar.Size.LARGE);
    }

    public void testAuthenticate_goodUser()
            throws FailedAuthenticationException
    {
        final UserTemplate mockUser = new UserTemplate("user1");

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.authenticate("user1", "password1")).thenReturn(mockUser);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        assertTrue(defaultUserManager.authenticate("user1", "password1"));

        verify(mockCrowdService).authenticate("user1", "password1");
    }

    public void testAuthenticate_nonExistingUser()
            throws FailedAuthenticationException
    {
        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.authenticate("user1", "password1")).thenThrow(new FailedAuthenticationException("username/password is incorrect"));

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        assertFalse(defaultUserManager.authenticate("user1", "password1"));

        verify(mockCrowdService).authenticate("user1", "password1");
    }

    public void testIsUserInGroup_goodUser()
    {
        final User mockUser = mock(User.class);
        final Group mockGroup = mock(Group.class);

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("user1")).thenReturn(mockUser);
        when(mockCrowdService.getGroup("group1")).thenReturn(mockGroup);
        when(mockCrowdService.isUserMemberOfGroup(mockUser, mockGroup)).thenReturn(true);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        assertTrue(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService).getUser("user1");
        verify(mockCrowdService).getGroup("group1");
        verify(mockCrowdService).isUserMemberOfGroup(mockUser, mockGroup);
    }

    public void testIsUserInGroup_nonExistingUser()
    {
        final Group mockGroup = mock(Group.class);

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("user1")).thenReturn(null);
        when(mockCrowdService.getGroup("group1")).thenReturn(mockGroup);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService).getUser("user1");
        verify(mockCrowdService).getGroup("group1");
    }

    public void testIsUserInGroup_nonExistingGroup()
    {
        final User mockUser = mock(User.class);

        final CrowdService mockCrowdService = mock(CrowdService.class);
        when(mockCrowdService.getUser("user1")).thenReturn(mockUser);
        when(mockCrowdService.getGroup("group1")).thenReturn(null);

        DefaultUserManager defaultUserManager = new DefaultUserManager(null, null, mockCrowdService, null);
        assertFalse(defaultUserManager.isUserInGroup("user1", "group1"));

        verify(mockCrowdService).getUser("user1");
        verify(mockCrowdService).getGroup("group1");
    }
}
