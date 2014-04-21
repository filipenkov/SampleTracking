package com.atlassian.jira.security.roles.actor;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import mock.user.MockOSUser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestUserRoleActorFactory extends ListeningTestCase
{
    private final RoleActorFactory roleActorFactory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
    {
        public User getUser(final String name)
        {
            return TestUserRoleActorFactory.this.getUser(name);
        }
    }, null);

    User getUser(final String name)
    {
        return new MockOSUser(name);
    }

    @Test
    public void testRoleActorContains() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertTrue(roleActor.contains(getUser("username")));
    }

    @Test
    public void testRoleActorGetUsers() throws Exception
    {
        final RoleActorFactory factory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
        {
            public User getUser(final String name)
            {
                return new MockOSUser(name, "Daniel Boone", "dan@example.com");
            }
        }, null);
        final RoleActor roleActor = factory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        final User user = getUser("username");
        assertTrue(roleActor.contains(user));
        assertEquals(1, roleActor.getUsers().size());
        assertTrue(roleActor.getUsers().contains(user));
        assertEquals("Daniel Boone", roleActor.getDescriptor());
    }

    @Test
    public void testRoleActorNotContains() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertFalse(roleActor.contains(getUser("test")));
    }

    @Test
    public void testRoleActorNotContainsNull() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertFalse(roleActor.contains(null));
    }

    @Test
    public void testUserRoleActorEqualsAndHashcode() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertEquals(roleActor1, roleActor2);
        assertEquals(roleActor2, roleActor1);
        assertEquals(roleActor1.hashCode(), roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeIdIrrelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(2), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertEquals(roleActor1, roleActor2);
        assertEquals(roleActor2, roleActor1);
        assertEquals(roleActor1.hashCode(), roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeProjectRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(2), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeProjectRoleRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(2), UserRoleActorFactory.TYPE, "username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeUsernameRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(2), UserRoleActorFactory.TYPE,
            "another-username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorIncorrectType() throws Exception
    {
        try
        {
            roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), "blah", "username");
            fail("should be bad!");
        }
        catch (final IllegalArgumentException yay)
        {}
    }

    @Test
    public void testRoleActorOptimizeAggregatesMultiple() throws Exception
    {
        Set userRoleActors = new HashSet();
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username"));
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "somedude"));
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "whatsit"));
        userRoleActors = roleActorFactory.optimizeRoleActorSet(userRoleActors);
        assertNotNull(userRoleActors);
        assertEquals(1, userRoleActors.size());
        final RoleActor roleActor = (RoleActor) userRoleActors.iterator().next();
        assertTrue(roleActor instanceof UserRoleActorFactory.AggregateRoleActor);

        assertTrue(roleActor.contains(getUser("username")));
        assertTrue(roleActor.contains(getUser("somedude")));
        assertFalse(roleActor.contains(getUser("whosee-whatsit")));
        assertFalse(roleActor.contains(null));
    }

    @Test
    public void testRoleActorOptimizeDoesNotAggregatesSingle() throws Exception
    {
        Set userRoleActors = new HashSet();
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username"));
        userRoleActors = roleActorFactory.optimizeRoleActorSet(userRoleActors);
        assertNotNull(userRoleActors);
        assertEquals(1, userRoleActors.size());
        final RoleActor roleActor = (RoleActor) userRoleActors.iterator().next();
        assertFalse(roleActor instanceof UserRoleActorFactory.AggregateRoleActor);
        assertTrue(roleActor instanceof UserRoleActorFactory.UserRoleActor);
    }

    @Test
    public void testAggregateRoleActorContains() throws Exception
    {
        Set roleActors = new HashSet();
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "fred"));
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "jim"));
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "david"));

        roleActors = Collections.unmodifiableSet(roleActors);
        roleActors = roleActorFactory.optimizeRoleActorSet(roleActors);
        assertNotNull(roleActors);
        final RoleActor roleActor = ((RoleActor) roleActors.iterator().next());
        assertTrue(roleActor.getUsers().contains(getUser("fred")));
        assertTrue(roleActor.getUsers().contains(getUser("jim")));
        assertTrue(roleActor.getUsers().contains(getUser("david")));
        assertFalse(roleActor.getUsers().contains(getUser("sally")));
    }

    @Test
    public void testIllegalArgumentExceptionIfEntityNotFoundThrownByFactory() throws Exception
    {
        final UserRoleActorFactory factory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
        {
            public User getUser(final String name)
            {
                return null;
            }
        }, null);

        try
        {
            factory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "fred");
            fail("RoleActorDoesNotExistException should have been thrown");
        }
        catch (final RoleActorDoesNotExistException yay)
        {}
    }

}
