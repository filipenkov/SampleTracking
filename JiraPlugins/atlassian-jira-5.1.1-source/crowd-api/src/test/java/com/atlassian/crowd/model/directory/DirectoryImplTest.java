package com.atlassian.crowd.model.directory;

import com.atlassian.crowd.model.InternalEntityTemplate;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class DirectoryImplTest
{
    private static final String LONG_STRING = StringUtils.repeat("X", 300);

    @Test(expected=IllegalArgumentException.class)
    public void testDirectoryImpl()
    {
        new DirectoryImpl(new InternalEntityTemplate(0L, LONG_STRING, true, null, null));
    }

    @Test
    public void testUpdateAttributesFrom()
    {
        final DirectoryImpl directory = new DirectoryImpl();
        directory.updateAttributesFrom(ImmutableMap.of("k1", "v1", "k2", "v2"));
        assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), directory.getAttributes());
        directory.updateAttributesFrom(ImmutableMap.of("k2", "v2a", "k3", "v3"));
        assertEquals(ImmutableMap.of("k2", "v2a", "k3", "v3"), directory.getAttributes());
    }

    @Test
    public void toStringSanitisesLdapPasswords()
    {
        DirectoryImpl dir = new DirectoryImpl();

        String s;

        dir.setAttribute("ldap.userdn", "CN=user");
        s = dir.toString();
        assertThat(s, containsString("CN=user"));
        assertThat(s, CoreMatchers.not(containsString("ldap.password")));

        dir.setAttribute("ldap.password", "PASSWORD");
        s = dir.toString();
        assertThat(s, containsString("CN=user"));
        assertThat(s, containsString("ldap.password=********"));
        assertThat(s, CoreMatchers.not(containsString("PASSWORD")));

        assertEquals("PASSWORD", dir.getAttributes().get("ldap.password"));
    }

    @Test
    public void toStringSanitisesRemoteCrowdPasswords()
    {
        DirectoryImpl dir = new DirectoryImpl();

        String s;

        dir.setAttribute("application.name", "test-application");
        s = dir.toString();
        assertThat(s, containsString("test-application"));
        assertThat(s, CoreMatchers.not(containsString("application.password")));

        dir.setAttribute("application.password", "PASSWORD");
        s = dir.toString();
        assertThat(s, containsString("test-application"));
        assertThat(s, containsString("application.password=********"));
        assertThat(s, CoreMatchers.not(containsString("PASSWORD")));

        assertEquals("PASSWORD", dir.getAttributes().get("application.password"));
    }
}
