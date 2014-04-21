package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @since v4.0
 */
public class TestUserNameComparator extends ListeningTestCase
{
    User adminUser;
    User lowerCaseAdminUser;
    User accentedAdminUser;
    User alfonzUser;
    User accentedAlfonzUser;
    User cecilUser;
    User accentedCecilUser;
    User umAlfonzUser;
    User dudeUser;
    User nooneUser;
    User accentedNooneUser;
    User zooUser;
    User sUser;
    User ssUser;

    @Before
    public void setUp() throws Exception
    {
        // Spanish testing names
        adminUser = new User("Admin", new MockProviderAccessor("OAdministrator", "admin@example.com"), new MockCrowdService());
        lowerCaseAdminUser = new User("admin", new MockProviderAccessor("Padministrator", "admin@example.com"), new MockCrowdService());
        accentedAdminUser = new User("\u00C1dmin", new MockProviderAccessor("Zdministrator", "admin@example.com"), new MockCrowdService());
        dudeUser = new User("dude", new MockProviderAccessor("Dude", "admin@example.com"), new MockCrowdService());
        nooneUser = new User("noone", new MockProviderAccessor("Noone", "admin@example.com"), new MockCrowdService());
        accentedNooneUser = new User("\u00D1oone", new MockProviderAccessor("WNoone", "admin@example.com"), new MockCrowdService());
        zooUser = new User("zoo", new MockProviderAccessor("zoo", "admin@example.com"), new MockCrowdService());

        // Solovak testing names
        alfonzUser = new User("alfonz", new MockProviderAccessor("zalfonz", "admin@example.com"), new MockCrowdService());
        accentedAlfonzUser = new User("\u00e1lfonz", new MockProviderAccessor("alfonz", "admin@example.com"), new MockCrowdService());
        umAlfonzUser = new User("\u00e4lfonz", new MockProviderAccessor("alfonz", "admin@example.com"), new MockCrowdService());
        cecilUser = new User("cecil", new MockProviderAccessor("cecil", "admin@example.com"), new MockCrowdService());
        accentedCecilUser = new User("\u010D\u00E9cil", new MockProviderAccessor("cecil", "admin@example.com"), new MockCrowdService());

        // German testing names
        sUser = new User("aas", new MockProviderAccessor("aas", "admin@example.com"), new MockCrowdService());
        ssUser = new User("aa\u00DF", new MockProviderAccessor("aas", "admin@example.com"), new MockCrowdService());
    }

    @After
    public void tearDown() throws Exception
    {
        adminUser = null;
        lowerCaseAdminUser = null;
        accentedAdminUser = null;
        dudeUser = null;
        nooneUser = null;
        accentedNooneUser = null;
        zooUser = null;
        alfonzUser = null;
        accentedAlfonzUser = null;
        umAlfonzUser = null;
        sUser = null;
        ssUser = null;
    }

    @Test
    public void testSortingInSpanish()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("es", "ES"));

        final List users = EasyList.build(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        // This comes second because the username comes into play
        assertEquals(lowerCaseAdminUser, users.get(1));
        assertEquals(accentedAdminUser, users.get(2));
        assertEquals(dudeUser, users.get(3));
        assertEquals(nooneUser, users.get(4));
        assertEquals(accentedNooneUser, users.get(5));
        assertEquals(zooUser, users.get(6));
    }

    @Test
    public void testSortingInEnglishWithSpanishList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        // This comes second because the username comes into play
        assertEquals(lowerCaseAdminUser, users.get(1));
        assertEquals(accentedAdminUser, users.get(2));
        assertEquals(dudeUser, users.get(3));
        assertEquals(nooneUser, users.get(4));
        assertEquals(accentedNooneUser, users.get(5));
        assertEquals(zooUser, users.get(6));
    }

    @Test
    public void testSortingInSolvak()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("sk"));

        final List users = EasyList.build(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        assertEquals(alfonzUser, users.get(1));
        assertEquals(accentedAlfonzUser, users.get(2));
        assertEquals(umAlfonzUser, users.get(3));
        assertEquals(cecilUser, users.get(4));
        assertEquals(accentedCecilUser, users.get(5));
    }

    @Test
    public void testSortingInEnglishWithSolvakList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        assertEquals(alfonzUser, users.get(1));
        assertEquals(accentedAlfonzUser, users.get(2));
        assertEquals(umAlfonzUser, users.get(3));
        assertEquals(cecilUser, users.get(4));
        assertEquals(accentedCecilUser, users.get(5));
    }

    @Test
    public void testSortingInGerman()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("de"));

        final List users = EasyList.build(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }

    @Test
    public void testSortingInEnglishWithGermanList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }

}
