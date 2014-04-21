package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.streams.spi.StreamsI18nResolver;
import com.atlassian.streams.testing.AbstractUserProfileAccessorTestSuite;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static com.atlassian.streams.api.common.Option.none;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraUserProfileAccessorTest extends AbstractUserProfileAccessorTestSuite
{
    @Mock EmailFormatter emailFormatter;
    @Mock JiraAuthenticationContext authenticationContext;
    @Mock UserManager userManager;
    @Mock AvatarService avatarService;
    @Mock AvatarManager avatarManager;
    @Mock StreamsI18nResolver i18nResolver;
    @Mock UserUtil userUtil;
    @Mock User user1;
    @Mock User user2;
    @Mock User user3;

    @Override
    public String getProfilePathTemplate()
    {
        return "/secure/ViewProfile.jspa?name={username}";
    }

    @Override
    protected String getProfilePicturePathTemplate()
    {
        return "/secure/useravatar?avatarId={profilePictureParameter}";
    }

    @Override
    protected String getProfilePicParameter(String username)
    {
        return "0";
    }

    @Before
    public void createUserProfileBuilder()
    {
        userProfileAccessor = new JiraUserProfileAccessor(
                userUtil, getApplicationProperties(), emailFormatter, authenticationContext, userManager,
                avatarService, avatarManager, i18nResolver);
    }

    @Before
    public void prepareUserUtil()
    {
        URI avatarUri = URI.create("http://localhost/streams/secure/useravatar?avatarId=0");
        when(avatarService.getAvatarURL(any(User.class), anyString(), any(Avatar.Size.class))).thenReturn(avatarUri);

        when(emailFormatter.emailVisible(any(User.class))).thenReturn(true);

        when(user1.getName()).thenReturn("user");
        when(user1.getDisplayName()).thenReturn("User");
        when(user1.getEmailAddress()).thenReturn("u@c.com");
        when(userUtil.getUserObject("user")).thenReturn(user1);
        when(userManager.getUserProfile("user")).thenReturn(mockUserProfile("user"));

        when(user2.getName()).thenReturn("user 2");
        when(user2.getDisplayName()).thenReturn("User 2");
        when(user2.getEmailAddress()).thenReturn("u2@c.com");
        when(userUtil.getUserObject("user 2")).thenReturn(user2);
        when(userManager.getUserProfile("user 2")).thenReturn(mockUserProfile("user 2"));

        when(user3.getName()).thenReturn("user3");
        when(user3.getDisplayName()).thenReturn("User <3&'>");
        when(user3.getEmailAddress()).thenReturn("u3@c.com");
        when(userUtil.getUserObject("user3")).thenReturn(user3);
        when(userManager.getUserProfile("user3")).thenReturn(mockUserProfile("user3"));
    }

    @Test
    public void assertThatEmailIsOnlyAvailableIfItIsVisibleToTheLoggedInUser()
    {
        when(emailFormatter.emailVisible(any(User.class))).thenReturn(false);
        assertThat(userProfileAccessor.getUserProfile("user").getEmail(), is(equalTo(none(String.class))));
    }

    @Test
    @Override
    @Ignore("SAL is doing this for us in JIRA, so there's no reason to test it ourselves")
    public void assertThatUsernameIsUriEncodedInProfileUri()
    {
    }

    @Test
    @Override
    @Ignore("SAL is doing this for us in JIRA, so there's no reason to test it ourselves")
    public void assertThatUsernameIsUriEncodedInProfilePictureUri()
    {
    }


    private UserProfile mockUserProfile(final String username)
    {
        return new UserProfile()
        {
            public String getUsername()
            {
                return username;
            }

            public String getFullName()
            {
                return null;
            }

            public String getEmail()
            {
                return null;
            }

            public URI getProfilePictureUri(int width, int height)
            {
                return getProfilePictureUri();
            }

            public URI getProfilePictureUri()
            {
                return URI.create("/secure/useravatar?avatarId=0");
            }

            public URI getProfilePageUri()
            {
                return URI.create("/secure/ViewProfile.jspa?name=" + username);
            }
        };
    }
}
