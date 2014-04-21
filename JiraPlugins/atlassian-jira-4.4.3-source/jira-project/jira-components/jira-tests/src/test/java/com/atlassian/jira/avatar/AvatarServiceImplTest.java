package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.MockPropertySet;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for AvatarServiceImplTest.
 *
 * @since v4.3
 */
public class AvatarServiceImplTest extends MockControllerTestCase
{
    private User callingUser;

    @Test
    public void avatarsEnabledShouldReturnTrueWhenAvatarManagerReturnsTrue() throws Exception
    {
        fixture().prepare();

        assertTrue(avatarService().isUserAvatarsEnabled());
    }

    @Test
    public void avatarsEnabledShouldReturnFalseWhenAvatarManagerReturnsFalse() throws Exception
    {
        fixture()
                .withAvatarsDisabled()
                .prepare();

        assertFalse(avatarService().isUserAvatarsEnabled());
    }

    @Test (expected = AvatarsDisabledException.class)
    public void getAvatarShouldThrowExceptionIfAvatarsAreDisabled() throws Exception
    {
        fixture().withAvatarsDisabled()
                .prepare();

        avatarService().getAvatar(callingUser, "some_id");
    }

    @Test
    public void getAvatarShouldReturnAnonymousAvatarForUnknownUser() throws Exception
    {
        long anonAvatarId = 5L;
        Avatar anonAvatar = createMock(Avatar.class);
        String unknownUsername = "unknown_user";

        fixture().withAnonymousAvatarId(anonAvatarId)
                .withUser(unknownUsername, null)
                .withAvatar(anonAvatarId, anonAvatar)
                .prepare();

        Avatar avatar = avatarService().getAvatar(callingUser, unknownUsername);
        assertThat(avatar, equalTo(anonAvatar));
    }

    @Test
    public void getAvatarShouldReturnDefaultAvatarForUserWithNoConfiguredAvatar() throws Exception
    {
        long defaultAvatarId = 42L;
        String knownUsername = "known_user";
        User knownUser = createMock(User.class);
        Avatar defaultAvatar = createMock(Avatar.class);

        fixture().withDefaultUserAvatarId(defaultAvatarId)
                .withUser(knownUsername, knownUser)
                .withAvatar(defaultAvatarId, defaultAvatar)
                .prepare();

        Avatar avatar = avatarService().getAvatar(callingUser, knownUsername);
        assertThat(avatar, equalTo(defaultAvatar));
    }

    @Test (expected = AvatarsDisabledException.class)
    public void getAvatarUrlShouldThrowExceptionIfAvatarsAreDisabled() throws Exception
    {
        fixture().withAvatarsDisabled()
                .prepare();

        avatarService().getAvatarURL(new MockUser("usah"), "some_id", Avatar.Size.SMALL);
    }

    @Test
    public void avatarUrlShouldStartWithASlashWhenContextPathIsEmpty() throws Exception
    {
        final User knownUser = createMock(User.class);
        final String knownUsername = "known_user";

        fixture()
                .withContextPath("")
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService().getAvatarURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo("/secure/useravatar?size=small"));
    }

    @Test
    public void avatarUrlShouldStartWithASlashWhenContextPathIsNotEmpty() throws Exception
    {
        final User knownUser = createMock(User.class);
        final String knownUsername = "known_user";

        fixture()
                .withContextPath("/jira")
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService().getAvatarURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo("/jira/secure/useravatar?size=small"));
    }

    @Before
    public void createCallingUser() throws Exception
    {
        callingUser = new MockUser("caller");
    }

    protected AvatarServiceImpl avatarService()
    {
        return instantiate(AvatarServiceImpl.class);
    }

    protected FixturePreparer fixture()
    {
        return new FixturePreparer();
    }

    /**
     * Builder class for preparing the test fixture.
     */
    class FixturePreparer
    {
        private boolean avatarsEnabled = true;
        private Long anonymousAvatarId = null;
        private Long defaultUserAvatarId = null;
        private Map<Long, Avatar> avatars = Maps.newHashMap();
        private Map<String, User> users = Maps.newHashMap();
        private Map<User, PropertySet> userProperties = Maps.newHashMap();
        private String contextPath = "";

        FixturePreparer withAvatarsDisabled()
        {
            this.avatarsEnabled = false;
            return this;
        }

        public FixturePreparer withAnonymousAvatarId(Long anonymousAvatarId)
        {
            this.anonymousAvatarId = anonymousAvatarId;
            return this;
        }

        public FixturePreparer withDefaultUserAvatarId(Long defaultAvatarId)
        {
            this.defaultUserAvatarId = defaultAvatarId;
            return this;
        }

        public FixturePreparer withAvatar(long avatarId, Avatar avatar)
        {
            avatars.put(avatarId, avatar);
            return this;
        }

        public FixturePreparer withUser(String username, User user)
        {
            return withUser(username, user, null);
        }

        public FixturePreparer withUser(String username, User user, PropertySet userProperties)
        {
            this.users.put(username, user);
            this.userProperties.put(user, userProperties != null ? userProperties : new MockPropertySet());
            return this;
        }

        public FixturePreparer withContextPath(String contextPath)
        {
            this.contextPath = contextPath;
            return this;
        }

        void prepare()
        {
            // setup avatar manager
            AvatarManager avatarManager = getMock(AvatarManager.class);
            expect(avatarManager.isUserAvatarsEnabled()).andReturn(avatarsEnabled).anyTimes();
            expect(avatarManager.getAnonymousAvatarId()).andReturn(anonymousAvatarId).anyTimes();
            expect(avatarManager.getDefaultAvatarId(Avatar.Type.USER)).andReturn(defaultUserAvatarId).anyTimes();
            for (Map.Entry<Long, Avatar> entry : avatars.entrySet())
            {
                expect(avatarManager.getById(entry.getKey())).andReturn(entry.getValue()).anyTimes();
            }

            // setup user manager
            UserManager userManager = getMock(UserManager.class);
            for (Map.Entry<String, User> fixtureUser : users.entrySet())
            {
                expect(userManager.getUserObject(fixtureUser.getKey())).andReturn(fixtureUser.getValue()).anyTimes();
            }

            // setup property manager
            UserPropertyManager userPropertyManager = getMock(UserPropertyManager.class);
            for (Map.Entry<User, PropertySet> propertySetEntry : userProperties.entrySet())
            {
                expect(userPropertyManager.getPropertySet(propertySetEntry.getKey())).andReturn(propertySetEntry.getValue()).anyTimes();
            }

            VelocityRequestContext context = createMock(VelocityRequestContext.class);
            expect(context.getBaseUrl()).andReturn(contextPath).anyTimes();

            VelocityRequestContextFactory factory = createMock(VelocityRequestContextFactory.class);
            expect(factory.getJiraVelocityRequestContext()).andReturn(context).anyTimes();

            ApplicationProperties applicationProperties = createMock(ApplicationProperties.class);
            expect(applicationProperties.getEncoding()).andReturn("utf-8").anyTimes();
        }
    }
}
